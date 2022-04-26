package com.sharkaboi.yogapartner.modules.asana_pose.ui.custom

import android.graphics.Bitmap
import android.graphics.Canvas
import com.sharkaboi.yogapartner.modules.asana_pose.ui.custom.GraphicOverlay.Graphic

/** Draw camera image to background.  */
class CameraPreviewBitmapGraphic(
    overlay: GraphicOverlay,
    private val bitmap: Bitmap
) : Graphic(overlay) {

    override fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, transformationMatrix, null)
    }
}
