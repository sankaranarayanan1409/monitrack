package com.example.monitrack.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.monitrack.data.entity.Activity
import com.example.monitrack.data.relation.ActivityWithConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Upsert
    suspend fun upsert(activities: List<Activity>)

    @Query("SELECT * FROM activities ORDER BY name")
    fun allActivities(): Flow<List<Activity>>

    @Query("SELECT * FROM activities ORDER BY name")
    suspend fun getAllNow(): List<Activity>

    @Query("SELECT COUNT(*) FROM activities")
    suspend fun count(): Int

    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getById(id: Long): Activity?

    /** Each activity joined with its config, ordered for a stable carousel/list. */
    @Transaction
    @Query("SELECT * FROM activities ORDER BY id")
    fun activitiesWithConfig(): Flow<List<ActivityWithConfig>>
}
