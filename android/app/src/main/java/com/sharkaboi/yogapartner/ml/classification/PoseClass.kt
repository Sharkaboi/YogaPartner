package com.sharkaboi.yogapartner.ml.classification

import android.content.Context

@Suppress("EnumEntryName")
enum class PoseClass {
    UNKNOWN,
    adho_mukha_svanasana,
    adho_mukha_vriksasana,
    agnistambhasana,
    ananda_balasana,
    anantasana,
    anjaneyasana,
    ardha_bhekasana,
    ardha_chandrasana,
    ardha_matsyendrasana,
    ardha_pincha_mayurasana,
    ardha_uttanasana,
    ashtanga_namaskara,
    astavakrasana,
    baddha_konasana,
    bakasana,
    balasana,
    bhairavasana,
    bharadvajasana_i,
    bhekasana,
    bhujangasana,
    bhujapidasana,
    bitilasana,
    camatkarasana,
    chakravakasana,
    chaturanga_dandasana,
    dandasana,
    dhanurasana,
    durvasasana,
    dwi_pada_viparita_dandasana,
    eka_pada_koundinyanasana_i,
    eka_pada_koundinyanasana_ii,
    eka_pada_rajakapotasana,
    eka_pada_rajakapotasana_ii,
    ganda_bherundasana,
    garbha_pindasana,
    garudasana,
    gomukhasana,
    halasana,
    hanumanasana,
    janu_sirsasana,
    kapotasana,
    krounchasana,
    kurmasana,
    lolasana,
    makarasana,
    makara_adho_mukha_svanasana,
    malasana,
    marichyasana_i,
    marichyasana_iii,
    marjaryasana,
    matsyasana,
    mayurasana,
    natarajasana,
    padangusthasana,
    padmasana,
    parighasana,
    paripurna_navasana,
    parivrtta_janu_sirsasana,
    parivrtta_parsvakonasana,
    parivrtta_trikonasana,
    parsva_bakasana,
    parsvottanasana,
    pasasana,
    paschimottanasana,
    phalakasana,
    pincha_mayurasana,
    prasarita_padottanasana,
    purvottanasana,
    salabhasana,
    salamba_bhujangasana,
    salamba_sarvangasana,
    salamba_sirsasana,
    savasana,
    setu_bandha_sarvangasana,
    simhasana,
    sukhasana,
    supta_baddha_konasana,
    supta_matsyendrasana,
    supta_padangusthasana,
    supta_virasana,
    tadasana,
    tittibhasana,
    tolasana,
    tulasana,
    upavistha_konasana,
    urdhva_dhanurasana,
    urdhva_hastasana,
    urdhva_mukha_svanasana,
    urdhva_prasarita_eka_padasana,
    ustrasana,
    utkatasana,
    uttanasana,
    uttana_shishosana,
    utthita_ashwa_sanchalanasana,
    utthita_hasta_padangustasana,
    utthita_parsvakonasana,
    utthita_trikonasana,
    vajrasana,
    vasisthasana,
    viparita_karani,
    virabhadrasana_i,
    virabhadrasana_ii,
    virabhadrasana_iii,
    virasana,
    vriksasana,
    vrischikasana,
    yoganidrasana;

