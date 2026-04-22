package com.cb.horizontalRuler

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.OverScroller
import androidx.core.content.ContextCompat
import com.cb.horizontalRuler.interfaces.HorizontalRulerListener
import com.cb.horizontalscrollview.R
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

class HorizontalRuler @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyle, defStyleRes) {
    companion object {
        private const val DEFAULT_MINOR_TICKS_PER_MAJOR = 10
        private const val DEFAULT_SNAP_DURATION_MS = 160
    }

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val baselinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val edgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val overScroller = OverScroller(context)
    private val viewConfig = ViewConfiguration.get(context)

    private var velocityTracker: VelocityTracker? = null
    private var lastTouchX = 0f
    private var isFlinging = false
    private var isSnapping = false
    private var lastBroadcastValue = Float.NaN

    var scrollListener: HorizontalRulerListener? = null
    private var onValueChangeListener: ((Float) -> Unit)? = null

    var minValue = 0f
        private set
    var maxValue = 100f
        private set
    var initialValue = (minValue + maxValue) / 2f
        private set
    var currentPositionValue: Float = 0f
        private set

    private var tickColor: Int
    private var indicatorColor: Int
    private var rulerBackgroundColor: Int
    private var edgeColor: Int

    private var majorTickSpacingPx: Float
    private var majorTickHeightPx: Float
    private var mediumTickHeightPx: Float
    private var minorTickHeightPx: Float
    private var tickStrokeWidthPx: Float
    private var indicatorWidthPx: Float
    private var indicatorHeightPx: Float
    private var baselineStrokeWidthPx: Float
    private var edgeStrokeWidthPx: Float
    private var baselinePaddingBottomPx: Float
    private var edgeInsetPx: Float
    private var stepValue: Float
    private var minorTicksPerMajor: Int
    private var snapAnimationDurationMs: Int
    private var showBaseline: Boolean
    private var showEdges: Boolean
    private var flingEnabled: Boolean

