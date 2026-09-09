package com.example.monitrack.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val startTime: Long,
    val endTime: Long? = null,
    /** True when the user edited the stop time on stop, i.e. the session was manually logged. */
    val manuallyAdjusted: Boolean = false,
) {
    val isRunning: Boolean get() = endTime == null
    val durationMs: Long? get() = endTime?.let { it - startTime }
}