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
        val docs = Asana.getFromDbList(result.documents)
        val (imp, notImp) = docs.partition { it.isImp == true }
        val sortedByAlphabetsImp = imp.sortedBy { it.name }
        val sortedByAlphabetsNotImp = notImp.sortedBy { it.name }
        sortedByAlphabetsImp + sortedByAlphabetsNotImp
    }

    companion object {
        private const val ASANA_COLLECTION = "yoga-asanas"
    }
}