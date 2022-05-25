package com.sharkaboi.yogapartner.data.models

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class Asana(
    val id: String,
    val name: String,
    val description: String,
    val difficulty: AsanaDifficulty,
    val asanaThumbnail: String,
    val isImp: Boolean?
) : Parcelable {
    companion object {
        private fun getFromDbValue(document: DocumentSnapshot): Asana {
            return Asana(
                id = document.id,
                name = document["asana_name"].toString(),
                description = document["asana_description"].toString(),
                difficulty = AsanaDifficulty.parseFromDb((document["asana_difficulty"] as Long).toInt()),
                asanaThumbnail = document["asana_thumbnail"].toString(),
                isImp = document["imp"] as Boolean?,
            )
        }

        fun getFromDbList(list: List<DocumentSnapshot>): List<Asana> {
            return list.map { getFromDbValue(it) }
        }
    }
}
