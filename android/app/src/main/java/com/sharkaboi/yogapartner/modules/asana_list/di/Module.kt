package com.sharkaboi.yogapartner.modules.asana_list.di

import com.sharkaboi.yogapartner.data.repo.FirestoreRepository
import com.sharkaboi.yogapartner.modules.asana_list.repo.AsanaListRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object Module {

    @Provides
    @ViewModelScoped
    fun getRepo(firestoreRepository: FirestoreRepository) = AsanaListRepository(firestoreRepository)
}