package com.netherpyro.speedometer.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import com.netherpyro.speedometer.R
import com.netherpyro.speedometer.dpToPx
import com.netherpyro.speedometer.spToPx
import java.util.Locale
import kotlin.math.min

/**
 * @author mmikhailov on 23.05.2020.
 */
class SpeedometerView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    @ColorInt
    private var rimColor: Int = Color.DKGRAY

    @ColorInt
    private var centerCircleColor: Int = Color.DKGRAY

    @ColorInt
    private var mainCircleColor: Int = Color.BLACK

    @ColorInt
    private var markColor = Color.WHITE

    @ColorInt
    private var textValueColor = Color.WHITE

    @ColorInt
    private var needleColor = Color.WHITE

    private val locale = Locale.getDefault()

    private val defMaxValue = 220.0
    private val defDivValue = 20.0
    private val defTextSize = 16f

    private var divValue = defDivValue
    private var labelText = "km/h"
    private var drawIntermediateMarks = true
    private var divideValueByDivision = false
    private var valueTextSize = context.spToPx(defTextSize)
    private var labelTextSize = context.spToPx(defTextSize)

    private val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val markPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val centerCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val valueTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val rimPath = Path()
    private val markPath = Path()
    private val needlePath = Path()

    private val arcRectF = RectF()

    private val centerCircleRadius = context.dpToPx(24f)
    private val rimWidth = context.dpToPx(8f)
    private val markHeight = context.dpToPx(12f)
    private val needleWidth = context.dpToPx(8f)
    private val rimWidthHalf = rimWidth / 2f
    private val startDeg = 135f
    private val sweepDeg = 270f
    private val endDeg = startDeg + sweepDeg

    private var markDegSpan = 0f
    private var side = 0
    private var centerX = 0f
    private var centerY = 0f

    var maxValue = defMaxValue
        set(value) {
            field = if (value > 0) value else defMaxValue
            invalidateMarkDegreeSpan()
            invalidate()
        }

    var currentValue: Double = 0.0
        set(value) {
            field = value
            invalidate()
        }

    init {
        attrs?.let {
            val typedArray = context.theme.obtainStyledAttributes(
                    attrs,
                    R.styleable.SpeedometerView,
                    0,
                    0
            )

            rimColor = typedArray.getColor(R.styleable.SpeedometerView_rimColor,
                    rimColor)
            centerCircleColor = typedArray.getColor(R.styleable.SpeedometerView_centerCircleColor,
                    centerCircleColor)
            mainCircleColor = typedArray.getColor(R.styleable.SpeedometerView_mainCircleColor,
                    mainCircleColor)
            markColor = typedArray.getColor(R.styleable.SpeedometerView_markColor,
                    markColor)
            textValueColor = typedArray.getColor(R.styleable.SpeedometerView_valueColor,
                    textValueColor)
            needleColor = typedArray.getColor(R.styleable.SpeedometerView_needleColor,
                    needleColor)
            labelText = typedArray.getString(R.styleable.SpeedometerView_label) ?: labelText
            divValue = typedArray.getInt(R.styleable.SpeedometerView_mark_division_value, defDivValue.toInt())
                .toDouble()
            divideValueByDivision = typedArray.getBoolean(R.styleable.SpeedometerView_divide_value_by_division,
                    divideValueByDivision)
            drawIntermediateMarks = typedArray.getBoolean(R.styleable.SpeedometerView_draw_intermediate_mark,
                    drawIntermediateMarks)

            valueTextSize = typedArray.getDimension(R.styleable.SpeedometerView_value_text_size, context.spToPx(defTextSize))
            labelTextSize = typedArray.getDimension(R.styleable.SpeedometerView_label_text_size, context.spToPx(defTextSize))

            typedArray.recycle()
        }

        rimPaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = rimWidth
            color = rimColor
        }

        markPaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = markHeight / 3f
            color = markColor
        }

        needlePaint.apply {
            style = Paint.Style.FILL
            color = needleColor
        }

        centerCirclePaint.apply {
            style = Paint.Style.FILL
            color = centerCircleColor
        }

        valueTextPaint.apply {
            color = textValueColor
            textSize = valueTextSize
            textAlign = Paint.Align.CENTER
        }

        labelTextPaint.apply {
            color = textValueColor
            textSize = labelTextSize
            textAlign = Paint.Align.CENTER
        }

        backgroundPaint.apply {
            style = Paint.Style.FILL
            color = mainCircleColor
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        side = min(measuredWidth, measuredHeight)
        centerX = side / 2f
        centerY = side / 2f

        arcRectF.set(
                rimWidthHalf + paddingLeft,
                rimWidthHalf + paddingTop,
                side - rimWidthHalf - paddingRight,
                side - rimWidthHalf - paddingBottom
        )
        rimPath.reset()
        rimPath.addArc(arcRectF, startDeg, sweepDeg)

        invalidateMarkDegreeSpan()

        markPath.reset()
        markPath.moveTo(centerX, paddingTop.toFloat())
        markPath.lineTo(centerX, paddingTop + markHeight)

        needlePath.reset()
        needlePath.moveTo(centerX, paddingTop.toFloat() + rimWidth / 2f)
        val needleBottomY = centerY + centerCircleRadius + context.dpToPx(8f)
        val needleTriangleBottomY = paddingTop.toFloat() + rimWidth * 1.1f
        needlePath.lineTo(centerX - needleWidth / 2f, needleTriangleBottomY)
        needlePath.lineTo(centerX - needleWidth / 2f, needleBottomY)
        needlePath.lineTo(centerX + needleWidth / 2f, needleBottomY)
        needlePath.lineTo(centerX + needleWidth / 2f, needleTriangleBottomY)
        needlePath.close()
    }

    private fun invalidateMarkDegreeSpan() {
        markDegSpan = if (drawIntermediateMarks) (divValue / 2.0).asDegree() else divValue.asDegree()
    }

    override fun onDraw(canvas: Canvas) {
        if (height > side) {
            canvas.translate(0f, (height - side) / 2f)
        }

        if (width > side) {
            canvas.translate((width - side) / 2f, 0f)
        }

        // draw static
        canvas.drawCircle(centerX, centerY, arcRectF.height() / 2f, backgroundPaint)
        canvas.drawPath(rimPath, rimPaint)
        canvas.drawCircle(centerX, centerY, centerCircleRadius, centerCirclePaint)
        canvas.drawText(labelText, centerX, side - centerY / 3f, labelTextPaint)

        // draw marks
        canvas.save()
        canvas.rotate(90f + startDeg, centerX, centerY)

        val textY = paddingTop + markHeight * 3f

        var i = startDeg
        var drawText = true
        while (i <= endDeg) {
            canvas.drawPath(markPath, markPaint)

            if (drawText) {
                canvas.rotate(-90f - i, centerX, textY)
                canvas.drawText(i.asValueFormatted(), centerX, textY, valueTextPaint)
                canvas.rotate(90f + i, centerX, textY)
            }

            canvas.rotate(markDegSpan, centerX, centerY)
            i += markDegSpan
            if (drawIntermediateMarks) {
                drawText = !drawText
            }
        }
        canvas.restore()

        // draw needle
        canvas.save()
        canvas.rotate(90f + startDeg + currentValue.asDegree(), centerX, centerY)
        canvas.drawPath(needlePath, needlePaint)
        canvas.restore()
    }

    private fun Double.asDegree() = (this / maxValue * (endDeg - startDeg)).toFloat()

    private fun Float.asValue(): Double = (this - startDeg) / (endDeg - startDeg) * maxValue

    private fun Float.asValueFormatted(): String =
            "%.0f".format(locale, if (divideValueByDivision) this.asValue() / divValue else this.asValue())
}