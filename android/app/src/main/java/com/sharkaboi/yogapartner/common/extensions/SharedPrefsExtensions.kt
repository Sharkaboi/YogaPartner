package com.sharkaboi.yogapartner.common.extensions

import android.content.SharedPreferences
import androidx.core.content.edit

fun <T> SharedPreferences.getOrSetDefault(key: String, value: T): T {
    return when (value) {
        is Float -> {
            val contains = this.contains(key)
            if (!contains) {
                this.edit { putFloat(key, value) }
            }
            this.getFloat(key, value) as T
        }
        is Boolean -> {
            val contains = this.contains(key)
            if (!contains) {
                this.edit { putBoolean(key, value) }
            }
            this.getBoolean(key, value) as T
        }
        is String -> {
            val contains = this.contains(key)
            if (!contains) {
                this.edit { putString(key, value) }
            }
            this.getString(key, value) as T
        }
        is Set<*> -> {
            val castedSet = value as Set<String>
            val contains = this.contains(key)
            if (!contains) {
                this.edit { putStringSet(key, castedSet) }
            }
            this.getStringSet(key, castedSet) as T
        }
        is Int -> {
            val contains = this.contains(key)
            if (!contains) {
                this.edit { putInt(key, value) }
            }
            this.getInt(key, value) as T
        }
        is Long -> {
            val contains = this.contains(key)
            if (!contains) {
                this.edit { putLong(key, value) }
            }
            this.getLong(key, value) as T
        }
        else -> throw IllegalArgumentException("Invalid value for getOrSetDefault")
    }
}