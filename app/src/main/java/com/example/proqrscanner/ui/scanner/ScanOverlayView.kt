package com.example.proqrscanner.ui.scanner

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.example.proqrscanner.R

class ScanOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.primary)
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val laserPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.laser_color)
        style = Paint.Style.FILL
    }

    private var laserY = 0f
    private var animator: ValueAnimator? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startLaserAnimation()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }

    private fun startLaserAnimation() {
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2000L
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = LinearInterpolator()
            addUpdateListener {
                laserY = height * (it.animatedValue as Float)
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw frame corners
        val radius = 30f
        val cornerLength = 80f
        val path = Path()
        // Top-left
        path.moveTo(0f, radius)
        path.lineTo(0f, 0f)
        path.lineTo(radius, 0f)
        path.moveTo(0f, 0f)
        path.lineTo(0f, cornerLength)
        path.moveTo(0f, 0f)
        path.lineTo(cornerLength, 0f)
        // Top-right
        path.moveTo(width - radius, 0f)
        path.lineTo(width.toFloat(), 0f)
        path.lineTo(width.toFloat(), radius)
        path.moveTo(width.toFloat(), 0f)
        path.lineTo(width.toFloat(), cornerLength)
        path.moveTo(width.toFloat(), 0f)
        path.lineTo(width.toFloat() - cornerLength, 0f)
        // Bottom-left
        path.moveTo(0f, height - radius)
        path.lineTo(0f, height.toFloat())
        path.lineTo(radius, height.toFloat())
        path.moveTo(0f, height.toFloat())
        path.lineTo(0f, height.toFloat() - cornerLength)
        path.moveTo(0f, height.toFloat())
        path.lineTo(cornerLength, height.toFloat())
        // Bottom-right
        path.moveTo(width - radius, height.toFloat())
        path.lineTo(width.toFloat(), height.toFloat())
        path.lineTo(width.toFloat(), height - radius)
        path.moveTo(width.toFloat(), height.toFloat())
        path.lineTo(width.toFloat(), height.toFloat() - cornerLength)
        path.moveTo(width.toFloat(), height.toFloat())
        path.lineTo(width.toFloat() - cornerLength, height.toFloat())

        canvas.drawPath(path, framePaint)

        // Draw laser line
        canvas.drawLine(0f, laserY, width.toFloat(), laserY, laserPaint)
    }
}