    init {
        val defaultTickColor = ContextCompat.getColor(context, R.color.ruler_marker_small_medium)
        val defaultIndicatorColor = ContextCompat.getColor(context, R.color.ruler_indicator_bar)
        val defaultBackgroundColor = ContextCompat.getColor(context, R.color.ruler_background_panel)
        val defaultEdgeColor = ContextCompat.getColor(context, R.color.ruler_edge_blue)

        val attrsArray = context.obtainStyledAttributes(attrs, R.styleable.HorizontalRuler)
        val startMinValue = attrsArray.getFloat(R.styleable.HorizontalRuler_minValue, minValue)
        val startMaxValue = attrsArray.getFloat(R.styleable.HorizontalRuler_maxValue, maxValue)
        val startInitialValue = attrsArray.getFloat(R.styleable.HorizontalRuler_initialValue, initialValue)

        tickColor = attrsArray.getColor(R.styleable.HorizontalRuler_tickColor, defaultTickColor)
        indicatorColor = attrsArray.getColor(R.styleable.HorizontalRuler_indicatorColor, defaultIndicatorColor)
        rulerBackgroundColor = attrsArray.getColor(R.styleable.HorizontalRuler_rulerBackgroundColor, defaultBackgroundColor)
        edgeColor = attrsArray.getColor(R.styleable.HorizontalRuler_edgeColor, defaultEdgeColor)

        majorTickSpacingPx = attrsArray.getDimension(R.styleable.HorizontalRuler_tickSpacing, dpToPx(38f))
        majorTickHeightPx = attrsArray.getDimension(R.styleable.HorizontalRuler_majorTickHeight, dpToPx(46f))
        mediumTickHeightPx = attrsArray.getDimension(R.styleable.HorizontalRuler_mediumTickHeight, dpToPx(30f))
        minorTickHeightPx = attrsArray.getDimension(R.styleable.HorizontalRuler_minorTickHeight, dpToPx(18f))
        tickStrokeWidthPx = attrsArray.getDimension(R.styleable.HorizontalRuler_tickStrokeWidth, dpToPx(1f))
        indicatorWidthPx = attrsArray.getDimension(R.styleable.HorizontalRuler_indicatorWidth, dpToPx(2f))
        indicatorHeightPx = attrsArray.getDimension(R.styleable.HorizontalRuler_indicatorHeight, dpToPx(46f))
        baselineStrokeWidthPx = attrsArray.getDimension(R.styleable.HorizontalRuler_baselineStrokeWidth, dpToPx(1f))
        edgeStrokeWidthPx = attrsArray.getDimension(R.styleable.HorizontalRuler_edgeStrokeWidth, dpToPx(1.5f))
        baselinePaddingBottomPx = attrsArray.getDimension(R.styleable.HorizontalRuler_baselinePaddingBottom, dpToPx(8f))
        edgeInsetPx = attrsArray.getDimension(R.styleable.HorizontalRuler_edgeInset, dpToPx(8f))
        stepValue = attrsArray.getFloat(R.styleable.HorizontalRuler_valueStep, 1f).coerceAtLeast(0.0001f)
        minorTicksPerMajor = attrsArray.getInt(R.styleable.HorizontalRuler_minorTicksPerMajor, DEFAULT_MINOR_TICKS_PER_MAJOR).coerceAtLeast(1)
        snapAnimationDurationMs = attrsArray.getInt(R.styleable.HorizontalRuler_snapAnimationDurationMs, DEFAULT_SNAP_DURATION_MS).coerceAtLeast(0)
        showBaseline = attrsArray.getBoolean(R.styleable.HorizontalRuler_showBaseline, true)
        showEdges = attrsArray.getBoolean(R.styleable.HorizontalRuler_showEdges, true)
        flingEnabled = attrsArray.getBoolean(R.styleable.HorizontalRuler_flingEnabled, true)
        attrsArray.recycle()

        tickPaint.color = tickColor
        tickPaint.strokeCap = Paint.Cap.ROUND

        indicatorPaint.color = indicatorColor
        indicatorPaint.strokeCap = Paint.Cap.ROUND

        baselinePaint.color = tickColor
        baselinePaint.alpha = 95

        edgePaint.color = edgeColor
        refreshPaintDimensions()
        refreshPaintColors()

        isClickable = true
        reload(startMinValue, startMaxValue, startInitialValue)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = max(
            dpToPx(70f),
            max(majorTickHeightPx, indicatorHeightPx) + baselinePaddingBottomPx * 2f
        ).roundToInt()
        val measuredWidth = resolveSize(suggestedMinimumWidth, widthMeasureSpec)
        val measuredHeight = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val viewWidth = width.toFloat()
        val centerX = viewWidth / 2f
        val baselineY = height / 2f
        val pxPerValue = pxPerValue()
        val minorStepValue = 1f / minorTicksPerMajor

        canvas.drawColor(rulerBackgroundColor)

        val visibleValueHalfRange = centerX / pxPerValue + 2f
        val startValue = currentPositionValue - visibleValueHalfRange
        val endValue = currentPositionValue + visibleValueHalfRange
        val startTickIndex = floor(startValue / minorStepValue).toInt() - 5
        val endTickIndex = ceil(endValue / minorStepValue).toInt() + 5
        var minTickX = Float.MAX_VALUE
        var maxTickX = Float.MIN_VALUE

        for (tickIndex in startTickIndex..endTickIndex) {
            val tickValue = tickIndex * minorStepValue
            if (tickValue < minValue || tickValue > maxValue) continue

            val x = centerX + ((tickValue - currentPositionValue) * pxPerValue)
            if (x < -2f || x > viewWidth + 2f) continue
            if (x < minTickX) minTickX = x
            if (x > maxTickX) maxTickX = x

            val tickHeight = when {
                tickIndex % minorTicksPerMajor == 0 -> majorTickHeightPx
                isMediumTick(tickIndex) -> mediumTickHeightPx
                else -> minorTickHeightPx
            }

            val top = baselineY - (tickHeight / 2f)
            val bottom = baselineY + (tickHeight / 2f)
            canvas.drawLine(x, top, x, bottom, tickPaint)
        }
        if (showBaseline && minTickX != Float.MAX_VALUE && maxTickX != Float.MIN_VALUE) {
            canvas.drawLine(minTickX, baselineY, maxTickX, baselineY, baselinePaint)
        }

        val indicatorTopY = baselineY - (indicatorHeightPx / 2f)
        val indicatorBottomY = baselineY + (indicatorHeightPx / 2f)
        canvas.drawLine(centerX, indicatorTopY, centerX, indicatorBottomY, indicatorPaint)

        if (showEdges) {
            canvas.drawLine(0f, edgeInsetPx, 0f, height - edgeInsetPx, edgePaint)
            val rightEdgeX = viewWidth - edgePaint.strokeWidth
            canvas.drawLine(rightEdgeX, edgeInsetPx, rightEdgeX, height - edgeInsetPx, edgePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                stopAnimations()
                ensureVelocityTracker()
                velocityTracker?.addMovement(event)
                lastTouchX = event.x
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                velocityTracker?.addMovement(event)
                val dx = event.x - lastTouchX
                lastTouchX = event.x
                updateValueByDragDelta(dx)
                return true
            }

            MotionEvent.ACTION_UP -> {
                velocityTracker?.addMovement(event)
                velocityTracker?.computeCurrentVelocity(1000, viewConfig.scaledMaximumFlingVelocity.toFloat())
                val xVelocity = velocityTracker?.xVelocity ?: 0f
                recycleVelocityTracker()
                performClick()

                if (flingEnabled && abs(xVelocity) >= viewConfig.scaledMinimumFlingVelocity) {
                    flingWithVelocity(-xVelocity)
                } else {
                    snapToNearestStep(animated = true)
                }
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                recycleVelocityTracker()
                snapToNearestStep(animated = true)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun computeScroll() {
        super.computeScroll()
        if (overScroller.computeScrollOffset()) {
            val value = scrollXToValue(overScroller.currX.toFloat())
            setCurrentValue(value, notifyListener = true)
            postInvalidateOnAnimation()
            return
        }

        if (isFlinging) {
            isFlinging = false
            snapToNearestStep(animated = true)
            return
        }

        if (isSnapping) {
            isSnapping = false
            val snapped = quantizeToStep(currentPositionValue)
            setCurrentValue(snapped, notifyListener = true)
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimations()
        recycleVelocityTracker()
    }

    override fun onSaveInstanceState(): Parcelable {
        return SavedState(super.onSaveInstanceState()).also {
            it.currentValue = currentPositionValue
            it.minValue = minValue
            it.maxValue = maxValue
            it.stepValue = stepValue
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        minValue = minOf(state.minValue, state.maxValue)
        maxValue = maxOf(state.minValue, state.maxValue)
        stepValue = state.stepValue.coerceAtLeast(0.0001f)
        currentPositionValue = clampValue(state.currentValue)
        initialValue = currentPositionValue
        lastBroadcastValue = Float.NaN
        broadcastIfNeeded()
        invalidate()
    }

    fun scrollToValue(value: Float) {
        scrollToValue(value, animated = false)
    }

    fun scrollToValue(value: Float, animated: Boolean) {
        stopAnimations()
        val snappedTarget = quantizeToStep(clampValue(value))
        if (!animated || snapAnimationDurationMs == 0) {
            setCurrentValue(snappedTarget, notifyListener = true)
            invalidate()
            return
        }

        val startX = valueToScrollX(currentPositionValue).roundToInt()
        val targetX = valueToScrollX(snappedTarget).roundToInt()
        overScroller.startScroll(startX, 0, targetX - startX, 0, snapAnimationDurationMs)
        isFlinging = false
        isSnapping = true
        postInvalidateOnAnimation()
    }

    fun reload(min: Float, max: Float, initValue: Float) {
        setRange(min, max, initValue)
    }

    fun setRange(min: Float, max: Float, initValue: Float = currentPositionValue) {
        if (min <= max) {
            minValue = min
            maxValue = max
        } else {
            minValue = max
            maxValue = min
        }

        initialValue = clampValue(initValue)
        lastBroadcastValue = Float.NaN
        setCurrentValue(initialValue, notifyListener = true)
        invalidate()
    }

    fun setStepValue(step: Float, snapCurrentValue: Boolean = true) {
        stepValue = step.coerceAtLeast(0.0001f)
        if (snapCurrentValue) {
            setCurrentValue(quantizeToStep(currentPositionValue), notifyListener = true)
        }
        invalidate()
    }

    fun setMinorTicksPerMajor(tickCount: Int) {
        minorTicksPerMajor = tickCount.coerceAtLeast(1)
        invalidate()
    }

    fun setTickSpacing(spacingPx: Float) {
        majorTickSpacingPx = spacingPx.coerceAtLeast(dpToPx(8f))
        invalidate()
    }

    fun setTickSpacingPx(spacingPx: Float) {
        setTickSpacing(spacingPx)
    }

    fun setSnapAnimationDuration(durationMs: Int) {
        snapAnimationDurationMs = durationMs.coerceAtLeast(0)
    }

    fun setDrawOptions(drawBaseline: Boolean = showBaseline, drawEdges: Boolean = showEdges) {
        showBaseline = drawBaseline
        showEdges = drawEdges
        invalidate()
    }

    fun setFlingEnabled(enabled: Boolean) {
        flingEnabled = enabled
    }

    fun setOnValueChangeListener(listener: ((Float) -> Unit)?) {
        onValueChangeListener = listener
    }

    fun setColors(
        tickColor: Int = this.tickColor,
        indicatorColor: Int = this.indicatorColor,
        backgroundColor: Int = rulerBackgroundColor,
        edgeColor: Int = this.edgeColor
    ) {
        this.tickColor = tickColor
        this.indicatorColor = indicatorColor
        this.rulerBackgroundColor = backgroundColor
        this.edgeColor = edgeColor
        refreshPaintColors()
        invalidate()
    }

    fun setStrokeWidths(
        tickStrokeWidthPx: Float = this.tickStrokeWidthPx,
        baselineStrokeWidthPx: Float = this.baselineStrokeWidthPx,
        indicatorWidthPx: Float = this.indicatorWidthPx,
        edgeStrokeWidthPx: Float = this.edgeStrokeWidthPx
    ) {
        this.tickStrokeWidthPx = tickStrokeWidthPx.coerceAtLeast(0.1f)
        this.baselineStrokeWidthPx = baselineStrokeWidthPx.coerceAtLeast(0.1f)
        this.indicatorWidthPx = indicatorWidthPx.coerceAtLeast(0.1f)
        this.edgeStrokeWidthPx = edgeStrokeWidthPx.coerceAtLeast(0.1f)
        refreshPaintDimensions()
        invalidate()
    }

    private fun updateValueByDragDelta(dx: Float) {
        if (dx == 0f) return
        val deltaValue = -dx / pxPerValue()
        setCurrentValue(currentPositionValue + deltaValue, notifyListener = true)
        postInvalidateOnAnimation()
    }

    private fun flingWithVelocity(xVelocity: Float) {
        val startX = valueToScrollX(currentPositionValue).roundToInt()
        val minX = valueToScrollX(minValue).roundToInt()
        val maxX = valueToScrollX(maxValue).roundToInt()

        overScroller.fling(startX, 0, xVelocity.roundToInt(), 0, minX, maxX, 0, 0)
        isFlinging = true
        isSnapping = false
        postInvalidateOnAnimation()
    }

    private fun snapToNearestStep(animated: Boolean) {
        val snappedValue = quantizeToStep(currentPositionValue)
        if (abs(snappedValue - currentPositionValue) < 0.001f) {
            setCurrentValue(snappedValue, notifyListener = true)
            invalidate()
            return
        }

        if (!animated) {
            setCurrentValue(snappedValue, notifyListener = true)
            invalidate()
            return
        }

        val startX = valueToScrollX(currentPositionValue).roundToInt()
        val targetX = valueToScrollX(snappedValue).roundToInt()
        overScroller.startScroll(startX, 0, targetX - startX, 0, snapAnimationDurationMs)
        isFlinging = false
        isSnapping = true
        postInvalidateOnAnimation()
    }

    private fun setCurrentValue(value: Float, notifyListener: Boolean) {
        val clamped = clampValue(value)
        if (abs(clamped - currentPositionValue) < 0.0001f) {
            if (notifyListener) broadcastIfNeeded()
            return
        }
        currentPositionValue = clamped
        if (notifyListener) broadcastIfNeeded()
    }

    private fun broadcastIfNeeded() {
        val callbackValue = quantizeToStep(currentPositionValue)
        if (lastBroadcastValue.isNaN() || abs(callbackValue - lastBroadcastValue) >= 0.0001f) {
            lastBroadcastValue = callbackValue
            scrollListener?.onRulerScrolled(callbackValue)
            onValueChangeListener?.invoke(callbackValue)
        }
    }

    private fun clampValue(value: Float): Float = value.coerceIn(minValue, maxValue)

    private fun quantizeToStep(value: Float): Float {
        val safeStep = stepValue.coerceAtLeast(0.0001f)
        val steps = ((value - minValue) / safeStep).roundToInt()
        return clampValue((steps * safeStep) + minValue)
    }

    private fun pxPerValue(): Float = majorTickSpacingPx.coerceAtLeast(dpToPx(8f))

    private fun valueToScrollX(value: Float): Float = (clampValue(value) - minValue) * pxPerValue()

    private fun scrollXToValue(scrollX: Float): Float = minValue + (scrollX / pxPerValue())

    private fun stopAnimations() {
        if (!overScroller.isFinished) {
            overScroller.abortAnimation()
        }
        isFlinging = false
        isSnapping = false
    }

    private fun ensureVelocityTracker() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        } else {
            velocityTracker?.clear()
        }
    }

    private fun recycleVelocityTracker() {
        velocityTracker?.recycle()
        velocityTracker = null
    }

    private fun refreshPaintColors() {
        tickPaint.color = tickColor
        indicatorPaint.color = indicatorColor
        baselinePaint.color = tickColor
        edgePaint.color = edgeColor
    }

    private fun refreshPaintDimensions() {
        tickPaint.strokeWidth = tickStrokeWidthPx
        baselinePaint.strokeWidth = baselineStrokeWidthPx
        indicatorPaint.strokeWidth = indicatorWidthPx
        edgePaint.strokeWidth = edgeStrokeWidthPx
    }

    private fun isMediumTick(tickIndex: Int): Boolean {
        val half = minorTicksPerMajor / 2
        return half > 0 && minorTicksPerMajor % 2 == 0 && tickIndex % half == 0
    }

    private fun dpToPx(dp: Float): Float = dp * resources.displayMetrics.density

    private class SavedState : BaseSavedState {
        var currentValue: Float = 0f
        var minValue: Float = 0f
        var maxValue: Float = 0f
        var stepValue: Float = 1f

        constructor(superState: Parcelable?) : super(superState)

        private constructor(source: Parcel) : super(source) {
            currentValue = source.readFloat()
            minValue = source.readFloat()
            maxValue = source.readFloat()
            stepValue = source.readFloat()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeFloat(currentValue)
            out.writeFloat(minValue)
            out.writeFloat(maxValue)
            out.writeFloat(stepValue)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(source: Parcel): SavedState = SavedState(source)

            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }
}
