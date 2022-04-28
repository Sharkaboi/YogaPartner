package com.sharkaboi.yogapartner.data.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sharkaboi.yogapartner.data.repo.FirestoreRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Module {

    @Provides
    @Singleton
    fun getDb(): FirebaseFirestore = Firebase.firestore

    @Provides
    @Singleton
    fun getRepo(db: FirebaseFirestore) = FirestoreRepository(db)
}