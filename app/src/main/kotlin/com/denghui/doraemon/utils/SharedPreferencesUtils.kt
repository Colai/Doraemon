@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER", "unused", "RedundantSemicolon")

package com.denghui.doraemon.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.denghui.doraemon.BuildConfig
import java.util.HashMap

private typealias SP = SharedPreferences

private val debug = BuildConfig.DEBUG

fun Context.sp(spName: String = this.packageName, mode: Int = Context.MODE_PRIVATE): SP {
    return this.applicationContext.getSharedPreferences(spName, mode)
}

fun SP.getBoolean(key: String, defaultValue: Boolean = false): Boolean {
    return this.getBoolean(key, defaultValue)
}

fun SP.setBoolean(key: String, value: Boolean = true) {
    this.edit().putBoolean(key, value).apply()
}

fun SP.getInt(key: String, defaultValue: Int = -1): Int {
    return this.getInt(key, defaultValue)
}

fun SP.setInt(key: String, value: Int = -1) {
    this.edit().putInt(key, value).apply()
}

fun SP.getLong(key: String, defaultValue: Long = -1): Long {
    return this.getLong(key, defaultValue)
}

fun SP.setLong(key: String, value: Long = -1) {
    this.edit().putLong(key, value).apply()
}

fun SP.getFloat(key: String, defaultValue: Float = -1F): Float {
    return this.getFloat(key, defaultValue)
}

fun SP.setFloat(key: String, value: Float = -1F) {
    this.edit().putFloat(key, value).apply()
}

fun SP.getString(key: String, defaultValue: String = ""): String {
    return this.getString(key, defaultValue)
}

fun SP.setString(key: String, value: String = "") {
    this.edit().putString(key, value).apply()
}

fun SP.setMap(data: HashMap<String, Any>) {
    val editor = this.edit()

    for ((key, value) in data) {
        when (value) {
            is Boolean -> editor.putBoolean(key, value)
            is Int -> editor.putInt(key, value)
            is Long -> editor.putLong(key, value)
            is Float -> editor.putFloat(key, value)
            is String -> editor.putString(key, value)
            else -> if (debug) Log.e("SharedPreferences", "$key has illegal value: $value")
        }
    }

    editor.apply()
}

fun Context.spContains(key: String, spName: String = this.packageName): Boolean {
    return this.sp(spName).contains(key);
}

fun Context.spClear(spName: String = this.packageName) {
    this.spEditor(spName).clear().apply()
}

fun Context.spRemove(key: String, spName: String = this.packageName) {
    this.spEditor(spName).remove(key).apply()
}

fun Context.spEditor(spName: String = this.packageName, mode: Int = Context.MODE_PRIVATE): SharedPreferences.Editor {
    return this.applicationContext.getSharedPreferences(spName, mode).edit()
}