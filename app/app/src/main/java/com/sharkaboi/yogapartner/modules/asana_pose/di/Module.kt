package com.sharkaboi.yogapartner.modules.asana_pose.di

import android.content.Context
import com.sharkaboi.yogapartner.ml.PoseClassifier
import com.sharkaboi.yogapartner.ml.PoseClassifierImpl
import com.sharkaboi.yogapartner.modules.asana_pose.repo.AsanaPoseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object Module {

    @Provides
    @ViewModelScoped
    fun providePoseClassifier(
        @ApplicationContext context: Context
    ): PoseClassifier = PoseClassifierImpl(context)

    @Provides
    @ViewModelScoped
    fun provideRepository(
        poseClassifier: PoseClassifier,
        @ApplicationContext context: Context
    ): AsanaPoseRepository = AsanaPoseRepository(poseClassifier, context)
}