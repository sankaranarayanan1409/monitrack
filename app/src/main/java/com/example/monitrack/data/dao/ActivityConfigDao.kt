package com.example.monitrack.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.monitrack.data.entity.ActivityConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityConfigDao {
    @Upsert
    suspend fun upsert(configs: List<ActivityConfig>)

    @Upsert
    suspend fun upsert(config: ActivityConfig)

    @Query("SELECT * FROM activity_config WHERE activityId = :activityId")
    fun configFlow(activityId: Long): Flow<ActivityConfig?>

    @Query("SELECT * FROM activity_config")
    fun allConfigs(): Flow<List<ActivityConfig>>

    @Query("SELECT * FROM activity_config WHERE activityId = :activityId")
    suspend fun getConfig(activityId: Long): ActivityConfig?

    @Query("SELECT * FROM activity_config WHERE id = :id")
    suspend fun getConfigById(id: Int): ActivityConfig?

    @Query("SELECT * FROM activity_config")
    suspend fun getAllConfigs(): List<ActivityConfig>
}
