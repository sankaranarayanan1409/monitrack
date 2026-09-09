package com.example.monitrack.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.monitrack.data.entity.Activity
import com.example.monitrack.data.entity.ActivityConfig

/** An activity joined with its configuration (one config per activity). */
data class ActivityWithConfig(
    @Embedded val activity: Activity,
    @Relation(parentColumn = "id", entityColumn = "activityId")
    val config: ActivityConfig?,
)
