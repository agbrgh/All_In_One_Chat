package com.example.all_in_one_chat.data.local.prefs

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "secure_prefs")

@Singleton
class SecurePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val aead: Aead

    init {
        AeadConfig.register()
        val keysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context, "tink_keyset", "tink_key_file")
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri("android-keystore://master_key")
            .build()
            .keysetHandle
        aead = keysetHandle.getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    }

    private fun encrypt(value: String): String {
        val encrypted = aead.encrypt(value.toByteArray(Charsets.UTF_8), null)
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    private fun decrypt(encryptedValue: String): String {
        return try {
            val decoded = Base64.decode(encryptedValue, Base64.NO_WRAP)
            String(aead.decrypt(decoded, null), Charsets.UTF_8)
        } catch (_: Exception) {
            ""
        }
    }

    suspend fun saveString(key: String, value: String) {
        val encryptedValue = encrypt(value)
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(key)] = encryptedValue
        }
    }

    suspend fun getString(key: String): String? {
        val encryptedValue = context.dataStore.data
            .map { preferences -> preferences[stringPreferencesKey(key)] }
            .first()
        return encryptedValue?.let { decrypt(it) }
    }

    fun getStringFlow(key: String): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[stringPreferencesKey(key)]?.let { decrypt(it) }
        }
    }

    suspend fun saveBoolean(key: String, value: Boolean) {
        saveString(key, value.toString())
    }

    suspend fun getBoolean(key: String, defaultValue: Boolean = true): Boolean {
        return getString(key)?.toBoolean() ?: defaultValue
    }

    fun getBooleanFlow(key: String, defaultValue: Boolean = true): Flow<Boolean> {
        return getStringFlow(key).map { it?.toBoolean() ?: defaultValue }
    }

    suspend fun remove(key: String) {
        context.dataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey(key))
        }
    }

    companion object {
        const val KEY_TELEGRAM_SESSION = "telegram_session"
        const val KEY_DISCORD_TOKEN = "discord_token"
        const val KEY_WHATSAPP_TOKEN = "whatsapp_token"
        const val KEY_SYNC_INTERVAL = "sync_interval"
        const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
    }
}
