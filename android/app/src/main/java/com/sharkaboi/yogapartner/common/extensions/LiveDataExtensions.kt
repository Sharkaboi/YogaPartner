package com.sharkaboi.yogapartner.common.extensions

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData

fun <T> Fragment.observe(liveData: LiveData<T>, action: (t: T) -> Unit) {
    liveData.observe(viewLifecycleOwner) { t ->
        action(t)
    }
}
