package com.sharkaboi.yogapartner.modules.asana_pose.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.QUEUE_FLUSH
import com.sharkaboi.yogapartner.ml.classification.AsanaClass
import java.util.*

class TTSSpeechManager(context: Context) {
    private var lastSpokenAsana: AsanaClass? = null
    private val tts = TextToSpeech(context.applicationContext) {}.apply {
        language = Locale.US
    }

    fun speakAsana(asanaClass: AsanaClass) {
        if (asanaClass == AsanaClass.UNKNOWN) {
            return
        }

        if (lastSpokenAsana == asanaClass) {
            return
        }

        lastSpokenAsana = asanaClass

        val textToBeSpoken = buildString {
            append("You are doing ")
            append(asanaClass.getFormattedString())
        }
        tts.speak(textToBeSpoken, QUEUE_FLUSH, null, System.currentTimeMillis().toString())
    }

    fun stop() {
        tts.stop()
    }

    fun shutdown() {
        tts.shutdown()
    }
}