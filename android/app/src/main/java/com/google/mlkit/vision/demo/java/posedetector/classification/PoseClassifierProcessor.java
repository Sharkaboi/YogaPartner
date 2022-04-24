/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.mlkit.vision.demo.java.posedetector.classification;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.WorkerThread;
import com.google.common.base.Preconditions;
import com.google.mlkit.vision.demo.R;
import com.google.mlkit.vision.pose.Pose;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Accepts a stream of {@link Pose} for classification and Rep counting.
 */
public class PoseClassifierProcessor {
  private static final String TAG = "PoseClassifierProcessor";
  private static final String POSE_SAMPLES_FILE = "pose/yoga_poses.csv";

  // Specify classes for which we want rep counting.
  // These are the labels in the given {@code POSE_SAMPLES_FILE}. You can set your own class labels
  // for your pose samples.
  private static final String PUSHUPS_CLASS = "pushups_down";
  private static final String SQUATS_CLASS = "squats_down";
  private static final String[] POSE_CLASSES = {
          "adho_mukha_svanasana",
          "adho_mukha_vriksasana",
          "agnistambhasana",
          "ananda_balasana",
          "anantasana",
          "anjaneyasana",
          "ardha_bhekasana",
          "ardha_chandrasana",
          "ardha_matsyendrasana",
          "ardha_pincha_mayurasana",
          "ardha_uttanasana",
          "ashtanga_namaskara",
          "astavakrasana",
          "baddha_konasana",
          "bakasana",
          "balasana",
          "bhairavasana",
          "bharadvajasana_i",
          "bhekasana",
          "bhujangasana",
          "bhujapidasana",
          "bitilasana",
          "camatkarasana",
          "chakravakasana",
          "chaturanga_dandasana",
          "dandasana",
          "dhanurasana",
          "durvasasana",
          "dwi_pada_viparita_dandasana",
          "eka_pada_koundinyanasana_i",
          "eka_pada_koundinyanasana_ii",
          "eka_pada_rajakapotasana",
          "eka_pada_rajakapotasana_ii",
          "ganda_bherundasana",
          "garbha_pindasana",
          "garudasana",
          "gomukhasana",
          "halasana",
          "hanumanasana",
          "janu_sirsasana",
          "kapotasana",
          "krounchasana",
          "kurmasana",
          "lolasana",
          "makarasana",
          "makara_adho_mukha_svanasana",
          "malasana",
          "marichyasana_i",
          "marichyasana_iii",
          "marjaryasana",
          "matsyasana",
          "mayurasana",
          "natarajasana",
          "padangusthasana",
          "padmasana",
          "parighasana",
          "paripurna_navasana",
          "parivrtta_janu_sirsasana",
          "parivrtta_parsvakonasana",
          "parivrtta_trikonasana",
          "parsva_bakasana",
          "parsvottanasana",
          "pasasana",
          "paschimottanasana",
          "phalakasana",
          "pincha_mayurasana",
          "prasarita_padottanasana",
          "purvottanasana",
          "salabhasana",
          "salamba_bhujangasana",
          "salamba_sarvangasana",
          "salamba_sirsasana",
          "savasana",
          "setu_bandha_sarvangasana",
          "simhasana",
          "sukhasana",
          "supta_baddha_konasana",
          "supta_matsyendrasana",
          "supta_padangusthasana",
          "supta_virasana",
          "tadasana",
          "tittibhasana",
          "tolasana",
          "tulasana",
          "upavistha_konasana",
          "urdhva_dhanurasana",
          "urdhva_hastasana",
          "urdhva_mukha_svanasana",
          "urdhva_prasarita_eka_padasana",
          "ustrasana",
          "utkatasana",
          "uttanasana",
          "uttana_shishosana",
          "utthita_ashwa_sanchalanasana",
          "utthita_hasta_padangustasana",
          "utthita_parsvakonasana",
          "utthita_trikonasana",
          "vajrasana",
          "vasisthasana",
          "viparita_karani",
          "virabhadrasana_i",
          "virabhadrasana_ii",
          "virabhadrasana_iii",
          "virasana",
          "vriksasana",
          "vrischikasana",
          "yoganidrasana"
  };

