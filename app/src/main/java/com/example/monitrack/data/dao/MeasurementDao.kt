package com.example.monitrack.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.monitrack.data.entity.BodyMeasurement
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MeasurementDao {
    @Insert
    suspend fun insert(measurement: BodyMeasurement): Long

    @Query("SELECT * FROM measurements WHERE date BETWEEN :start AND :end ORDER BY date")
    fun measurementsBetween(start: LocalDate, end: LocalDate): Flow<List<BodyMeasurement>>

    @Query("SELECT * FROM measurements ORDER BY date DESC LIMIT 1")
    fun latestMeasurement(): Flow<BodyMeasurement?>
}