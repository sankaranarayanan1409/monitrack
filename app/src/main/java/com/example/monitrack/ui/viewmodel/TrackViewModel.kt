package com.example.monitrack.ui.viewmodel

import android.app.Application
import androidx.annotation.DrawableRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.monitrack.data.entity.ActivityConfig
import com.example.monitrack.data.enums.Sex
import com.example.monitrack.data.relation.ActivityWithConfig
import com.example.monitrack.data.util.Metrics
import com.example.monitrack.reminder.ReminderScheduler
import com.example.monitrack.repository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class ActivityUiState(
    val name: String,
    @DrawableRes val icon: Int?,
    val config: ActivityConfig?,
    val runningSessionId: Long?,
    val runningSince: Long?,
    val todayTotalMs: Long,
    val streak: Int?,
) {
    val isRunning: Boolean get() = runningSessionId != null
}

/** Latest body metrics, shown as gauges above the activity pager. Null when nothing is logged. */
data class BodyCompositionUiState(
    val bmi: Double?,
    val bodyFatPercent: Double?,
    val sex: Sex?,
)

class TrackViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = application.repository
    private val zone: ZoneId = ZoneId.systemDefault()

    val activities: StateFlow<List<ActivityUiState>> =
        repository.activitiesWithConfig()
            .flatMapLatest { list ->
                if (list.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    combine(list.map { stateFor(it) }) { it.toList() }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val bodyComposition: StateFlow<BodyCompositionUiState?> = combine(
        repository.latestMeasurement(),
        repository.profileFlow(),
    ) { measurement, profile ->
        val bmi = Metrics.bmi(measurement?.weightKg, profile?.heightCm)
        val bodyFat = measurement?.bodyFatPercent
        if (bmi == null && bodyFat == null) null
        else BodyCompositionUiState(bmi = bmi, bodyFatPercent = bodyFat, sex = profile?.sex)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private fun stateFor(item: ActivityWithConfig) = combine(
        repository.activeSession(item.activity.name),
        repository.completedSessions(item.activity.name),
    ) { active, completed ->
        val today = LocalDate.now(zone)
        val config = item.config
        ActivityUiState(
            name = item.activity.name,
            icon = item.activity.icon,
            config = config,
            runningSessionId = active?.id,
            runningSince = active?.startTime,
            todayTotalMs = Metrics.dailyTotalMs(completed, today, zone),
            streak = config?.takeIf { it.streakEnabled }?.let { Metrics.streak(completed, it, today, zone) },
        )
    }

    fun start(name: String) = viewModelScope.launch {
        val id = repository.startSession(name, System.currentTimeMillis())
        repository.getSession(id)?.let { ReminderScheduler.schedule(getApplication(), repository, it) }
    }

    fun stop(sessionId: Long, endTime: Long, manuallyAdjusted: Boolean) = viewModelScope.launch {
        repository.stopSession(sessionId, endTime, manuallyAdjusted)
        ReminderScheduler.cancel(getApplication(), sessionId)
    }

    fun cancel(sessionId: Long) = viewModelScope.launch {
        repository.cancelSession(sessionId)
        ReminderScheduler.cancel(getApplication(), sessionId)
    }
}
