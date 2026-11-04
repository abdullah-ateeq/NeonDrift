package com.example.neondrift.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "neondrift_prefs")

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val BEST_SCORE = intPreferencesKey("best_score")
        val BEST_DISTANCE = floatPreferencesKey("best_distance")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        val themeString = prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        val themeMode = try {
            ThemeMode.valueOf(themeString)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
        val sound = prefs[Keys.SOUND_ENABLED] ?: true
        val haptics = prefs[Keys.HAPTICS_ENABLED] ?: true
        AppSettings(
            themeMode = themeMode,
            soundEnabled = sound,
            hapticsEnabled = haptics
        )
    }

    val statsFlow: Flow<GameStats> = context.dataStore.data.map { prefs ->
        GameStats(
            bestScore = prefs[Keys.BEST_SCORE] ?: 0,
            bestDistance = prefs[Keys.BEST_DISTANCE] ?: 0f
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun updateHighScore(score: Int, distance: Float): Boolean {
        var isNewHigh = false
        context.dataStore.edit { prefs ->
            val currentBestScore = prefs[Keys.BEST_SCORE] ?: 0
            val currentBestDist = prefs[Keys.BEST_DISTANCE] ?: 0f

            if (score > currentBestScore) {
                prefs[Keys.BEST_SCORE] = score
                isNewHigh = true
            }
            if (distance > currentBestDist) {
                prefs[Keys.BEST_DISTANCE] = distance
            }
        }
        return isNewHigh
    }

    suspend fun resetStats() {
        context.dataStore.edit { prefs ->
            prefs[Keys.BEST_SCORE] = 0
            prefs[Keys.BEST_DISTANCE] = 0f
        }
    }
}