    fun getFormattedString(): String {
//    @StringRes val stringResId = when(this){
        val string = when (this) {
            PoseClass.UNKNOWN -> "Unknown"
            PoseClass.adho_mukha_svanasana -> "adho mukha svanasana"
            PoseClass.adho_mukha_vriksasana -> "adho mukha vriksasana"
            PoseClass.agnistambhasana -> "agnistambhasana"
            PoseClass.ananda_balasana -> "ananda balasana"
            PoseClass.anantasana -> "anantasana"
            PoseClass.anjaneyasana -> "anjaneyasana"
            PoseClass.ardha_bhekasana -> "ardha bhekasana"
            PoseClass.ardha_chandrasana -> "ardha chandrasana"
            PoseClass.ardha_matsyendrasana -> "ardha matsyendrasana"
            PoseClass.ardha_pincha_mayurasana -> "ardha pincha mayurasana"
            PoseClass.ardha_uttanasana -> "ardha uttanasana"
            PoseClass.ashtanga_namaskara -> "ashtanga namaskara"
            PoseClass.astavakrasana -> "astavakrasana"
            PoseClass.baddha_konasana -> "baddha konasana"
            PoseClass.bakasana -> "bakasana"
            PoseClass.balasana -> "balasana"
            PoseClass.bhairavasana -> "bhairavasana"
            PoseClass.bharadvajasana_i -> "bharadvajasana i"
            PoseClass.bhekasana -> "bhekasana"
            PoseClass.bhujangasana -> "bhujangasana"
            PoseClass.bhujapidasana -> "bhujapidasana"
            PoseClass.bitilasana -> "bitilasana"
            PoseClass.camatkarasana -> "camatkarasana"
            PoseClass.chakravakasana -> "chakravakasana"
            PoseClass.chaturanga_dandasana -> "chaturanga dandasana"
            PoseClass.dandasana -> "dandasana"
            PoseClass.dhanurasana -> "dhanurasana"
            PoseClass.durvasasana -> "durvasasana"
            PoseClass.dwi_pada_viparita_dandasana -> "dwi pada viparita dandasana"
            PoseClass.eka_pada_koundinyanasana_i -> "eka pada koundinyanasana i"
            PoseClass.eka_pada_koundinyanasana_ii -> "eka pada koundinyanasana ii"
            PoseClass.eka_pada_rajakapotasana -> "eka pada rajakapotasana"
            PoseClass.eka_pada_rajakapotasana_ii -> "eka pada rajakapotasana ii"
            PoseClass.ganda_bherundasana -> "ganda bherundasana"
            PoseClass.garbha_pindasana -> "garbha pindasana"
            PoseClass.garudasana -> "garudasana"
            PoseClass.gomukhasana -> "gomukhasana"
            PoseClass.halasana -> "halasana"
            PoseClass.hanumanasana -> "hanumanasana"
            PoseClass.janu_sirsasana -> "janu sirsasana"
            PoseClass.kapotasana -> "kapotasana"
            PoseClass.krounchasana -> "krounchasana"
            PoseClass.kurmasana -> "kurmasana"
            PoseClass.lolasana -> "lolasana"
            PoseClass.makarasana -> "makarasana"
            PoseClass.makara_adho_mukha_svanasana -> "makara adho mukha svanasana"
            PoseClass.malasana -> "malasana"
            PoseClass.marichyasana_i -> "marichyasana i"
            PoseClass.marichyasana_iii -> "marichyasana iii"
            PoseClass.marjaryasana -> "marjaryasana"
            PoseClass.matsyasana -> "matsyasana"
            PoseClass.mayurasana -> "mayurasana"
            PoseClass.natarajasana -> "natarajasana"
            PoseClass.padangusthasana -> "padangusthasana"
            PoseClass.padmasana -> "padmasana"
            PoseClass.parighasana -> "parighasana"
            PoseClass.paripurna_navasana -> "paripurna navasana"
            PoseClass.parivrtta_janu_sirsasana -> "parivrtta janu sirsasana"
            PoseClass.parivrtta_parsvakonasana -> "parivrtta parsvakonasana"
            PoseClass.parivrtta_trikonasana -> "parivrtta trikonasana"
            PoseClass.parsva_bakasana -> "parsva bakasana"
            PoseClass.parsvottanasana -> "parsvottanasana"
            PoseClass.pasasana -> "pasasana"
            PoseClass.paschimottanasana -> "paschimottanasana"
            PoseClass.phalakasana -> "phalakasana"
            PoseClass.pincha_mayurasana -> "pincha_mayurasana"
            PoseClass.prasarita_padottanasana -> "prasarita padottanasana"
            PoseClass.purvottanasana -> "purvottanasana"
            PoseClass.salabhasana -> "salabhasana"
            PoseClass.salamba_bhujangasana -> "salamba bhujangasana"
            PoseClass.salamba_sarvangasana -> "salamba sarvangasana"
            PoseClass.salamba_sirsasana -> "salamba sirsasana"
            PoseClass.savasana -> "savasana"
            PoseClass.setu_bandha_sarvangasana -> "setu bandha sarvangasana"
            PoseClass.simhasana -> "simhasana"
            PoseClass.sukhasana -> "sukhasana"
            PoseClass.supta_baddha_konasana -> "supta baddha konasana"
            PoseClass.supta_matsyendrasana -> "supta matsyendrasana"
            PoseClass.supta_padangusthasana -> "supta padangusthasana"
            PoseClass.supta_virasana -> "supta virasana"
            PoseClass.tadasana -> "tadasana"
            PoseClass.tittibhasana -> "tittibhasana"
            PoseClass.tolasana -> "tolasana"
            PoseClass.tulasana -> "tulasana"
            PoseClass.upavistha_konasana -> "upavistha konasana"
            PoseClass.urdhva_dhanurasana -> "urdhva dhanurasana"
            PoseClass.urdhva_hastasana -> "urdhva hastasana"
            PoseClass.urdhva_mukha_svanasana -> "urdhva mukha svanasana"
            PoseClass.urdhva_prasarita_eka_padasana -> "urdhva prasarita eka padasana"
            PoseClass.ustrasana -> "ustrasana"
            PoseClass.utkatasana -> "utkatasana"
            PoseClass.uttanasana -> "uttanasana"
            PoseClass.uttana_shishosana -> "uttana shishosana"
            PoseClass.utthita_ashwa_sanchalanasana -> "utthita ashwa sanchalanasana"
            PoseClass.utthita_hasta_padangustasana -> "utthita hasta padangustasana"
            PoseClass.utthita_parsvakonasana -> "utthita parsvakonasana"
            PoseClass.utthita_trikonasana -> "utthita trikonasana"
            PoseClass.vajrasana -> "vajrasana"
            PoseClass.vasisthasana -> "vasisthasana"
            PoseClass.viparita_karani -> "viparita karani"
            PoseClass.virabhadrasana_i -> "virabhadrasana i"
            PoseClass.virabhadrasana_ii -> "virabhadrasana ii"
            PoseClass.virabhadrasana_iii -> "virabhadrasana iii"
            PoseClass.virasana -> "virasana"
            PoseClass.vriksasana -> "vriksasana"
            PoseClass.vrischikasana -> "vrischikasana"
            PoseClass.yoganidrasana -> "yoganidrasana"
        }
//    return context.getString(stringResId)
        return string
    }
}