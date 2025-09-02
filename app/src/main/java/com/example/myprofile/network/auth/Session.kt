package com.example.myprofile.network.auth

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit

object Session {
    private const val PREFS = "auth_prefs"
    private const val TOKEN = "JWT_TOKEN"

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun saveToken(token: String) {
        appContext.getSharedPreferences(PREFS, MODE_PRIVATE)
            .edit { putString(TOKEN, token) }
    }

    fun token(): String? {
        return appContext.getSharedPreferences(PREFS, MODE_PRIVATE)
            .getString(TOKEN, null)
    }

    fun clear() {
        appContext.getSharedPreferences(PREFS, MODE_PRIVATE)
            .edit { remove(TOKEN) }
    }

    fun isLoggedIn(): Boolean {
        val t = token() ?: return false
        return !Jwt.isExpired(t)
    }
}