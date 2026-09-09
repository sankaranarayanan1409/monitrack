package com.example.monitrack.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.monitrack.data.Profile
import com.example.monitrack.data.enums.Sex
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.profileDataStore by preferencesDataStore(name = "profile")

/** DataStore-backed persistence for the single [Profile]. */
class ProfileStore(private val context: Context) {

    private object Keys {
        val SETUP_COMPLETE = booleanPreferencesKey("setup_complete")
        val BODY_FAT_ENABLED = booleanPreferencesKey("body_fat_enabled")
        val SEX = stringPreferencesKey("sex")
        val HEIGHT_CM = doublePreferencesKey("height_cm")
        val DOB_EPOCH_DAY = longPreferencesKey("dob_epoch_day")
    }

    /** Emits null until setup has written a profile, then the stored [Profile]. */
    val profileFlow: Flow<Profile?> = context.profileDataStore.data.map { prefs ->
        val setupComplete = prefs[Keys.SETUP_COMPLETE] ?: return@map null
        Profile(
            setupComplete = setupComplete,
            bodyFatEnabled = prefs[Keys.BODY_FAT_ENABLED] ?: false,
            sex = prefs[Keys.SEX]?.let { Sex.valueOf(it) },
            heightCm = prefs[Keys.HEIGHT_CM],
            dateOfBirth = prefs[Keys.DOB_EPOCH_DAY]?.let { LocalDate.ofEpochDay(it) },
        )
    }

    suspend fun getProfile(): Profile? = profileFlow.first()

    suspend fun save(profile: Profile) {
        context.profileDataStore.edit { prefs ->
            prefs[Keys.SETUP_COMPLETE] = profile.setupComplete
            prefs[Keys.BODY_FAT_ENABLED] = profile.bodyFatEnabled
            profile.sex?.let { prefs[Keys.SEX] = it.name } ?: prefs.remove(Keys.SEX)
            profile.heightCm?.let { prefs[Keys.HEIGHT_CM] = it } ?: prefs.remove(Keys.HEIGHT_CM)
            profile.dateOfBirth?.let { prefs[Keys.DOB_EPOCH_DAY] = it.toEpochDay() }
                ?: prefs.remove(Keys.DOB_EPOCH_DAY)
        }
    }
}
