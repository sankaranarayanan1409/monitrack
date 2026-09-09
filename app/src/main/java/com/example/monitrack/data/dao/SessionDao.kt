package com.example.monitrack.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.monitrack.data.entity.Session
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert
    suspend fun insert(session: Session): Long

    @Update
    suspend fun update(session: Session)

    @Delete
    suspend fun delete(session: Session)

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getById(id: Long): Session?

    @Query("SELECT * FROM sessions WHERE type = :type AND endTime IS NULL LIMIT 1")
    fun activeSession(type: String): Flow<Session?>

    @Query("SELECT * FROM sessions WHERE type = :type AND endTime IS NOT NULL ORDER BY startTime DESC")
    fun completedSessions(type: String): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE type = :type AND endTime IS NOT NULL")
    suspend fun completedSessionsNow(type: String): List<Session>
}