package com.devd.calenderbydw.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class CalendarDataStore @Inject constructor(
    private val calendarDataStore: DataStore<Preferences>
) : CalendarPreferenceInterface {

    override suspend fun setPreferString(key: String, value: String) {
        Result.runCatching {
            calendarDataStore.edit { preferences ->
                preferences[stringPreferencesKey(key)] = value
            }
        }
    }

    override suspend fun getPreferString(key: String): String? {
        return Result.runCatching {
            val flow = calendarDataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .map { preferences ->
                    preferences[stringPreferencesKey(key)]
                }
            val value = flow.firstOrNull() ?: ""
            value
        }.getOrNull()
    }

    override suspend fun getStringClear(key: String) {
        Result.runCatching {
            calendarDataStore.edit { preferences ->
                preferences.remove(stringPreferencesKey(key))
            }
        }
    }

    override suspend fun setPreferBoolean(key: String, value: Boolean) {
        Result.runCatching {
            calendarDataStore.edit { preferences ->
                preferences[booleanPreferencesKey(key)] = value
            }
        }
    }

    fun preferStringFlow(key :String) : Flow<String?>{
        return calendarDataStore.data.map { it -> it[stringPreferencesKey(key)] }
    }

    override suspend fun getPreferBoolean(key: String): Boolean? {
        return Result.runCatching {
            val flow = calendarDataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .map { preferences ->
                    preferences[booleanPreferencesKey(key)]
                }
            val value = flow.firstOrNull() ?: false
            value
        }.getOrNull()
    }

    override suspend fun getBooleanClear(key: String) {
        Result.runCatching {
            calendarDataStore.edit { preferences ->
                preferences.remove(booleanPreferencesKey(key))
            }
        }
    }

    override suspend fun setPreferLong(key: String, value: Long) {
        Result.runCatching {
            calendarDataStore.edit { preferences ->
                preferences[longPreferencesKey(key)] = value
            }
        }
    }

    override suspend fun getPreferLong(key: String): Long? {

        return Result.runCatching {
            val flow = calendarDataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .map { preferences ->
                    preferences[longPreferencesKey(key)]
                }
            val value = flow.firstOrNull()
            value
        }.getOrNull()
    }

    fun preferLongFlow(key :String) : Flow<Long?>{
        return calendarDataStore.data.map { it -> it[longPreferencesKey(key)] }
    }

    override suspend fun getLongClear(key: String) {
        Result.runCatching {
            calendarDataStore.edit { preferences ->
                preferences.remove(longPreferencesKey(key))
            }
        }
    }
}


interface CalendarPreferenceInterface {
    suspend fun setPreferString(key: String, value: String)
    suspend fun getPreferString(key: String): String?
    suspend fun getStringClear(key: String)
    suspend fun setPreferBoolean(key: String, value: Boolean)
    suspend fun getPreferBoolean(key: String): Boolean?
    suspend fun getBooleanClear(key: String)
    suspend fun setPreferLong(key: String, value: Long)
    suspend fun getPreferLong(key: String): Long?
    suspend fun getLongClear(key: String)
}

class DataStoreKey{
    companion object{
        const val PREF_KEY_WIDGET_SHOW_TIME="widgetShowTime"
        const val PREF_KET_WIDGET_CLICK_DATE ="clickDate"
        const val PREF_WRITE_DIARY_LAST_DATE = "writeDiaryLastDate"
        const val PREF_TODAY_ALREADY_WRITE_DIARY = "alreadyWriteDiary"
    }
}