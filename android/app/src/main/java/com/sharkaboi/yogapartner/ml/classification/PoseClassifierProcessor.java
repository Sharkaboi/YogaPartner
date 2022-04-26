

package com.sharkaboi.yogapartner.ml.classification;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Looper;

import androidx.annotation.WorkerThread;

import com.google.common.base.Preconditions;
import com.google.mlkit.vision.pose.Pose;
import com.sharkaboi.yogapartner.R;
import com.sharkaboi.yogapartner.ml.classification.reps.EMASmoothing;
import com.sharkaboi.yogapartner.ml.classification.reps.RepetitionCounter;
import com.sharkaboi.yogapartner.ml.models.ClassificationResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 * Accepts a stream of {@link Pose} for classification and Rep counting.
 */
public class PoseClassifierProcessor {
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
            Timber.d("Error when loading pose samples.\n" + e);
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
            String maxConfidenceClass = classification.getMaxConfidenceClass();
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
