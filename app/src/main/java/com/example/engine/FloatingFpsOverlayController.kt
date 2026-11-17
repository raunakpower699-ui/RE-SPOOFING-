package com.example.engine

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.TypedValue
import android.view.Choreographer
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import com.example.model.RefreshRateTarget
import kotlin.math.roundToInt

/**
 * Lightweight, non-focus-stealing floating FPS overlay HUD rendered over games
 * via WindowManager TYPE_APPLICATION_OVERLAY.
 *
 * Uses Choreographer VSync deltas internally and updates its compact TextView/Canvas
 * only every 500ms (with ZERO wm.updateViewLayout() calls unless dragged or Hz changes),
 * ensuring zero CPU/GPU contention during gameplay.
 */
class FloatingFpsOverlayController(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())

    private var rootContainer: LinearLayout? = null
    private var fpsTextView: TextView? = null
    private var statusBadgeView: TextView? = null
    private var detailTextView: TextView? = null
    private var sparklineView: FpsSparklineView? = null
    private var containerBackground: GradientDrawable? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    @Volatile
    var isOverlayShowing: Boolean = false
        private set

    @Volatile
    private var currentTarget: RefreshRateTarget = RefreshRateTarget.HZ_144

    @Volatile
    private var effectiveTargetHz: Float = 144f

    @Volatile
    private var currentCpuTempCelsius: Float = 40f

    @Volatile
    private var isThermalThrottling: Boolean = false

    @Volatile
    var liveFps: Int = 144
        private set

    @Volatile
    var minFps: Int = 144
        private set

    @Volatile
    var liveFrameTimeMs: Float = 6.9f
        private set

    @Volatile
    var stabilityPercent: Int = 100
        private set

    @Volatile
    var fpsHistory: List<Int> = List(16) { 144 }
        private set

    var onUserClosedOverlay: (() -> Unit)? = null

    private var isMeasuring = false
    private var lastFrameNanos = 0L
    private var windowStartNanos = 0L
    private var frameCountInWindow = 0
    private var droppedCountInWindow = 0
    private var totalFramesInWindow = 0
    private var worstDeltaMsInWindow = 0f

    private val choreographerCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isMeasuring) return

            if (lastFrameNanos > 0L) {
                val deltaMs = (frameTimeNanos - lastFrameNanos) / 1_000_000f
                if (deltaMs in 1.0f..500.0f) {
                    liveFrameTimeMs = deltaMs
                    totalFramesInWindow++
                    val targetMs = 1000f / effectiveTargetHz.coerceAtLeast(60f)
                    if (deltaMs > targetMs * 1.45f) {
                        droppedCountInWindow++
                    }
                    if (deltaMs > worstDeltaMsInWindow) {
                        worstDeltaMsInWindow = deltaMs
                    }
                }
            }
            lastFrameNanos = frameTimeNanos
            frameCountInWindow++

            if (windowStartNanos == 0L) {
                windowStartNanos = frameTimeNanos
            } else if (frameTimeNanos - windowStartNanos >= 500_000_000L) {
                val elapsedSec = (frameTimeNanos - windowStartNanos) / 1_000_000_000.0
                val measured = (frameCountInWindow / elapsedSec).roundToInt().coerceIn(1, 240)
                liveFps = measured

                val instMin = if (worstDeltaMsInWindow > 0f) {
                    (1000f / worstDeltaMsInWindow).roundToInt().coerceIn(1, measured)
                } else {
                    measured
                }
                minFps = instMin

                val stability = if (totalFramesInWindow > 0) {
                    (((totalFramesInWindow - droppedCountInWindow).toFloat() / totalFramesInWindow.toFloat()) * 100f)
                        .roundToInt()
                        .coerceIn(0, 100)
                } else {
                    100
                }
                stabilityPercent = stability
                fpsHistory = (fpsHistory.drop(1) + measured).takeLast(16)

                frameCountInWindow = 0
                droppedCountInWindow = 0
                totalFramesInWindow = 0
                worstDeltaMsInWindow = 0f
                windowStartNanos = frameTimeNanos

                refreshOverlayViews()
            }

            try {
                Choreographer.getInstance().postFrameCallback(this)
            } catch (_: Throwable) {
            }
        }
    }

    fun show(
        target: RefreshRateTarget,
        cpuTempCelsius: Float,
        thermalThrottling: Boolean,
        preferredModeId: Int = 0,
        matchedPanelHz: Float = target.hz.toFloat()
    ): Boolean {
        currentTarget = target
        effectiveTargetHz = matchedPanelHz
        currentCpuTempCelsius = cpuTempCelsius
        isThermalThrottling = thermalThrottling

        val canOverlay = try {
            Settings.canDrawOverlays(context)
        } catch (_: Throwable) {
            false
        }
        if (!canOverlay) {
            isOverlayShowing = false
            return false
        }
        isOverlayShowing = true

        mainHandler.post {
            try {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return@post
                if (rootContainer != null && rootContainer?.isAttachedToWindow == true) {
                    updateTargetRefreshRateInternal(wm, target, preferredModeId, matchedPanelHz)
                    refreshOverlayViews()
                    isOverlayShowing = true
                    startMeasuringInternal()
                    return@post
                }

                val view = buildHudView()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                    try {
                        view.requestedFrameRate = effectiveTargetHz
                    } catch (_: Throwable) {
                    }
                }
                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.TOP or Gravity.START
                    x = dp(14)
                    y = dp(46)
                    @Suppress("DEPRECATION")
                    preferredRefreshRate = effectiveTargetHz
                    if (preferredModeId != 0) {
                        preferredDisplayModeId = preferredModeId
                    }
                }

                attachDragBehavior(view, wm, params)
                wm.addView(view, params)
                rootContainer = view
                layoutParams = params
                isOverlayShowing = true
                startMeasuringInternal()
                refreshOverlayViews()
            } catch (_: Throwable) {
                isOverlayShowing = false
            }
        }
        return true
    }

    fun hide() {
        stopMeasuringInternal()
        isOverlayShowing = false
        mainHandler.post {
            try {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                val view = rootContainer
                if (wm != null && view != null && view.isAttachedToWindow) {
                    wm.removeViewImmediate(view)
                }
            } catch (_: Throwable) {
            } finally {
                rootContainer = null
                fpsTextView = null
                statusBadgeView = null
                detailTextView = null
                sparklineView = null
                containerBackground = null
                layoutParams = null
            }
        }
    }

    fun updateTargetRefreshRate(
        target: RefreshRateTarget,
        preferredModeId: Int = 0,
        matchedPanelHz: Float = target.hz.toFloat()
    ) {
        currentTarget = target
        effectiveTargetHz = matchedPanelHz
        mainHandler.post {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return@post
            updateTargetRefreshRateInternal(wm, target, preferredModeId, matchedPanelHz)
            refreshOverlayViews()
        }
    }

    fun updateTelemetry(cpuTempCelsius: Float, thermalThrottling: Boolean) {
        currentCpuTempCelsius = cpuTempCelsius
        isThermalThrottling = thermalThrottling
        if (isOverlayShowing) {
            mainHandler.post { refreshOverlayViews() }
        }
    }

    private fun updateTargetRefreshRateInternal(
        wm: WindowManager,
        target: RefreshRateTarget,
        preferredModeId: Int,
        matchedPanelHz: Float
    ) {
        val view = rootContainer ?: return
        val params = layoutParams ?: return
        if (!view.isAttachedToWindow) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            try {
                view.requestedFrameRate = matchedPanelHz
            } catch (_: Throwable) {
            }
        }
        if (params.preferredRefreshRate != matchedPanelHz) {
            @Suppress("DEPRECATION")
            params.preferredRefreshRate = matchedPanelHz
            if (preferredModeId != 0) {
                params.preferredDisplayModeId = preferredModeId
            }
            try {
                wm.updateViewLayout(view, params)
            } catch (_: Throwable) {
            }
        }
    }

    private fun startMeasuringInternal() {
        if (isMeasuring) return
        isMeasuring = true
        lastFrameNanos = 0L
        windowStartNanos = 0L
        frameCountInWindow = 0
        droppedCountInWindow = 0
        totalFramesInWindow = 0
        worstDeltaMsInWindow = 0f
        liveFps = currentTarget.hz
        minFps = currentTarget.hz
        stabilityPercent = 100
        fpsHistory = List(16) { currentTarget.hz }
        try {
            Choreographer.getInstance().postFrameCallback(choreographerCallback)
        } catch (_: Throwable) {
        }
    }

    private fun stopMeasuringInternal() {
        isMeasuring = false
        try {
            Choreographer.getInstance().removeFrameCallback(choreographerCallback)
        } catch (_: Throwable) {
        }
    }

    private fun buildHudView(): LinearLayout {
        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(14).toFloat()
            setColor(Color.parseColor("#E3090E1A"))
            setStroke(dp(1) + 1, Color.parseColor("#00E676"))
        }
        containerBackground = bg

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = bg
            setPadding(dp(11), dp(7), dp(11), dp(7))
            elevation = dp(8).toFloat()
        }

        // Top row: Live FPS + Status Badge + Close button
        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val fpsView = TextView(context).apply {
            text = "${currentTarget.hz} FPS"
            setTextColor(Color.parseColor("#00E676"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        fpsTextView = fpsView

        val badgeView = TextView(context).apply {
            text = "STABLE"
            setTextColor(Color.parseColor("#00E5FF"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 9.5f)
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            setPadding(dp(7), dp(2), dp(4), dp(2))
        }
        statusBadgeView = badgeView

        val closeBtn = TextView(context).apply {
            text = "✕"
            contentDescription = "Hide Floating FPS Overlay"
            setTextColor(Color.parseColor("#9AA7BD"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(8), dp(2), dp(2), dp(2))
            setOnClickListener {
                hide()
                onUserClosedOverlay?.invoke()
            }
        }

        topRow.addView(fpsView)
        topRow.addView(badgeView)
        topRow.addView(closeBtn)

        // Second row: Frame time, Stability %, Temp
        val detailView = TextView(context).apply {
            text = "6.9ms • 100% • 40.0°C"
            setTextColor(Color.parseColor("#DCE6F5"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }
        detailTextView = detailView

        // Micro 16-bar FPS sparkline
        val spark = FpsSparklineView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(14)
            ).apply {
                topMargin = dp(4)
            }
        }
        sparklineView = spark

        root.addView(topRow)
        root.addView(detailView)
        root.addView(spark)
        return root
    }

    private fun refreshOverlayViews() {
        val fps = liveFps
        val target = effectiveTargetHz.roundToInt().coerceAtLeast(60)
        val ratio = fps.toFloat() / target.toFloat()

        val accentColor = when {
            isThermalThrottling || ratio < 0.72f -> Color.parseColor("#FF2A4D")
            ratio < 0.90f -> Color.parseColor("#FFB300")
            else -> Color.parseColor("#00E676")
        }

        val badgeText = when {
            isThermalThrottling -> "THROTTLE"
            ratio < 0.72f -> "DROP"
            ratio < 0.90f -> "WARM"
            else -> "STABLE"
        }

        fpsTextView?.apply {
            text = "$fps FPS"
            setTextColor(accentColor)
        }
        statusBadgeView?.apply {
            text = badgeText
            setTextColor(accentColor)
        }
        detailTextView?.text = String.format(
            java.util.Locale.US,
            "%.1fms • %d%% • %.0f°C",
            liveFrameTimeMs,
            stabilityPercent,
            currentCpuTempCelsius
        )
        containerBackground?.setStroke(dp(1) + 1, accentColor)
        sparklineView?.updateSamples(fpsHistory, target)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun attachDragBehavior(
        view: View,
        wm: WindowManager,
        params: WindowManager.LayoutParams
    ) {
        var startX = 0
        var startY = 0
        var touchStartX = 0f
        var touchStartY = 0f

        view.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startX = params.x
                    startY = params.y
                    touchStartX = event.rawX
                    touchStartY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = startX + (event.rawX - touchStartX).toInt()
                    params.y = startY + (event.rawY - touchStartY).toInt()
                    try {
                        if (view.isAttachedToWindow) {
                            wm.updateViewLayout(view, params)
                        }
                    } catch (_: Throwable) {
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            context.resources.displayMetrics
        ).roundToInt()
    }

    private class FpsSparklineView(context: Context) : View(context) {
        private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#26FFFFFF")
        }
        private var samples: List<Int> = List(16) { 144 }
        private var targetHz: Int = 144

        fun updateSamples(newSamples: List<Int>, newTargetHz: Int) {
            samples = newSamples
            targetHz = newTargetHz.coerceAtLeast(60)
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat()
            val h = height.toFloat()
            if (w <= 0f || h <= 0f || samples.isEmpty()) return

            canvas.drawRoundRect(0f, 0f, w, h, 4f, 4f, bgPaint)

            val count = samples.size
            val gap = 2f
            val barWidth = ((w - gap * (count + 1)) / count).coerceAtLeast(2f)

            samples.forEachIndexed { idx, fps ->
                val ratio = (fps.toFloat() / targetHz.toFloat()).coerceIn(0.15f, 1.0f)
                barPaint.color = when {
                    ratio < 0.72f -> Color.parseColor("#FF2A4D")
                    ratio < 0.90f -> Color.parseColor("#FFB300")
                    else -> Color.parseColor("#00E676")
                }
                val left = gap + idx * (barWidth + gap)
                val barHeight = (h - 4f) * ratio
                val top = (h - 2f) - barHeight
                canvas.drawRoundRect(left, top, left + barWidth, h - 2f, 2f, 2f, barPaint)
            }
        }
    }
}
