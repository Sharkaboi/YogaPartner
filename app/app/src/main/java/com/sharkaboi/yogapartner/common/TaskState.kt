package com.sharkaboi.yogapartner.common

sealed class TaskState<out T : Any> {
    data class Success<out R : Any>(val value: R) : TaskState<R>()
    data class Failure(val message: String) : TaskState<Nothing>()
    object NoErrorFailure: TaskState<Nothing>()
}