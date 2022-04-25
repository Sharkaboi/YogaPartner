package com.sharkaboi.yogapartner.common.extensions

import android.content.Context
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.Task
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * @return int code from [com.google.android.gms.common.ConnectionResult]
 */
internal fun Context.isGooglePlayServicesAvailable(): Int {
    val googleApiAvailability = GoogleApiAvailability.getInstance()
    return googleApiAvailability.isGooglePlayServicesAvailable(this.applicationContext)
}

@JvmName("awaitVoid")
suspend fun Task<Void>.await() = suspendCoroutine<Unit> { continuation ->
    addOnSuccessListener { continuation.resume(Unit) }
    addOnFailureListener { continuation.resumeWithException(it) }
}

suspend fun <TResult> Task<TResult>.await() = suspendCoroutine<TResult> { continuation ->
    addOnSuccessListener { continuation.resume(it) }
    addOnFailureListener { continuation.resumeWithException(it) }
}