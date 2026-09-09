package com.example.monitrack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 5,
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

        /** Drops the `icon` (INTEGER resource id) column from `activities` and `activity_config`. */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE activities_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL("INSERT INTO activities_new (id, name) SELECT id, name FROM activities")
                db.execSQL("DROP TABLE activities")
                db.execSQL("ALTER TABLE activities_new RENAME TO activities")
                db.execSQL("CREATE UNIQUE INDEX index_activities_name ON activities(name)")

                db.execSQL(
                    """
                    CREATE TABLE activity_config_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        type TEXT NOT NULL,
                        targetMinutes INTEGER NOT NULL,
                        aggregatesDaily INTEGER NOT NULL,
                        requiresTargetForCompletion INTEGER NOT NULL,
                        dailyNudgeEnabled INTEGER NOT NULL,
                        nudgeTime INTEGER NOT NULL,
                        overrunAlertEnabled INTEGER NOT NULL,
                        streakEnabled INTEGER NOT NULL,
                        streakColor INTEGER NOT NULL,
                        activityId INTEGER,
                        FOREIGN KEY(activityId) REFERENCES activities(id) ON DELETE SET NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO activity_config_new
                        (id, type, targetMinutes, aggregatesDaily, requiresTargetForCompletion,
                         dailyNudgeEnabled, nudgeTime, overrunAlertEnabled, streakEnabled, streakColor, activityId)
                    SELECT id, type, targetMinutes, aggregatesDaily, requiresTargetForCompletion,
                           dailyNudgeEnabled, nudgeTime, overrunAlertEnabled, streakEnabled, streakColor, activityId
                    FROM activity_config
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE activity_config")
                db.execSQL("ALTER TABLE activity_config_new RENAME TO activity_config")
                db.execSQL("CREATE INDEX index_activity_config_activityId ON activity_config(activityId)")
            }
        }

        /** Re-adds `icon` on `activities`, now a stable [com.example.monitrack.data.util.ActivityIcons] key. */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE activities ADD COLUMN icon TEXT")
                db.execSQL("UPDATE activities SET icon = 'sleep' WHERE name = 'Sleep'")
                db.execSQL("UPDATE activities SET icon = 'work' WHERE name = 'Work'")
                db.execSQL("UPDATE activities SET icon = 'exercise' WHERE name = 'Exercise'")
            }
        }

        /**
         * Adds `activityId` (FK to `activities`) on `sessions`, backfilled by matching the
         * existing `type` label to `activities.name`. From here on, sessions are queried by
         * that id — not the name — so a future rename of an activity can't orphan its history.
         * `type` is kept as a point-in-time display label; it just stops being load-bearing.
         */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE sessions_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        activityId INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        startTime INTEGER NOT NULL,
                        endTime INTEGER,
                        manuallyAdjusted INTEGER NOT NULL,
                        FOREIGN KEY(activityId) REFERENCES activities(id) ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO sessions_new (id, activityId, type, startTime, endTime, manuallyAdjusted)
                    SELECT sessions.id, activities.id, sessions.type, sessions.startTime, sessions.endTime, sessions.manuallyAdjusted
                    FROM sessions
                    INNER JOIN activities ON activities.name = sessions.type
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE sessions")
                db.execSQL("ALTER TABLE sessions_new RENAME TO sessions")
                db.execSQL("CREATE INDEX index_sessions_activityId ON sessions(activityId)")

                // activity_config.activityId was added in MIGRATION_2_3 but never backfilled;
                // DailyNudgeWorker requires it, so link existing rows the same way sessions are above.
                db.execSQL(
                    """
                    UPDATE activity_config
                    SET activityId = (SELECT id FROM activities WHERE activities.name = activity_config.type)
                    WHERE activityId IS NULL
                    """.trimIndent(),
                )
            }
        }

        fun get(context: Context): MonitrackDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MonitrackDatabase::class.java,
                    "monitrack.db",
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                    .also { instance = it }
            }
    }
}
