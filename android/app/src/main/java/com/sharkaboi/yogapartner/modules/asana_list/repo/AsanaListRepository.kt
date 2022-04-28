package com.sharkaboi.yogapartner.modules.asana_list.repo

import com.sharkaboi.yogapartner.data.DataState
import com.sharkaboi.yogapartner.data.getDataState
import com.sharkaboi.yogapartner.data.models.Asana
import com.sharkaboi.yogapartner.data.repo.FirestoreRepository

class AsanaListRepository(
    private val firestoreRepository: FirestoreRepository
) {

    suspend fun getAsanas(): DataState<List<Asana>> {
        return getDataState {
            firestoreRepository.getAsanas()
        }
    }
}