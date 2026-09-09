package com.example.monitrack.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "measurements")
data class BodyMeasurement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val weightKg: Double? = null,
    val neckCm: Double? = null,
    val waistCm: Double? = null,
    val hipCm: Double? = null,
    /** Computed via the US Navy formula at entry time; stored so charts read it directly. */
    val bodyFatPercent: Double? = null,
)