

package com.sharkaboi.yogapartner.ml.classification;

import android.content.Context;
import android.os.Looper;

import androidx.annotation.WorkerThread;

import com.google.common.base.Preconditions;
import com.google.mlkit.vision.pose.Pose;
import com.sharkaboi.yogapartner.R;
import com.sharkaboi.yogapartner.ml.models.ClassificationResult;
import com.sharkaboi.yogapartner.ml.models.TrainedPoseSample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Accepts a stream of {@link Pose} for classification and Rep counting.
 */
public class PoseClassifierProcessor {
    private PoseClassifier poseClassifier;

    @WorkerThread
    public PoseClassifierProcessor(Context context, boolean isStreamMode) {
        Preconditions.checkState(Looper.myLooper() != Looper.getMainLooper());
        loadPoseSamples(context);
    }

    private void loadPoseSamples(Context context) {
        List<TrainedPoseSample> trainedPoseSamples = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.getResources().openRawResource(R.raw.yoga_poses)));
            String csvLine = reader.readLine();
            while (csvLine != null) {
                // If line is not a valid {@link PoseSample}, we'll get null and skip adding to the list.
                TrainedPoseSample trainedPoseSample = TrainedPoseSample.getPoseSample(csvLine, ",");
                if (trainedPoseSample != null) {
                    trainedPoseSamples.add(trainedPoseSample);
                }
                csvLine = reader.readLine();
            }
        } catch (IOException e) {
            Timber.d("Error when loading pose samples.\n" + e);
        }
        poseClassifier = new PoseClassifier(trainedPoseSamples);
    }

    /**
     * Given a new {@link Pose} input, returns a list of formatted {@link String}s with Pose
     * classification results.
     */
    @WorkerThread
    public List<String> getPoseResult(Pose pose) {
        Preconditions.checkState(Looper.myLooper() != Looper.getMainLooper());
        List<String> result = new ArrayList<>();
        ClassificationResult classification = poseClassifier.classify(pose);

        // Add maxConfidence class of current frame to result if pose is found.
        if (!pose.getAllPoseLandmarks().isEmpty()) {
            PoseClass maxConfidenceClass = classification.getMaxConfidenceClass();
            float confidence = classification.getClassConfidence(maxConfidenceClass)
                            / poseClassifier.confidenceRange();
            result.add(maxConfidenceClass.getFormattedString());
        }

        return result;
    }

}
