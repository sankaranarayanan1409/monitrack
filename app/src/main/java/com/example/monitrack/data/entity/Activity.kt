package com.example.monitrack.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * An activity definition. [ActivityConfig] and `sessions` join to this via `activityId`, which
 * Track/Streaks/reminders query by; the name is the canonical, unique identifier.
 *
 * [icon] is a stable key into [com.example.monitrack.data.util.ActivityIcons], not a drawable
 * resource ID: resource IDs are not stable across builds, so storing one directly causes icons
 * to shuffle after a reinstall that adds or removes drawables.
 */
@Entity(
    tableName = "activities",
    indices = [Index(value = ["name"], unique = true)],
)
data class Activity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String? = null,
) {
    companion object {
        fun defaults(): List<Activity> = listOf(
            Activity(name = "Sleep", icon = "sleep"),
            Activity(name = "Work", icon = "work"),
            Activity(name = "Exercise", icon = "exercise"),
        )
    }
}
