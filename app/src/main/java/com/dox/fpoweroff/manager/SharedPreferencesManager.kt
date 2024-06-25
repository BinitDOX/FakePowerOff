package com.dox.fpoweroff.manager

import android.content.Context
import android.content.SharedPreferences
import com.dox.fpoweroff.utility.Constants.APP_PREFS
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SharedPreferencesManager @Inject constructor(@ApplicationContext context: Context) {
    private var sharedPrefs: SharedPreferences = context.getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE)

    fun save(key: String, value: String?) {
        sharedPrefs.edit().putString(key, value).apply()
    }

    fun get(key: String): String? {
        return sharedPrefs.getString(key, null)
    }
}