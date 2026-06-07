@file:Suppress("DEPRECATION")

package com.disheveled.dailyquotes.data.api

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.core.module.Module
import org.koin.dsl.module

actual val settingsModule: Module = module {
    single<Settings> {
        val context = get<Context>()
        val securePrefs = encryptedSessionPrefs(context)
        migrateLegacySessionPrefs(
            legacy = context.getSharedPreferences(LEGACY_SESSION_PREFS, Context.MODE_PRIVATE),
            secure = securePrefs,
        )
        SharedPreferencesSettings(securePrefs)
    }
}

private fun encryptedSessionPrefs(context: Context): SharedPreferences {
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    return EncryptedSharedPreferences.create(
        context,
        SECURE_SESSION_PREFS,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
}

private fun migrateLegacySessionPrefs(legacy: SharedPreferences, secure: SharedPreferences) {
    if (legacy.all.isEmpty() || secure.all.isNotEmpty()) return

    val editor = secure.edit()
    legacy.all.forEach { (key, value) ->
        if (value is String) editor.putString(key, value)
    }
    editor.apply()
    legacy.edit().clear().apply()
}

private const val LEGACY_SESSION_PREFS = "dailyquotes"
private const val SECURE_SESSION_PREFS = "dailyquotes_secure"