  private final boolean isStreamMode;

  private EMASmoothing emaSmoothing;
  private List<RepetitionCounter> repCounters;
  private PoseClassifier poseClassifier;
  private String lastRepResult;

  @WorkerThread
  public PoseClassifierProcessor(Context context, boolean isStreamMode) {
    Preconditions.checkState(Looper.myLooper() != Looper.getMainLooper());
    this.isStreamMode = isStreamMode;
    if (isStreamMode) {
      emaSmoothing = new EMASmoothing();
      repCounters = new ArrayList<>();
      lastRepResult = "";
    }
    loadPoseSamples(context);
  }

  private void loadPoseSamples(Context context) {
    List<PoseSample> poseSamples = new ArrayList<>();
    try {
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(context.getResources().openRawResource(R.raw.yoga_poses)));
      String csvLine = reader.readLine();
      while (csvLine != null) {
        // If line is not a valid {@link PoseSample}, we'll get null and skip adding to the list.
        PoseSample poseSample = PoseSample.getPoseSample(csvLine, ",");
        if (poseSample != null) {
          poseSamples.add(poseSample);
        }
        csvLine = reader.readLine();
      }
    } catch (IOException e) {
      Log.e(TAG, "Error when loading pose samples.\n" + e);
    }
    poseClassifier = new PoseClassifier(poseSamples);
    if (isStreamMode) {
      for (String className : POSE_CLASSES) {
        repCounters.add(new RepetitionCounter(className));
      }
    }
  }

  /**
   * Given a new {@link Pose} input, returns a list of formatted {@link String}s with Pose
   * classification results.
   *
   * <p>Currently it returns up to 2 strings as following:
   * 0: PoseClass : X reps
   * 1: PoseClass : [0.0-1.0] confidence
   */
  @WorkerThread
  public List<String> getPoseResult(Pose pose) {
    Preconditions.checkState(Looper.myLooper() != Looper.getMainLooper());
    List<String> result = new ArrayList<>();
    ClassificationResult classification = poseClassifier.classify(pose);

    // Update {@link RepetitionCounter}s if {@code isStreamMode}.
    if (isStreamMode) {
      // Feed pose to smoothing even if no pose found.
      classification = emaSmoothing.getSmoothedResult(classification);

      // Return early without updating repCounter if no pose found.
      if (pose.getAllPoseLandmarks().isEmpty()) {
        result.add(lastRepResult);
        return result;
      }

      for (RepetitionCounter repCounter : repCounters) {
        int repsBefore = repCounter.getNumRepeats();
        int repsAfter = repCounter.addClassificationResult(classification);
        if (repsAfter > repsBefore) {
          // Play a fun beep when rep counter updates.
          ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
          tg.startTone(ToneGenerator.TONE_PROP_BEEP);
          lastRepResult = String.format(
              Locale.US, "%s : %d reps", repCounter.getClassName(), repsAfter);
          break;
        }
      }
      result.add(lastRepResult);
    }

    // Add maxConfidence class of current frame to result if pose is found.
    if (!pose.getAllPoseLandmarks().isEmpty()) {
      if(classification.getAllClasses().size() != 0)
        Log.d("Sharkaboi", "Classes: " + classification.getAllClasses().toString());
      String maxConfidenceClass = classification.getMaxConfidenceClass();
      if(!maxConfidenceClass.equals(""))
        Log.d("Sharkaboi", "maxConfidenceClass: " + maxConfidenceClass);
      String maxConfidenceClassResult = String.format(
          Locale.US,
          "%s : %.2f confidence",
          maxConfidenceClass,
          classification.getClassConfidence(maxConfidenceClass)
              / poseClassifier.confidenceRange());
      result.add(maxConfidenceClassResult);
    }

    return result;
  }

}
