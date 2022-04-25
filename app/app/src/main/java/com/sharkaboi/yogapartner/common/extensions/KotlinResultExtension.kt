package com.sharkaboi.yogapartner.common.extensions

internal fun <T> Result<T>.getOrNullWithStackTrace(): T? {
    return when {
        isFailure -> {
            exceptionOrNull()?.printStackTrace()
            null
        }
        else -> this.getOrDefault(null)
    }
}
