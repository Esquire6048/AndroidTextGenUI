package com.example.textgenui.backend

import android.content.Context
import android.util.Log
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Locale

object PrefUtils {
    private val TAG = this::class.simpleName
    private val TEMPERATURE_KEY = floatPreferencesKey("temperature")
    private val HISTORY_KEY = stringPreferencesKey("history")
    private val LOCALE_KEY = stringPreferencesKey("locale")
    private val LLM_CONTEXT_STRING_KEY = stringPreferencesKey("llm_context_string")
    private val LLM_USER_BIO_KEY = stringPreferencesKey("llm_user_bio")
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    suspend fun saveLLMContext(context: Context, value: String) {
        context.dataStore.edit { settings -> settings[LLM_CONTEXT_STRING_KEY] = value }
    }

    suspend fun loadLLMContext(context: Context): String {
        return context.dataStore.data.first()[LLM_CONTEXT_STRING_KEY] ?: ""
    }

    suspend fun saveHistory(context: Context, value: List<MsgItem>) {
        context.dataStore.edit { settings -> settings[HISTORY_KEY] = Json.encodeToString<List<MsgItem>>(value) }
    }

    suspend fun loadHistory(context: Context): List<MsgItem> {
        val result = context.dataStore.data.first()[HISTORY_KEY]
        return if (result != null) Json.decodeFromString<List<MsgItem>>(result) else emptyList()
    }

    suspend fun saveTemperature(context: Context, value: Float) {
        context.dataStore.edit { settings -> settings[TEMPERATURE_KEY] = value }
    }

    suspend fun loadTemperature(context: Context): Float {
        return context.dataStore.data.first()[TEMPERATURE_KEY] ?: 0.5f
    }

    suspend fun saveLocale(context: Context, locale: Locale) {
        context.dataStore.edit { settings -> settings[LOCALE_KEY] = locale.toLanguageTag() }
    }

    suspend fun loadLocale(context: Context): Locale {
        val languageTag = context.dataStore.data.first()[LOCALE_KEY]
        val result = if (languageTag != null) Locale.forLanguageTag(languageTag) else Locale.getDefault()
        Log.e("DEBUG", "Loaded locale: " + result.toLanguageTag())
        return result
    }

    suspend fun saveLLMUserBio(context: Context, value: String) {
        context.dataStore.edit { settings -> settings[LLM_USER_BIO_KEY] = value }
    }

    suspend fun loadLLMUserBio(context: Context): String {
        return context.dataStore.data.first()[LLM_USER_BIO_KEY] ?: ""
    }
}