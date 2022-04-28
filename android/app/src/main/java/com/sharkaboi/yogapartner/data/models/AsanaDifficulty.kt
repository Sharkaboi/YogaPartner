package com.sharkaboi.yogapartner.data.models

enum class AsanaDifficulty {
    UNKNOWN,
    EASY,
    MEDIUM,
    DIFFICULT;

    fun getDbValue(): Int {
        return when (this) {
            UNKNOWN -> -1
            EASY -> 0
            MEDIUM -> 1
            DIFFICULT -> 2
        }
    }

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