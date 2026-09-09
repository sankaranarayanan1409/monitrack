package com.example.monitrack.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.monitrack.data.util.Metrics
import com.example.monitrack.repository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId

data class StreakUi(
    val name: String,
    val streakColor: Int,
    val metDays: Set<LocalDate>,
    val streak: Int,
)

class StreaksViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = application.repository
    private val zone: ZoneId = ZoneId.systemDefault()

    val streaks: StateFlow<List<StreakUi>> =
        repository.activitiesWithConfig()
            .flatMapLatest { list ->
                val streakable = list.filter { it.config?.streakEnabled == true }
                if (streakable.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    combine(
                        streakable.map { item ->
                            repository.completedSessions(item.activity.name)
                                .map { sessions -> item to sessions }
                        },
                    ) { pairs ->
                        val today = LocalDate.now(zone)
                        pairs.map { (item, sessions) ->
                            val config = item.config!!
                            StreakUi(
                                name = item.activity.name,
                                streakColor = config.streakColor,
                                metDays = Metrics.metDays(sessions, config, zone),
                                streak = Metrics.streak(sessions, config, today, zone),
                            )
                        }
                    }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
