package com.sharkaboi.yogapartner.ml.classification

@Suppress("EnumEntryName", "SpellCheckingInspection")
enum class AsanaClass {
    UNKNOWN,
    adho_mukha_svanasana,
    bhujangasana,
    bidalasana,
    phalakasana,
    ustrasana,
    utkatasana,
    utkata_konasana,
    virabhadrasana_i,
    virabhadrasana_ii,
    vrikshasana;

    fun getFormattedString(): String {
        val string = when (this) {
            UNKNOWN -> "Unknown"
            adho_mukha_svanasana -> "adho mukha svanasana"
            bhujangasana -> "bhujangasana"
            phalakasana -> "phalakasana"
            ustrasana -> "ustrasana"
            utkatasana -> "utkatasana"
            virabhadrasana_i -> "virabhadrasana 1"
            virabhadrasana_ii -> "virabhadrasana 2"
            utkata_konasana -> "utkata konasana"
            bidalasana -> "bidalasana"
            vrikshasana -> "vrikshasana"
        }
        return string
    }
}