package com.sharkaboi.yogapartner.data.models

import androidx.annotation.Keep

@Keep
enum class AsanaType {
    UNKNOWN,
    STANDING,
    SITTING,
    FULL_BODY;

    fun getDbValue(): String {
        return when (this) {
            UNKNOWN -> "unknown"
            STANDING -> "standing"
            SITTING -> "sitting"
            FULL_BODY -> "full_body"
        }
    }

    companion object {
        fun parseFromDb(string: String): AsanaType {
            return when (string) {
                "standing" -> STANDING
                "sitting" -> SITTING
                "full_body" -> FULL_BODY
                else -> UNKNOWN
            }
        }
    }
}