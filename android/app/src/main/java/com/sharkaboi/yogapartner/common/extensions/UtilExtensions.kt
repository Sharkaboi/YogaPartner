package com.sharkaboi.yogapartner.common.extensions

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment

internal fun Fragment.showToast(message: String?, length: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(context, message ?: "", length).show()

internal fun Context.showToast(message: String?, length: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, message ?: "", length).show()
