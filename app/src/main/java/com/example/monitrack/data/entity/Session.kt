package com.example.monitrack.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = Activity::class,
            parentColumns = ["id"],
            childColumns = ["activityId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("activityId")],
)
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** FK to [Activity]. The source of truth for which activity this session belongs to. */
    val activityId: Long = 0,
    /**
     * The activity's name at the time this session was logged. Display-only history label —
     * a later rename of the [Activity] does not change it, and no query joins on it, so
     * renaming never orphans existing sessions.
     */
    val type: String,
    val startTime: Long,
    val endTime: Long? = null,
    /** True when the user edited the stop time on stop, i.e. the session was manually logged. */
    val manuallyAdjusted: Boolean = false,
) {
    val isRunning: Boolean get() = endTime == null
    val durationMs: Long? get() = endTime?.let { it - startTime }
}
