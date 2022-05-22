package com.sharkaboi.yogapartner.test

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.android.odml.image.BitmapMlImageBuilder
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.sharkaboi.yogapartner.R
import com.sharkaboi.yogapartner.common.extensions.await
import com.sharkaboi.yogapartner.ml.classification.AsanaClass
import com.sharkaboi.yogapartner.ml.config.DetectorOptions
import com.sharkaboi.yogapartner.ml.models.TrainedPoseSample
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.IOException

class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        findViewById<Button>(R.id.btnTest).setOnClickListener {
            runTest()
        }
    }

    private fun runTest() {
        lifecycleScope.launch {
            val btn = findViewById<Button>(R.id.btnTest)
            btn?.text = "Running"
            btn?.isEnabled = false
            initPoseSamples()
            val testImages: List<Pair<AsanaClass, File>> = getTestData()
            runClassifierOnTestingDataOnAccurateDetectorWithGpu(testImages)
//            runClassifierOnTestingDataOnAccurateDetectorWithoutGpu(testImages)
//            runClassifierOnTestingDataOnFastDetectorWithGpu(testImages)
//            runClassifierOnTestingDataOnFastDetectorWithoutGpu(testImages)
            btn?.isEnabled = true
            btn?.text = "Run test"
        }
    }

    private suspend fun runClassifierOnTestingDataOnAccurateDetectorWithGpu(testImages: List<Pair<AsanaClass, File>>) {
        val builder: AccuratePoseDetectorOptions.Builder =
            AccuratePoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        builder.setPreferredHardwareConfigs(PoseDetectorOptions.CPU_GPU)
        val detector = PoseDetection.getClient(builder.build())
        getClassificationWithDetector(
            "runClassifierOnTestingDataOnAccurateDetectorWithGpu",
            detector,
            testImages
        )
    }

    private suspend fun runClassifierOnTestingDataOnAccurateDetectorWithoutGpu(testImages: List<Pair<AsanaClass, File>>) {
        val builder: AccuratePoseDetectorOptions.Builder =
            AccuratePoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        builder.setPreferredHardwareConfigs(PoseDetectorOptions.CPU)
        val detector = PoseDetection.getClient(builder.build())
        getClassificationWithDetector(
            "runClassifierOnTestingDataOnAccurateDetectorWithoutGpu",
            detector,
            testImages
        )
    }

    private suspend fun runClassifierOnTestingDataOnFastDetectorWithGpu(testImages: List<Pair<AsanaClass, File>>) {
        val builder =
            PoseDetectorOptions.Builder().setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        builder.setPreferredHardwareConfigs(PoseDetectorOptions.CPU_GPU)
        val detector = PoseDetection.getClient(builder.build())
        getClassificationWithDetector(
            "runClassifierOnTestingDataOnFastDetectorWithGpu",
            detector,
            testImages
        )
    }

    private suspend fun runClassifierOnTestingDataOnFastDetectorWithoutGpu(testImages: List<Pair<AsanaClass, File>>) {
        val builder =
            PoseDetectorOptions.Builder().setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        builder.setPreferredHardwareConfigs(PoseDetectorOptions.CPU)
        val detector = PoseDetection.getClient(builder.build())
        getClassificationWithDetector(
            "runClassifierOnTestingDataOnFastDetectorWithoutGpu",
            detector,
            testImages
        )
    }

    private val testSampleSizeRatio = 20f / 100f
    private lateinit var poseSamples: List<TrainedPoseSample>

    private fun initPoseSamples() {
        val trainedPoseSamples: MutableList<TrainedPoseSample> = ArrayList()
        try {
            val downloadFolder =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadFolder!!.absolutePath + File.separator + "data_out-v1.csv")
            val reader = file.bufferedReader()
            var csvLine = reader.readLine()
            while (csvLine != null) {
                Timber.d("initPoseSamples $csvLine")
                val trainedPoseSample = TrainedPoseSample.getPoseSample(csvLine, ",")
                if (trainedPoseSample != null) {
                    trainedPoseSamples.add(trainedPoseSample)
                }
                csvLine = reader.readLine()
            }
        } catch (e: IOException) {
            Timber.d("Error when loading pose samples.\n$e")
        }
        poseSamples = trainedPoseSamples
    }

    private fun getTestData(): List<Pair<AsanaClass, File>> {
        Timber.d("getTestData")
        val testingData = mutableListOf<Pair<AsanaClass, File>>()
        val downloadFolder =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val testFolder = File(downloadFolder!!.absolutePath + File.separator + "v1")
        testFolder.listFiles()?.forEach { classFolder ->
            Timber.d("getTestData class : ${classFolder.name} ")
            val label = AsanaClass.valueOf(classFolder.name)
            val images = classFolder.listFiles().orEmpty()
            val sampleCount = images.count()
            val takeCount = (sampleCount * testSampleSizeRatio).toInt()
            val testList = images.take(takeCount).map { Pair(label, it) }
            testingData.addAll(testList)
        }
        return testingData
    }

    private suspend fun runDetection(
        detector: PoseDetector,
        image: Pair<AsanaClass, File>
    ): Pose {
        val bitmap = BitmapFactory.decodeFile(image.second.absolutePath)
        val mlImage = BitmapMlImageBuilder(bitmap).build()
        Timber.d("Process landmark of ${image.second.name}")
        val pose = detector.process(mlImage).await()
        Timber.d("Processed landmark of ${image.second.name}")
        return pose
    }

    private fun getClassification(landmarks: Pose): AsanaClass {
        val classifier = DetectorOptions.getInstance().getClassifier(poseSamples)
        Timber.d("getClassifications")
        return classifier.classify(landmarks).getMaxConfidenceClass()
    }

    private var totalCount = 0
    private var correctCount = 0
    private suspend fun getClassificationWithDetector(
        which: String,
        detector: PoseDetector,
        testImages: List<Pair<AsanaClass, File>>
    ) {
        Timber.d("Testing $which")
        totalCount = testImages.count()
        correctCount = 0
        testImages.forEach {
            val landmarks = runDetection(detector, it)
            val output = getClassification(landmarks)
            log(it.first, output)
        }
        logAccuracy(which)
    }

    private fun log(expected: AsanaClass, output: AsanaClass) {
        Timber.d("$expected expected - output $output")
        if (expected == output) {
            correctCount++
        }
    }

    private fun logAccuracy(
        which: String
    ) {
        if (totalCount == 0) {
            Timber.d("$which expected count 0")
            return
        }
        Timber.d("$which Accuracy : ${correctCount * 100 / totalCount}")
    }
}