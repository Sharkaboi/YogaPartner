package com.sharkaboi.yogapartner

import androidx.camera.core.ExperimentalGetImage
import androidx.multidex.MultiDexApplication
import com.sharkaboi.yogapartner.ml.config.DetectorOptions
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
@ExperimentalGetImage
class YogaPartner : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(object : Timber.DebugTree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                super.log(priority, "shark_log_$tag", message, t)
            }
        })
        DetectorOptions.init(this)
    }
}