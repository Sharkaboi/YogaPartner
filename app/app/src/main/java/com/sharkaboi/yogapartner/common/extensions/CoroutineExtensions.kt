package com.sharkaboi.yogapartner.common.extensions

import kotlinx.coroutines.*

fun <T> debounce(
    delay: Long = 800L,
    scope: CoroutineScope,
    callback: (T?) -> Unit
): (T?) -> Unit {
    var debounceJob: Job? = null
    return { param: T? ->
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(delay)
            callback(param)
        }
    }
}