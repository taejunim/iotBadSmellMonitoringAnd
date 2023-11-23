package kr.co.metisinfo.iotbadsmellmonitoringand.util

import android.content.Context
import android.content.SharedPreferences

class PreferenceUtil(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("preference_data", Context.MODE_PRIVATE)

    fun getString(key: String, defValue: String): String {
        return prefs.getString(key, defValue).toString()
    }

    fun setString(key: String, str: String) {
        prefs.edit().putString(key, str).apply()
    }

    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return prefs.getBoolean(key, defValue)
    }

    fun setBoolean(key: String, str: Boolean) {
        prefs.edit().putBoolean(key, str).apply()
    }

    fun getLong(key: String, defValue: Long): String {
        return prefs.getLong(key, defValue).toString()
    }

    fun setLong(key: String, str: Long) {
        prefs.edit().putLong(key, str).apply()
    }

    fun isExist(key: String): Boolean {
        return prefs.contains(key)
    }
}

