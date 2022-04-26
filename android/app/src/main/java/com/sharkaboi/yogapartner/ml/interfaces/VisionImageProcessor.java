

package com.sharkaboi.yogapartner.ml.interfaces;

import androidx.camera.core.ImageProxy;

import com.google.mlkit.common.MlKitException;
import com.sharkaboi.yogapartner.modules.asana_pose.camera.GraphicOverlay;

/**
 * An interface to process the images with different vision detectors and custom image models.
 */
public interface VisionImageProcessor {

    /**
     * Processes ImageProxy image data, e.g. used for CameraX live preview case.
     */
    void processImageProxy(ImageProxy image, GraphicOverlay graphicOverlay) throws MlKitException;

    /**
     * Stops the underlying machine learning model and release resources.
     */
    void stop();
}
