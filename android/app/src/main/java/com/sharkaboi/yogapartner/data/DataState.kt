package com.sharkaboi.yogapartner.data

sealed class DataState<T : Any> {
    data class Success<T : Any>(val data: T) : DataState<T>()
    data class Failed<T : Any>(val message: String) : DataState<T>()
}

internal suspend fun <T : Any> getDataState(block: suspend () -> T): DataState<T> {
    runCatching {
        val result = block()
        return DataState.Success(result)
    }.getOrElse {
        return DataState.Failed(it.message ?: "")
    }
}