package com.example.monitrack.data

import com.example.monitrack.data.enums.Sex
import java.time.LocalDate

/**
 * App profile: gates first-run setup and holds the rarely-changing details the US Navy
 * body-fat formula needs. Persisted in DataStore (see [com.example.monitrack.data.datastore.ProfileStore]);
 * all fields stay on-device.
 */
data class Profile(
    val setupComplete: Boolean = false,
    val bodyFatEnabled: Boolean = false,
    val sex: Sex? = null,
    val heightCm: Double? = null,
    val dateOfBirth: LocalDate? = null,
)
