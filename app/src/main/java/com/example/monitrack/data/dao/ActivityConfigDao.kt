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

    @Query("SELECT * FROM activity_config WHERE type = :type")
    fun configFlow(type: String): Flow<ActivityConfig?>

    @Query("SELECT * FROM activity_config")
    fun allConfigs(): Flow<List<ActivityConfig>>

    @Query("SELECT * FROM activity_config WHERE type = :type")
    suspend fun getConfig(type: String): ActivityConfig?

    @Query("SELECT * FROM activity_config")
    suspend fun getAllConfigs(): List<ActivityConfig>
}
