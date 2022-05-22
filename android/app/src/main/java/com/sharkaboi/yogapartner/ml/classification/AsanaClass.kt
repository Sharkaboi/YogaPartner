package com.sharkaboi.yogapartner.ml.classification

@Suppress("EnumEntryName")
enum class AsanaClass {
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
    utkata_konasana,
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
        val string = when (this) {
            UNKNOWN -> "Unknown"
            adho_mukha_svanasana -> "adho mukha svanasana"
            adho_mukha_vriksasana -> "adho mukha vriksasana"
            agnistambhasana -> "agnistambhasana"
            ananda_balasana -> "ananda balasana"
            anantasana -> "anantasana"
            anjaneyasana -> "anjaneyasana"
            ardha_bhekasana -> "ardha bhekasana"
            ardha_chandrasana -> "ardha chandrasana"
            ardha_matsyendrasana -> "ardha matsyendrasana"
            ardha_pincha_mayurasana -> "ardha pincha mayurasana"
            ardha_uttanasana -> "ardha uttanasana"
            ashtanga_namaskara -> "ashtanga namaskara"
            astavakrasana -> "astavakrasana"
            baddha_konasana -> "baddha konasana"
            bakasana -> "bakasana"
            balasana -> "balasana"
            bhairavasana -> "bhairavasana"
            bharadvajasana_i -> "bharadvajasana i"
            bhekasana -> "bhekasana"
            bhujangasana -> "bhujangasana"
            bhujapidasana -> "bhujapidasana"
            bitilasana -> "bitilasana"
            camatkarasana -> "camatkarasana"
            chakravakasana -> "chakravakasana"
            chaturanga_dandasana -> "chaturanga dandasana"
            dandasana -> "dandasana"
            dhanurasana -> "dhanurasana"
            durvasasana -> "durvasasana"
            dwi_pada_viparita_dandasana -> "dwi pada viparita dandasana"
            eka_pada_koundinyanasana_i -> "eka pada koundinyanasana i"
            eka_pada_koundinyanasana_ii -> "eka pada koundinyanasana ii"
            eka_pada_rajakapotasana -> "eka pada rajakapotasana"
            eka_pada_rajakapotasana_ii -> "eka pada rajakapotasana ii"
            ganda_bherundasana -> "ganda bherundasana"
            garbha_pindasana -> "garbha pindasana"
            garudasana -> "garudasana"
            gomukhasana -> "gomukhasana"
            halasana -> "halasana"
            hanumanasana -> "hanumanasana"
            janu_sirsasana -> "janu sirsasana"
            kapotasana -> "kapotasana"
            krounchasana -> "krounchasana"
            kurmasana -> "kurmasana"
            lolasana -> "lolasana"
            makarasana -> "makarasana"
            makara_adho_mukha_svanasana -> "makara adho mukha svanasana"
            malasana -> "malasana"
            marichyasana_i -> "marichyasana i"
            marichyasana_iii -> "marichyasana iii"
            marjaryasana -> "marjaryasana"
            matsyasana -> "matsyasana"
            mayurasana -> "mayurasana"
            natarajasana -> "natarajasana"
            padangusthasana -> "padangusthasana"
            padmasana -> "padmasana"
            parighasana -> "parighasana"
            paripurna_navasana -> "paripurna navasana"
            parivrtta_janu_sirsasana -> "parivrtta janu sirsasana"
            parivrtta_parsvakonasana -> "parivrtta parsvakonasana"
            parivrtta_trikonasana -> "parivrtta trikonasana"
            parsva_bakasana -> "parsva bakasana"
            parsvottanasana -> "parsvottanasana"
            pasasana -> "pasasana"
            paschimottanasana -> "paschimottanasana"
            phalakasana -> "phalakasana"
            pincha_mayurasana -> "pincha_mayurasana"
            prasarita_padottanasana -> "prasarita padottanasana"
            purvottanasana -> "purvottanasana"
            salabhasana -> "salabhasana"
            salamba_bhujangasana -> "salamba bhujangasana"
            salamba_sarvangasana -> "salamba sarvangasana"
            salamba_sirsasana -> "salamba sirsasana"
            savasana -> "savasana"
            setu_bandha_sarvangasana -> "setu bandha sarvangasana"
            simhasana -> "simhasana"
            sukhasana -> "sukhasana"
            supta_baddha_konasana -> "supta baddha konasana"
            supta_matsyendrasana -> "supta matsyendrasana"
            supta_padangusthasana -> "supta padangusthasana"
            supta_virasana -> "supta virasana"
            tadasana -> "tadasana"
            tittibhasana -> "tittibhasana"
            tolasana -> "tolasana"
            tulasana -> "tulasana"
            upavistha_konasana -> "upavistha konasana"
            urdhva_dhanurasana -> "urdhva dhanurasana"
            urdhva_hastasana -> "urdhva hastasana"
            urdhva_mukha_svanasana -> "urdhva mukha svanasana"
            urdhva_prasarita_eka_padasana -> "urdhva prasarita eka padasana"
            ustrasana -> "ustrasana"
            utkatasana -> "utkatasana"
            uttanasana -> "uttanasana"
            uttana_shishosana -> "uttana shishosana"
            utthita_ashwa_sanchalanasana -> "utthita ashwa sanchalanasana"
            utthita_hasta_padangustasana -> "utthita hasta padangustasana"
            utthita_parsvakonasana -> "utthita parsvakonasana"
            utthita_trikonasana -> "utthita trikonasana"
            vajrasana -> "vajrasana"
            vasisthasana -> "vasisthasana"
            viparita_karani -> "viparita karani"
            virabhadrasana_i -> "virabhadrasana i"
            virabhadrasana_ii -> "virabhadrasana ii"
            virabhadrasana_iii -> "virabhadrasana iii"
            virasana -> "virasana"
            vriksasana -> "vriksasana"
            vrischikasana -> "vrischikasana"
            yoganidrasana -> "yoganidrasana"
            utkata_konasana -> "utkata konasana"
        }
        return string
    }
}