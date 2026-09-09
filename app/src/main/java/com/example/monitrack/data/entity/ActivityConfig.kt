package com.example.monitrack.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(
    tableName = "activity_config",
    foreignKeys = [
        ForeignKey(
            entity = Activity::class,
            parentColumns = ["id"],
            childColumns = ["activityId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("activityId")],
)
data class ActivityConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: String,
    val targetMinutes: Int,
    /** Evaluate the target against the summed daily total rather than a single session. */
    val aggregatesDaily: Boolean,
    /** A session/day below target is shown as "incomplete". */
    val requiresTargetForCompletion: Boolean,
    val dailyNudgeEnabled: Boolean,
    val nudgeTime: LocalTime,
    val overrunAlertEnabled: Boolean,
    val streakEnabled: Boolean,
    /** ARGB color for the streak contribution grid. */
    val streakColor: Int,
    /** FK to [Activity], set once at setup by matching [type] to [Activity.name]. */
    val activityId: Long? = null,
) {
    val targetMs: Long get() = targetMinutes * 60_000L

    companion object {
        /** Seed values shown as defaults in first-run setup. */
        fun defaults(): List<ActivityConfig> = listOf(
            ActivityConfig(
                type = "Sleep",
                targetMinutes = 480,
                aggregatesDaily = true,
                requiresTargetForCompletion = true,
                dailyNudgeEnabled = true,
                nudgeTime = LocalTime.of(9, 0),
                overrunAlertEnabled = false,
                streakEnabled = true,
                streakColor = 0xFF8A7CA8.toInt(),
            ),
            ActivityConfig(
                type = "Work",
                targetMinutes = 480,
                aggregatesDaily = false,
                requiresTargetForCompletion = false,
                dailyNudgeEnabled = false,
                nudgeTime = LocalTime.of(18, 0),
                overrunAlertEnabled = true,
                streakEnabled = false,
                streakColor = 0xFFC8873C.toInt(),
            ),
            ActivityConfig(
                type = "Exercise",
                targetMinutes = 60,
                aggregatesDaily = false,
                requiresTargetForCompletion = true,
                dailyNudgeEnabled = true,
                nudgeTime = LocalTime.of(20, 0),
                overrunAlertEnabled = true,
                streakEnabled = true,
                streakColor = 0xFFB5643C.toInt(),
            ),
        )
    }
}