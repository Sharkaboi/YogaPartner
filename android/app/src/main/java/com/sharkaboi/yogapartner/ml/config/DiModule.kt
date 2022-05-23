package com.sharkaboi.yogapartner.ml.config

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DiModule {

    @Provides
    @Singleton
    fun getDetectorOptions(
        @ApplicationContext context: Context,
        sharedPreferences: SharedPreferences
    ) = DetectorOptions(context, sharedPreferences)
}