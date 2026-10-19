package com.example.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.os.SystemClock
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class RedMagicHardwareController(private val context: Context) {

    private val isGpuRunning = AtomicBoolean(false)
    private val isAudioDspRunning = AtomicBoolean(false)
    private val isSensorRunning = AtomicBoolean(false)
    private val isStorageRunning = AtomicBoolean(false)

    private var gpuJob: Job? = null
    private var audioJob: Job? = null
    private var sensorJob: Job? = null
    private var storageJob: Job? = null

    private var audioTrack: AudioTrack? = null

    fun startOpenGlGpuFloorLock(scope: CoroutineScope, isExtreme: Boolean) {
        if (isGpuRunning.getAndSet(true)) return

        gpuJob = scope.launch(Dispatchers.Default) {
            var eglDisplay: EGLDisplay? = null
            var eglContext: EGLContext? = null
            var eglSurface: EGLSurface? = null

            try {
                eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
                val version = IntArray(2)
                EGL14.eglInitialize(eglDisplay, version, 0, version, 1)

                val configAttribs = intArrayOf(
                    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                    EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_ALPHA_SIZE, 8,
                    EGL14.EGL_NONE
                )
                val configs = arrayOfNulls<EGLConfig>(1)
                val numConfigs = IntArray(1)
                EGL14.eglChooseConfig(eglDisplay, configAttribs, 0, configs, 0, 1, numConfigs, 0)
                val config = configs[0] ?: return@launch

                val contextAttribs = intArrayOf(
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL14.EGL_NONE
                )
                eglContext = EGL14.eglCreateContext(eglDisplay, config, EGL14.EGL_NO_CONTEXT, contextAttribs, 0)

                val pbufferAttribs = intArrayOf(
                    EGL14.EGL_WIDTH, 64,
                    EGL14.EGL_HEIGHT, 64,
                    EGL14.EGL_NONE
                )
                eglSurface = EGL14.eglCreatePbufferSurface(eglDisplay, config, pbufferAttribs, 0)
                EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)

                EGL14.eglSwapInterval(eglDisplay, 0)
                GLES20.glDisable(GLES20.GL_DITHER)

                val vertexShaderCode = """
                    attribute vec4 vPosition;
                    void main() {
                        gl_Position = vPosition;
                    }
                """.trimIndent()

                val fragmentShaderCode = """
                    precision mediump float;
                    uniform vec2 uResolution;
                    uniform float uTime;
                    void main() {
                        vec2 st = gl_FragCoord.xy / uResolution.xy;
                        vec3 color = vec3(0.0);
                        float d = distance(st, vec2(0.5));
                        color = vec3(sin(uTime + d * 10.0), cos(uTime - d * 10.0), sin(uTime * 0.5));
                        gl_FragColor = vec4(color, 1.0);
                    }
                """.trimIndent()

                val program = createProgram(vertexShaderCode, fragmentShaderCode)
                val vPositionLoc = GLES20.glGetAttribLocation(program, "vPosition")
                val uResolutionLoc = GLES20.glGetUniformLocation(program, "uResolution")
                val uTimeLoc = GLES20.glGetUniformLocation(program, "uTime")

                val triangleCoords = floatArrayOf(
                    -1.0f, -1.0f, 0.0f,
                     1.0f, -1.0f, 0.0f,
                    -1.0f,  1.0f, 0.0f,
                     1.0f,  1.0f, 0.0f
                )
                val vertexBuffer = ByteBuffer.allocateDirect(triangleCoords.size * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(triangleCoords)
                vertexBuffer.position(0)

                var frameCount = 0
                while (isActive && isGpuRunning.get()) {
                    GLES20.glViewport(0, 0, 64, 64)
                    GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)
                    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

                    GLES20.glUseProgram(program)
                    GLES20.glEnableVertexAttribArray(vPositionLoc)
                    GLES20.glVertexAttribPointer(vPositionLoc, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

                    GLES20.glUniform2f(uResolutionLoc, 64f, 64f)
                    GLES20.glUniform1f(uTimeLoc, frameCount * 0.05f)

                    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
                    GLES20.glFinish()

                    frameCount++
                    if (isExtreme) {
                        // Tight render loop
                    } else {
                        SystemClock.sleep(1)
                    }
                }
            } catch (_: Throwable) {
            } finally {
                try {
                    if (eglDisplay != null) {
                        EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
                        if (eglSurface != null) EGL14.eglDestroySurface(eglDisplay, eglSurface)
                        if (eglContext != null) EGL14.eglDestroyContext(eglDisplay, eglContext)
                        EGL14.eglTerminate(eglDisplay)
                    }
                } catch (_: Throwable) {
                }
            }
        }
    }

    fun stopOpenGlGpuFloorLock() {
        isGpuRunning.set(false)
        gpuJob?.cancel()
        gpuJob = null
    }

    fun startLowLatencyAudioDspLock(scope: CoroutineScope) {
        if (isAudioDspRunning.getAndSet(true)) return

        audioJob = scope.launch(Dispatchers.Default) {
            try {
                val sampleRate = 48000
                val bufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(1024)

                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setFlags(AudioAttributes.FLAG_LOW_LATENCY)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                    .build()

                audioTrack?.play()
                val silentBuffer = ShortArray(bufferSize / 2)
                while (isActive && isAudioDspRunning.get()) {
                    audioTrack?.write(silentBuffer, 0, silentBuffer.size)
                }
            } catch (_: Throwable) {
            }
        }
    }

    fun stopLowLatencyAudioDspLock() {
        isAudioDspRunning.set(false)
        audioJob?.cancel()
        audioJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Throwable) {
        } finally {
            audioTrack = null
        }
    }

    fun startHighRateSensorPipelineLock(scope: CoroutineScope) {
        if (isSensorRunning.getAndSet(true)) return
        sensorJob = scope.launch(Dispatchers.Default) {
            while (isActive && isSensorRunning.get()) {
                SystemClock.sleep(7) // ~144Hz
            }
        }
    }

    fun stopHighRateSensorPipelineLock() {
        isSensorRunning.set(false)
        sensorJob?.cancel()
        sensorJob = null
    }

    fun startUfsStorageKeepAlive(scope: CoroutineScope) {
        if (isStorageRunning.getAndSet(true)) return
        storageJob = scope.launch(Dispatchers.IO) {
            try {
                val file = File(context.cacheDir, "ufs_keepalive.tmp")
                val raf = RandomAccessFile(file, "rw")
                val buf = ByteArray(4096) { 0x55.toByte() }
                while (isActive && isStorageRunning.get()) {
                    raf.seek(0)
                    raf.write(buf)
                    raf.fd.sync()
                    SystemClock.sleep(100)
                }
                raf.close()
                file.delete()
            } catch (_: Throwable) {
            }
        }
    }

    fun stopUfsStorageKeepAlive() {
        isStorageRunning.set(false)
        storageJob?.cancel()
        storageJob = null
    }

    private fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        return program
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }
}
