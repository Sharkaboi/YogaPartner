package com.sharkaboi.yogapartner.data.models

import androidx.annotation.Keep

@Keep
enum class AsanaDifficulty {
    UNKNOWN,
    EASY,
    MEDIUM,
    DIFFICULT;

    companion object {
        fun parseFromDb(index: Int): AsanaDifficulty {
            return when (index) {
                0 -> EASY
                1 -> MEDIUM
                2 -> DIFFICULT
                else -> UNKNOWN
            }
        }
    }
}