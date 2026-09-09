package com.example.monitrack.data.entity

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.monitrack.R

/**
 * An activity definition. Scaffold for user-definable activities — [ActivityConfig] joins to
 * this via `activityId`. Not yet wired into the app (Track/Streaks still run off config); the
 * name is the canonical, unique identifier.
 */
@Entity(
    tableName = "activities",
    indices = [Index(value = ["name"], unique = true)],
)
data class Activity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @DrawableRes val icon: Int? = null,
) {
    companion object {
        fun defaults(): List<Activity> = listOf(
            Activity(name = "Sleep", icon = R.drawable.ic_sleep),
            Activity(name = "Work", icon = R.drawable.ic_work),
            Activity(name = "Exercise", icon = R.drawable.ic_exercise),
        )
    }
}
