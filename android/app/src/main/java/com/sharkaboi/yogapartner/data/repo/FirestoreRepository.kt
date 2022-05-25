package com.sharkaboi.yogapartner.data.repo

import com.google.firebase.firestore.FirebaseFirestore
import com.sharkaboi.yogapartner.data.models.Asana
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.withContext

class FirestoreRepository(
    private val db: FirebaseFirestore
) {

    suspend fun getAsanas(): List<Asana> = withContext(Dispatchers.IO) {
        val deferred = db.collection(ASANA_COLLECTION)
            .get()
            .asDeferred()
        val result = deferred.await()
//        Timber.d(result.documents.toString())
        Asana.getFromDbList(result.documents).sortedBy { it.name }
    }

    companion object {
        private const val ASANA_COLLECTION = "yoga-asanas"
    }
}