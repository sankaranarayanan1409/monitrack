package com.example.monitrack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.monitrack.data.dao.ActivityConfigDao
import com.example.monitrack.data.dao.ActivityDao
import com.example.monitrack.data.dao.MeasurementDao
import com.example.monitrack.data.dao.SessionDao
import com.example.monitrack.data.entity.Activity
import com.example.monitrack.data.entity.ActivityConfig
import com.example.monitrack.data.entity.BodyMeasurement
import com.example.monitrack.data.entity.Session
import com.example.monitrack.data.util.Converters

@Database(
    entities = [Session::class, BodyMeasurement::class, ActivityConfig::class, Activity::class],
    version = 2,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class MonitrackDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun measurementDao(): MeasurementDao
    abstract fun activityConfigDao(): ActivityConfigDao
    abstract fun activityDao(): ActivityDao

    companion object {
        @Volatile
        private var instance: MonitrackDatabase? = null

        fun get(context: Context): MonitrackDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MonitrackDatabase::class.java,
                    "monitrack.db",
                )
                    // Pre-release: no shipped data to preserve, so recreate on schema change.
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
    }
}
