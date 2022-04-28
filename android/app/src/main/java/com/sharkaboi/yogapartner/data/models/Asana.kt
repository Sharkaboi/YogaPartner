package com.sharkaboi.yogapartner.data.models

import android.os.Parcelable
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.parcelize.Parcelize

@Parcelize
data class Asana(
    val id: String,
    val name: String,
    val description: String,
    val difficulty: AsanaDifficulty,
    val asanaType: AsanaType,
    val asanaThumbnail: String,
    val nameSanskrit: String,
    val nameEn: String,
    val bodyParts: List<BodyPart>,
    val instructions: List<String>
) : Parcelable {
    fun getDbValue() = buildMap {
        put("asana_name", name)
        put("asana_description", description)
        put("asana_difficulty", difficulty.getDbValue())
        put("asana_type", asanaType.getDbValue())
        put("asana_thumbnail", asanaThumbnail)
        put("asana_name_sanskrit", nameSanskrit)
        put("asana_name_en", nameEn)
        put("asana_body_parts", bodyParts.getDbValue())
        put("asana_instruction_images", instructions)
    }

    companion object {
        fun getFromDbValue(document: DocumentSnapshot): Asana {
            return Asana(
                id = document.id,
                name = document["asana_name"].toString(),
                description = document["asana_description"].toString(),
                difficulty = AsanaDifficulty.parseFromDb((document["asana_difficulty"] as Long).toInt()),
                asanaType = AsanaType.parseFromDb(document["asana_type"].toString()),
                asanaThumbnail = document["asana_thumbnail"].toString(),
                nameSanskrit = document["asana_name_sanskrit"].toString(),
                nameEn = document["asana_name_en"].toString(),
                bodyParts = BodyPart.parseList(document["asana_body_parts"] as List<String>),
                instructions = document["asana_instruction_images"] as List<String>,
            )
        }

        fun getFromDbList(list: List<DocumentSnapshot>): List<Asana> {
            return list.map { getFromDbValue(it) }
        }
    }
}
