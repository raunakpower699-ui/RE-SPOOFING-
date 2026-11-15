package com.example.engine

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.view.Display
import android.view.Gravity
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.Window
import android.view.WindowManager
import com.example.model.RefreshRatePermissionStatus
import com.example.model.RefreshRateTarget
import kotlin.math.abs
import kotlin.math.roundToInt

class RefreshRateLockController(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var overlaySurfaceView: SurfaceView? = null
    private var overlayParams: WindowManager.LayoutParams? = null
    @Volatile
    private var isOverlayAnchored: Boolean = false
    @Volatile
    private var isSystemSettingsApplied: Boolean = false
    @Volatile
    private var activeTarget: RefreshRateTarget = RefreshRateTarget.HZ_144

    data class PanelDisplayInfo(
        val resolutionLabel: String,
        val maxHz: Int,
        val supportedHzList: List<Int>,
        val bestModeIdByTargetHz: Map<Int, Int>
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
                    bestModeIdByTargetHz = emptyMap()
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
                }
            }

            PanelDisplayInfo(
                resolutionLabel = "$width x $height (${maxHz}Hz Panel)",
                maxHz = maxHz,
                supportedHzList = distinctHz,
                bestModeIdByTargetHz = modeMap
            )
        } catch (_: Throwable) {
            PanelDisplayInfo(
                resolutionLabel = "1080 x 2400 (144Hz)",
                maxHz = 144,
                supportedHzList = listOf(60, 90, 120, 144),
                bestModeIdByTargetHz = emptyMap()
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
        mainHandler.post {
            try {
                val lp = window.attributes
                if (lockActive) {
                    @Suppress("DEPRECATION")
                    lp.preferredRefreshRate = target.hz.toFloat()
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
        applySystemSettingsRefreshRate(target)
        attachOrUpdateOverlayAnchor(target)
    }

    fun releaseGlobalRefreshRateLock() {
        isSystemSettingsApplied = false
        detachOverlayAnchor()
    }

    private fun applySystemSettingsRefreshRate(target: RefreshRateTarget) {
        try {
            if (!Settings.System.canWrite(context)) {
                isSystemSettingsApplied = false
                return
            }
            val resolver = context.contentResolver
            val hzFloat = target.hz.toFloat()
            val hzInt = target.hz

            // Standard Android + OriginOS / FuntouchOS / MIUI / ColorOS refresh rate keys
            tryPutFloat(resolver, "min_refresh_rate", hzFloat)
            tryPutFloat(resolver, "peak_refresh_rate", hzFloat)
            tryPutInt(resolver, "user_refresh_rate", hzInt)
            tryPutInt(resolver, "vivo_screen_refresh_rate_mode", hzInt)
            isSystemSettingsApplied = true
        } catch (_: Throwable) {
            isSystemSettingsApplied = false
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

    private fun attachOrUpdateOverlayAnchor(target: RefreshRateTarget) {
        try {
            if (!Settings.canDrawOverlays(context)) {
                isOverlayAnchored = false
                return
            }
        } catch (_: Throwable) {
            isOverlayAnchored = false
            return
        }

        val panelInfo = queryPanelDisplayInfo()
        val modeId = panelInfo.bestModeIdByTargetHz[target.hz] ?: 0

        mainHandler.post {
            try {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return@post

                val existingView = overlaySurfaceView
                val existingParams = overlayParams
                if (existingView != null && existingParams != null && existingView.isAttachedToWindow) {
                    @Suppress("DEPRECATION")
                    existingParams.preferredRefreshRate = target.hz.toFloat()
                    if (modeId != 0) {
                        existingParams.preferredDisplayModeId = modeId
                    }
                    wm.updateViewLayout(existingView, existingParams)
                    applySurfaceFrameRate(existingView.holder.surface, target)
                    isOverlayAnchored = true
                    return@post
                }

                val surfaceView = SurfaceView(context).apply {
                    setZOrderOnTop(false)
                    setBackgroundColor(Color.TRANSPARENT)
                    holder.setFormat(PixelFormat.TRANSPARENT)
                    holder.addCallback(object : SurfaceHolder.Callback {
                        override fun surfaceCreated(holder: SurfaceHolder) {
                            applySurfaceFrameRate(holder.surface, activeTarget)
                        }

                        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                            applySurfaceFrameRate(holder.surface, activeTarget)
                        }

                        override fun surfaceDestroyed(holder: SurfaceHolder) {
                        }
                    })
                }

                val params = WindowManager.LayoutParams(
                    1,
                    1,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSPARENT
                ).apply {
                    gravity = Gravity.TOP or Gravity.START
                    x = 0
                    y = 0
                    @Suppress("DEPRECATION")
                    preferredRefreshRate = target.hz.toFloat()
                    if (modeId != 0) {
                        preferredDisplayModeId = modeId
                    }
                }

                wm.addView(surfaceView, params)
                overlaySurfaceView = surfaceView
                overlayParams = params
                isOverlayAnchored = true
            } catch (_: Throwable) {
                isOverlayAnchored = false
            }
        }
    }

    private fun detachOverlayAnchor() {
        mainHandler.post {
            try {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                val view = overlaySurfaceView
                if (wm != null && view != null && view.isAttachedToWindow) {
                    wm.removeViewImmediate(view)
                }
            } catch (_: Throwable) {
            } finally {
                overlaySurfaceView = null
                overlayParams = null
                isOverlayAnchored = false
            }
        }
    }

    private fun applySurfaceFrameRate(surface: Surface?, target: RefreshRateTarget) {
        if (surface == null || !surface.isValid) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                surface.setFrameRate(
                    target.hz.toFloat(),
                    Surface.FRAME_RATE_COMPATIBILITY_FIXED_SOURCE,
                    Surface.CHANGE_FRAME_RATE_ALWAYS
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                surface.setFrameRate(
                    target.hz.toFloat(),
                    Surface.FRAME_RATE_COMPATIBILITY_FIXED_SOURCE
                )
            }
        } catch (_: Throwable) {
        }
    }
}
