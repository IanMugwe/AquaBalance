package com.example.AquaBalance

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

class CircularWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val wavePaint = Paint()
    private val circlePaint = Paint()
    private val textPaint = Paint()
    private val path = Path()
    private var progress = 0f
    private var waveHeight = 20f
    private var waveLength = 1000f
    private var waveSpeed = 500f
    private var startTime: Long = 0
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f

    init {
        wavePaint.style = Paint.Style.FILL
        wavePaint.color = Color.BLUE

        circlePaint.style = Paint.Style.STROKE
        circlePaint.color = Color.BLACK
        circlePaint.strokeWidth = 5f

        textPaint.color = Color.BLACK
        textPaint.textSize = 40f
        textPaint.textAlign = Paint.Align.CENTER

        startTime = System.currentTimeMillis()
    }

    fun setProgress(value: Float) {
        progress = value.coerceIn(0f, 100f)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        radius = min(w, h) / 2f - circlePaint.strokeWidth
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the circular frame
        canvas.drawCircle(centerX, centerY, radius, circlePaint)

        // Calculate wave parameters
        val time = (System.currentTimeMillis() - startTime) / 1000f
        val cycle = 2 * PI * (time * waveSpeed % waveLength) / waveLength

        // Create a circular clip path
        val clipPath = Path()
        clipPath.addCircle(centerX, centerY, radius, Path.Direction.CW)
        canvas.clipPath(clipPath)

        // Draw the wave
        path.reset()
        path.moveTo(centerX - radius, centerY + radius)

        var x = centerX - radius
        while (x <= centerX + radius) {
            val y = centerY + radius - (2 * radius * progress / 100) +
                    sin(x / radius * PI + cycle).toFloat() * waveHeight
            path.lineTo(x, y)
            x += 10
        }

        path.lineTo(centerX + radius, centerY + radius)
        path.close()

        canvas.drawPath(path, wavePaint)

        // Draw the percentage text
        canvas.drawText("${progress.toInt()}%", centerX, centerY, textPaint)

        invalidate()
    }
}