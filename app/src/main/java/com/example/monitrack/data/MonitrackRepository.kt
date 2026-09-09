package com.example.monitrack.data

import com.example.monitrack.data.dao.ActivityConfigDao
import com.example.monitrack.data.dao.ActivityDao
import com.example.monitrack.data.dao.MeasurementDao
import com.example.monitrack.data.dao.SessionDao
import com.example.monitrack.data.datastore.ProfileStore
import com.example.monitrack.data.entity.Activity
import com.example.monitrack.data.entity.ActivityConfig
import com.example.monitrack.data.entity.BodyMeasurement
import com.example.monitrack.data.entity.Session
import com.example.monitrack.data.relation.ActivityWithConfig
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** Single data source bridging the DAOs and DataStore to the ViewModels. */
class MonitrackRepository(
    private val sessionDao: SessionDao,
    private val measurementDao: MeasurementDao,
    private val activityConfigDao: ActivityConfigDao,
    private val activityDao: ActivityDao,
    private val profileStore: ProfileStore,
) {
    // Activities (name + icon; source of truth joined with config)
    fun activitiesWithConfig(): Flow<List<ActivityWithConfig>> = activityDao.activitiesWithConfig()
    suspend fun getActivitiesNow(): List<Activity> = activityDao.getAllNow()
    suspend fun getActivity(id: Long): Activity? = activityDao.getById(id)

    // Sessions (keyed by activityId, not name, so a rename never orphans history)
    fun activeSession(activityId: Long): Flow<Session?> = sessionDao.activeSession(activityId)

    fun completedSessions(activityId: Long): Flow<List<Session>> =
        sessionDao.completedSessions(activityId)

    suspend fun completedSessionsNow(activityId: Long): List<Session> =
        sessionDao.completedSessionsNow(activityId)

    suspend fun getSession(id: Long): Session? = sessionDao.getById(id)

    suspend fun startSession(activityId: Long, name: String, startTime: Long): Long =
        sessionDao.insert(Session(activityId = activityId, type = name, startTime = startTime))

    suspend fun stopSession(id: Long, endTime: Long, manuallyAdjusted: Boolean) {
        val session = sessionDao.getById(id) ?: return
        if (!session.isRunning) return
        sessionDao.update(session.copy(endTime = endTime, manuallyAdjusted = manuallyAdjusted))
    }

    /** Discard a running session entirely (nothing is recorded). */
    suspend fun cancelSession(id: Long) {
        val session = sessionDao.getById(id) ?: return
        if (session.isRunning) sessionDao.delete(session)
    }

    // Activity configuration (keyed by activityId, not name)
    fun configFlow(activityId: Long): Flow<ActivityConfig?> = activityConfigDao.configFlow(activityId)
    fun allConfigs(): Flow<List<ActivityConfig>> = activityConfigDao.allConfigs()
    suspend fun getConfig(activityId: Long): ActivityConfig? = activityConfigDao.getConfig(activityId)
    suspend fun getConfigById(id: Int): ActivityConfig? = activityConfigDao.getConfigById(id)
    suspend fun getAllConfigs(): List<ActivityConfig> = activityConfigDao.getAllConfigs()
    suspend fun saveConfig(config: ActivityConfig) = activityConfigDao.upsert(config)
    suspend fun saveConfigs(configs: List<ActivityConfig>) = activityConfigDao.upsert(configs)

    // Profile (DataStore-backed)
    fun profileFlow(): Flow<Profile?> = profileStore.profileFlow
    suspend fun getProfile(): Profile? = profileStore.getProfile()
    suspend fun saveProfile(profile: Profile) = profileStore.save(profile)

    // Measurements
    fun measurementsBetween(start: LocalDate, end: LocalDate): Flow<List<BodyMeasurement>> =
        measurementDao.measurementsBetween(start, end)

    fun latestMeasurement(): Flow<BodyMeasurement?> = measurementDao.latestMeasurement()

    suspend fun addMeasurement(measurement: BodyMeasurement): Long =
        measurementDao.insert(measurement)
}
