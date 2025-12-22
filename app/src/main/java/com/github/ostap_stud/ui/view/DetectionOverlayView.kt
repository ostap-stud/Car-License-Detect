package com.github.ostap_stud.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.github.ostap_stud.ui.analysis.Detection
import com.github.ostap_stud.ui.analysis.LicenseDetection
import java.util.Locale

class DetectionOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var scale: Float = 1f
    private var postScaleWidthOffset: Float = 0f
    private var postScaleHeightOffset: Float = 0f

    private var isScalingUpdated: Boolean = false

    private var carDetections: List<Detection> = emptyList()
    private var plateDetections: List<LicenseDetection> = emptyList()

    private val objPaint: Paint = Paint().apply {
        strokeWidth = STROKE_WIDTH
    }

    private val textPaint: Paint = Paint().apply {
        textSize = TEXT_SIZE
        color = TEXT_COLOR
    }

    private val textRect: Rect = Rect()

    fun setAndInvalidate(
        carDetections: List<Detection>,
        plateDetections: List<LicenseDetection>,
        inputImageWidth: Float,
        inputImageHeight: Float
    ){
        this.carDetections = carDetections
        this.plateDetections = plateDetections
        updateScaling(inputImageWidth, inputImageHeight)
        invalidate()
    }

    private fun translateX(x: Float): Float{
        return x * scale - postScaleWidthOffset
    }

    private fun translateY(y: Float): Float{
        return y * scale - postScaleHeightOffset
    }

    private fun updateScaling(inputImageWidth: Float, inputImageHeight: Float){
        if (!isScalingUpdated){
            val inputAspectRatio = inputImageWidth / inputImageHeight
            val overlayAspectRatio = width.toFloat() / height
            if (overlayAspectRatio > inputAspectRatio){
                scale = width / inputImageWidth
                postScaleHeightOffset = (width / inputAspectRatio - height) / 2
            } else{
                scale = height / inputImageHeight
                postScaleWidthOffset = (height * inputAspectRatio - width) / 2
            }
            isScalingUpdated = true
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val desiredHeight = suggestedMinimumHeight + paddingTop + paddingBottom

        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        carDetections.forEach { det ->
            objPaint.color = CAR_COLOR
            objPaint.style = Paint.Style.STROKE

            val left = translateX(det.x1)
            val top = translateY(det.y1)
            val right = translateX(det.x2)
            val bottom = translateY(det.y2)

            canvas.drawRect(
                left, top, right, bottom, objPaint
            )

            val textToDraw = String.format(Locale.getDefault(), CAR_LABEL_FORMAT, det.cls, det.score)
            textPaint.getTextBounds(textToDraw, 0, textToDraw.length, textRect)
            val textWidth = textRect.width()
            val textHeight = textRect.height()

            objPaint.style = Paint.Style.FILL
            canvas.drawRect(left, top, left + textWidth + TEXT_RECT_PADDING, top + textHeight + TEXT_RECT_PADDING, objPaint)
            canvas.drawText(textToDraw, left, top + textHeight, textPaint)
        }

        plateDetections.forEach { det ->
            objPaint.color = PLATE_COLOR
            objPaint.style = Paint.Style.STROKE

            val left = translateX(det.x1)
            val top = translateY(det.y1)
            val right = translateX(det.x2)
            val bottom = translateY(det.y2)

            canvas.drawRect(
                left, top, right, bottom, objPaint
            )

            val textToDraw = String.format(Locale.getDefault(), LICENSE_LABEL_FORMAT, det.cls, det.numberText, det.score)
            textPaint.getTextBounds(textToDraw, 0, textToDraw.length, textRect)
            val textWidth = textRect.width()
            val textHeight = textRect.height()

            objPaint.style = Paint.Style.FILL
            canvas.drawRect(left, top, left + textWidth + TEXT_RECT_PADDING, top + textHeight + TEXT_RECT_PADDING, objPaint)
            canvas.drawText(textToDraw, left, top + textHeight, textPaint)
        }
    }

    companion object{
        private val TEXT_SIZE = 28f
        private val TEXT_RECT_PADDING = 5f
        private val STROKE_WIDTH = 5f
        private val CAR_LABEL_FORMAT = "%s: %.2f%% conf"
        private val LICENSE_LABEL_FORMAT = "%s: %s\n%.2f%% conf"
        private val CAR_COLOR = Color.BLUE
        private val PLATE_COLOR = Color.GREEN
        private val TEXT_COLOR = Color.WHITE
    }
}