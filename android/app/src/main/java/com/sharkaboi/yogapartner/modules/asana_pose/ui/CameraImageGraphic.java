

package com.sharkaboi.yogapartner.modules.asana_pose.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.sharkaboi.yogapartner.modules.asana_pose.camera.GraphicOverlay;
import com.sharkaboi.yogapartner.modules.asana_pose.camera.GraphicOverlay.Graphic;

/** Draw camera image to background. */
public class CameraImageGraphic extends Graphic {

  private final Bitmap bitmap;

  public CameraImageGraphic(GraphicOverlay overlay, Bitmap bitmap) {
    super(overlay);
    this.bitmap = bitmap;
  }

  @Override
  public void draw(Canvas canvas) {
    canvas.drawBitmap(bitmap, getTransformationMatrix(), null);
  }
}
