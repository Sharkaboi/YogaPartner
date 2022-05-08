package com.sharkaboi.yogapartner.modules.asana_pose.ui.custom

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.sharkaboi.yogapartner.ml.config.DetectorOptions
import com.sharkaboi.yogapartner.modules.asana_pose.ui.custom.GraphicOverlay.Graphic

/** Graphic instance for rendering inference info (latency, FPS, resolution) in an overlay view.  */
class FpsInfoGraphic(
    private val overlay: GraphicOverlay,
    private val frameLatency: Long,
    private val detectorLatency: Long,
    // Only valid when a stream of input images is being processed. Null for single image mode.
    private val framesPerSecond: Int?
) : Graphic(overlay) {
    private val textPaint: Paint = Paint()
    private var showLatencyInfo = DetectorOptions.getInstance().shouldShowLatencyInfo()

    @Synchronized
    override fun draw(canvas: Canvas) {
        val x = TEXT_SIZE * 0.5f
        val y = TEXT_SIZE * 1.5f
        if (DetectorOptions.getInstance().shouldShowInputImageSize()) {
            canvas.drawText(
                "InputImage size: " + overlay.imageHeight + "x" + overlay.imageWidth,
                x,
                y,
                textPaint
            )
        }
        if (!showLatencyInfo) {
            return
        }

        // Draw FPS (if valid) and inference latency
        if (framesPerSecond != null) {
            canvas.drawText(
                "FPS: $framesPerSecond, Frame latency: $frameLatency ms",
                x,
                y + TEXT_SIZE,
                textPaint
            )
        } else {
            canvas.drawText("Frame latency: $frameLatency ms", x, y + TEXT_SIZE, textPaint)
        }
        canvas.drawText(
            "Detector latency: $detectorLatency ms", x, y + TEXT_SIZE * 2, textPaint
        )
    }

    companion object {
        private const val TEXT_COLOR = Color.WHITE
        private const val TEXT_SIZE = 60.0f
    }

    init {
        textPaint.color = TEXT_COLOR
        textPaint.textSize = TEXT_SIZE
        textPaint.setShadowLayer(5.0f, 0f, 0f, Color.BLACK)
        postInvalidate()
    }
}
