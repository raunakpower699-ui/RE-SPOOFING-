package com.example.engine

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.SurfaceTexture
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.view.Choreographer
import android.view.Display
import android.view.Gravity
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import com.example.model.RefreshRatePermissionStatus
import com.example.model.RefreshRateTarget
import kotlin.math.abs
import kotlin.math.roundToInt

class RefreshRateLockController(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var overlayAnchorView: FrameLayout? = null
    private var overlaySurface: Surface? = null
    private var overlayParams: WindowManager.LayoutParams? = null
    @Volatile
    private var isOverlayAnchored: Boolean = false
    @Volatile
    private var isSystemSettingsApplied: Boolean = false
    @Volatile
    private var activeTarget: RefreshRateTarget = RefreshRateTarget.HZ_144
    @Volatile
    private var lastAppliedOverlayHz: Int = -1
    @Volatile
    private var pendingOverlayTargetHz: Float = 144f

    @Volatile
    var liveMeasuredFps: Int = 144
        private set
    @Volatile
    var liveFrameTimeMs: Float = 6.9f
        private set
    @Volatile
    var droppedFramesCount: Int = 0
        private set
    private var isMonitoringFrames = false
    private var lastFrameNanos = 0L
    private var frameCounter = 0
    private var windowStartNanos = 0L

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isMonitoringFrames) return
            if (lastFrameNanos > 0L) {
                val deltaNs = frameTimeNanos - lastFrameNanos
                val deltaMs = deltaNs / 1_000_000f
                if (deltaMs in 1.0f..500.0f) {
                    liveFrameTimeMs = deltaMs
                    val targetMs = 1000f / activeTarget.hz.toFloat()
                    if (deltaMs > targetMs * 1.45f) {
                        droppedFramesCount++
                    }
                }
            }
            lastFrameNanos = frameTimeNanos
            frameCounter++
            if (windowStartNanos == 0L) {
                windowStartNanos = frameTimeNanos
            } else if (frameTimeNanos - windowStartNanos >= 1_000_000_000L) {
                val elapsedSec = (frameTimeNanos - windowStartNanos) / 1_000_000_000.0
                val fps = (frameCounter / elapsedSec).roundToInt().coerceIn(1, 240)
                liveMeasuredFps = fps
                frameCounter = 0
                windowStartNanos = frameTimeNanos
            }
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    fun startFrameMonitor() {
        mainHandler.post {
            if (isMonitoringFrames) return@post
            isMonitoringFrames = true
            lastFrameNanos = 0L
            frameCounter = 0
            windowStartNanos = 0L
            try {
                Choreographer.getInstance().postFrameCallback(frameCallback)
            } catch (_: Throwable) {
            }
        }
    }

    fun stopFrameMonitor() {
        mainHandler.post {
            isMonitoringFrames = false
            try {
                Choreographer.getInstance().removeFrameCallback(frameCallback)
            } catch (_: Throwable) {
            }
        }
    }

    fun resetDroppedFrames() {
        droppedFramesCount = 0
    }

    data class PanelDisplayInfo(
        val resolutionLabel: String,
        val maxHz: Int,
        val supportedHzList: List<Int>,
        val bestModeIdByTargetHz: Map<Int, Int>,
        val bestRefreshRateByTargetHz: Map<Int, Float> = emptyMap()
    )

    fun queryPanelDisplayInfo(): PanelDisplayInfo {
        return try {
            val dm = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
            val display = dm?.getDisplay(Display.DEFAULT_DISPLAY)
            if (display == null) {
                return PanelDisplayInfo(
                    resolutionLabel = "1080 x 2400 (144Hz)",
                    maxHz = 144,
                    supportedHzList = listOf(60, 90, 120, 144),
                    bestModeIdByTargetHz = emptyMap(),
                    bestRefreshRateByTargetHz = emptyMap()
                )
            }
            val modes = display.supportedModes ?: emptyArray()
            val currentMode = display.mode
            val width = currentMode?.physicalWidth ?: 1080
            val height = currentMode?.physicalHeight ?: 2400

            val distinctHz = modes
                .map { it.refreshRate.roundToInt() }
                .filter { it >= 60 }
                .distinct()
                .sorted()
                .ifEmpty { listOf(60, 90, 120, 144) }

            val maxHz = distinctHz.maxOrNull() ?: 144

            // Match modes preferring native physical resolution first, then closest Hz >= target (or max available)
            val modeMap = mutableMapOf<Int, Int>()
            val rateMap = mutableMapOf<Int, Float>()
            for (target in RefreshRateTarget.entries) {
                val targetHz = target.hz.toFloat()
                val sameResModes = modes.filter {
                    it.physicalWidth == width && it.physicalHeight == height
                }.ifEmpty { modes.toList() }

                val exactOrHigher = sameResModes
                    .filter { it.refreshRate >= targetHz - 1.5f }
                    .minByOrNull { abs(it.refreshRate - targetHz) }

                val fallbackHighest = sameResModes.maxByOrNull { it.refreshRate }
                val chosen = exactOrHigher ?: fallbackHighest
                if (chosen != null) {
                    modeMap[target.hz] = chosen.modeId
                    rateMap[target.hz] = chosen.refreshRate
                }
            }

            PanelDisplayInfo(
                resolutionLabel = "$width x $height (${maxHz}Hz Panel)",
                maxHz = maxHz,
                supportedHzList = distinctHz,
                bestModeIdByTargetHz = modeMap,
                bestRefreshRateByTargetHz = rateMap
            )
        } catch (_: Throwable) {
            PanelDisplayInfo(
                resolutionLabel = "1080 x 2400 (144Hz)",
                maxHz = 144,
                supportedHzList = listOf(60, 90, 120, 144),
                bestModeIdByTargetHz = emptyMap(),
                bestRefreshRateByTargetHz = emptyMap()
            )
        }
    }

    fun checkPermissionStatus(): RefreshRatePermissionStatus {
        val panelInfo = queryPanelDisplayInfo()
        val canOverlay = try {
            Settings.canDrawOverlays(context)
        } catch (_: Throwable) {
            false
        }
        val canWrite = try {
            Settings.System.canWrite(context)
        } catch (_: Throwable) {
            false
        }
        val batteryIgnored = try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            pm?.isIgnoringBatteryOptimizations(context.packageName) ?: false
        } catch (_: Throwable) {
            false
        }

        return RefreshRatePermissionStatus(
            canDrawOverlays = canOverlay,
            canWriteSystemSettings = canWrite,
            isBatteryOptimizationIgnored = batteryIgnored,
            panelMaxHz = panelInfo.maxHz,
            supportedPanelHz = panelInfo.supportedHzList,
            globalOverlayAnchorActive = isOverlayAnchored,
            systemSettingsLocked = isSystemSettingsApplied
        )
    }

    fun applyWindowRefreshRateLock(window: Window?, target: RefreshRateTarget, lockActive: Boolean) {
        if (window == null) return
        val panelInfo = queryPanelDisplayInfo()
        val modeId = panelInfo.bestModeIdByTargetHz[target.hz] ?: 0
        val effectiveHz = panelInfo.bestRefreshRateByTargetHz[target.hz] ?: target.hz.toFloat()
        mainHandler.post {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                    try {
                        window.isFrameRatePowerSavingsBalanced = !lockActive
                        window.decorView.requestedFrameRate = if (lockActive) effectiveHz else 0f
                    } catch (_: Throwable) {
                    }
                }
                val lp = window.attributes
                if (lockActive) {
                    @Suppress("DEPRECATION")
                    lp.preferredRefreshRate = effectiveHz
                    if (modeId != 0) {
                        lp.preferredDisplayModeId = modeId
                    }
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    @Suppress("DEPRECATION")
                    lp.preferredRefreshRate = 0f
                    lp.preferredDisplayModeId = 0
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                window.attributes = lp
            } catch (_: Throwable) {
            }
        }
    }

    fun lockRefreshRateGlobally(target: RefreshRateTarget) {
        activeTarget = target
        val panelInfo = queryPanelDisplayInfo()
        val effectiveHz = panelInfo.bestRefreshRateByTargetHz[target.hz] ?: target.hz.toFloat()
        pendingOverlayTargetHz = effectiveHz
        applySystemSettingsRefreshRate(effectiveHz)
        attachOrUpdateOverlayAnchor(target, panelInfo)
    }

    fun releaseGlobalRefreshRateLock() {
        isSystemSettingsApplied = false
        lastAppliedOverlayHz = -1
        detachOverlayAnchor()
    }

    private fun applySystemSettingsRefreshRate(effectiveHz: Float) {
        try {
            if (!Settings.System.canWrite(context)) {
                isSystemSettingsApplied = false
                return
            }
            val resolver = context.contentResolver
            val hzFloat = effectiveHz
            val hzInt = effectiveHz.roundToInt()

            // Only write keys if they drifted (avoids unnecessary ContentResolver churn during gaming)
            if (readFloatOrNull(resolver, "min_refresh_rate") != hzFloat) {
                tryPutFloat(resolver, "min_refresh_rate", hzFloat)
            }
            if (readFloatOrNull(resolver, "peak_refresh_rate") != hzFloat) {
                tryPutFloat(resolver, "peak_refresh_rate", hzFloat)
            }
            if (readIntOrNull(resolver, "user_refresh_rate") != hzInt) {
                tryPutInt(resolver, "user_refresh_rate", hzInt)
            }
            if (readIntOrNull(resolver, "vivo_screen_refresh_rate_mode") != hzInt) {
                tryPutInt(resolver, "vivo_screen_refresh_rate_mode", hzInt)
            }
            isSystemSettingsApplied = true
        } catch (_: Throwable) {
            isSystemSettingsApplied = false
        }
    }

    private fun readFloatOrNull(resolver: android.content.ContentResolver, key: String): Float? {
        return try {
            Settings.System.getFloat(resolver, key)
        } catch (_: Throwable) {
            null
        }
    }

    private fun readIntOrNull(resolver: android.content.ContentResolver, key: String): Int? {
        return try {
            Settings.System.getInt(resolver, key)
        } catch (_: Throwable) {
            null
        }
    }

    private fun tryPutFloat(resolver: android.content.ContentResolver, key: String, value: Float) {
        try {
            Settings.System.putFloat(resolver, key, value)
        } catch (_: Throwable) {
        }
    }

    private fun tryPutInt(resolver: android.content.ContentResolver, key: String, value: Int) {
        try {
            Settings.System.putInt(resolver, key, value)
        } catch (_: Throwable) {
        }
    }

    private fun attachOrUpdateOverlayAnchor(target: RefreshRateTarget, panelInfo: PanelDisplayInfo = queryPanelDisplayInfo()) {
        try {
            if (!Settings.canDrawOverlays(context)) {
                isOverlayAnchored = false
                return
            }
        } catch (_: Throwable) {
            isOverlayAnchored = false
            return
        }

        val modeId = panelInfo.bestModeIdByTargetHz[target.hz] ?: 0
        val effectiveHz = panelInfo.bestRefreshRateByTargetHz[target.hz] ?: target.hz.toFloat()
        pendingOverlayTargetHz = effectiveHz

        mainHandler.post {
            try {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return@post

                val existingView = overlayAnchorView
                val existingParams = overlayParams
                if (existingView != null && existingParams != null && existingView.isAttachedToWindow) {
                    applySurfaceFrameRate(overlaySurface, effectiveHz)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                        try {
                            existingView.requestedFrameRate = effectiveHz
                        } catch (_: Throwable) {
                        }
                    }
                    if (lastAppliedOverlayHz != target.hz) {
                        @Suppress("DEPRECATION")
                        existingParams.preferredRefreshRate = effectiveHz
                        if (modeId != 0) {
                            existingParams.preferredDisplayModeId = modeId
                        }
                        wm.updateViewLayout(existingView, existingParams)
                        lastAppliedOverlayHz = target.hz
                    }
                    isOverlayAnchored = true
                    return@post
                }

                // Micro 2x2 anchor with imperceptible alpha=1/255 + TextureView Surface.setFrameRate():
                // Ensures Android 15/16 SurfaceFlinger NEVER culls the vote layer and holds DisplayModeDirector at target Hz.
                val anchorRoot = FrameLayout(context).apply {
                    setBackgroundColor(Color.argb(1, 0, 0, 0))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                        try {
                            requestedFrameRate = effectiveHz
                        } catch (_: Throwable) {
                        }
                    }
                }

                val textureView = TextureView(context).apply {
                    isOpaque = false
                    alpha = 0.01f
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(st: SurfaceTexture, width: Int, height: Int) {
                            try {
                                val surf = Surface(st)
                                val canvas = surf.lockCanvas(null)
                                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                                surf.unlockCanvasAndPost(canvas)
                                overlaySurface = surf
                                applySurfaceFrameRate(surf, pendingOverlayTargetHz)
                            } catch (_: Throwable) {
                            }
                        }

                        override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, width: Int, height: Int) {}
                        override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
                            try {
                                overlaySurface?.release()
                            } catch (_: Throwable) {
                            }
                            overlaySurface = null
                            return true
                        }

                        override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}
                    }
                }
                anchorRoot.addView(textureView, FrameLayout.LayoutParams(2, 2))

                val params = WindowManager.LayoutParams(
                    2,
                    2,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.TOP or Gravity.START
                    x = 0
                    y = 0
                    @Suppress("DEPRECATION")
                    preferredRefreshRate = effectiveHz
                    if (modeId != 0) {
                        preferredDisplayModeId = modeId
                    }
                }

                wm.addView(anchorRoot, params)
                overlayAnchorView = anchorRoot
                overlayParams = params
                lastAppliedOverlayHz = target.hz
                isOverlayAnchored = true
            } catch (_: Throwable) {
                isOverlayAnchored = false
            }
        }
    }

    private fun applySurfaceFrameRate(surface: Surface?, targetHz: Float) {
        if (surface == null || !surface.isValid) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                surface.setFrameRate(
                    targetHz,
                    Surface.FRAME_RATE_COMPATIBILITY_FIXED_SOURCE,
                    Surface.CHANGE_FRAME_RATE_ALWAYS
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                @Suppress("DEPRECATION")
                surface.setFrameRate(
                    targetHz,
                    Surface.FRAME_RATE_COMPATIBILITY_FIXED_SOURCE
                )
            }
        } catch (_: Throwable) {
        }
    }

    private fun detachOverlayAnchor() {
        mainHandler.post {
            try {
                overlaySurface?.release()
            } catch (_: Throwable) {
            } finally {
                overlaySurface = null
            }
            try {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                val view = overlayAnchorView
                if (wm != null && view != null && view.isAttachedToWindow) {
                    wm.removeViewImmediate(view)
                }
            } catch (_: Throwable) {
            } finally {
                overlayAnchorView = null
                overlayParams = null
                isOverlayAnchored = false
            }
        }
    }
}
