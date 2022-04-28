package com.sharkaboi.yogapartner.data.models

enum class BodyPart {
    UNKNOWN,
    CALF,
    BICEP,
    SHOULDERS;

    fun getDbValue(): String {
        return when (this) {
            UNKNOWN -> "unknown"
            CALF -> "calf"
            BICEP -> "bicep"
            SHOULDERS -> "shoulders"
        }
    }

    companion object {
        fun parseFromDb(string: String): BodyPart {
            return when (string) {
                "shoulders" -> SHOULDERS
                "bicep" -> BICEP
                "calf" -> CALF
                else -> UNKNOWN
            }
        }

        fun parseList(list: List<String>): List<BodyPart> {
            return list.map { parseFromDb(it) }
        }
    }
}

fun List<BodyPart>.getDbValue(): List<String> {
    return this.map { it.getDbValue() }
}