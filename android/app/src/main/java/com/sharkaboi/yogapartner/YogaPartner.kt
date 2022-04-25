package com.sharkaboi.yogapartner

import androidx.multidex.MultiDexApplication
import timber.log.Timber

class YogaPartner : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(object : Timber.DebugTree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                super.log(priority, "shark_log_$tag", message, t)
            }
        })
    }
}