package com.example.monitrack.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.monitrack.data.Profile
import com.example.monitrack.data.entity.ActivityConfig
import com.example.monitrack.reminder.DailyNudgeScheduler
import com.example.monitrack.repository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = application.repository

    val configs: StateFlow<List<ActivityConfig>> =
        repository.allConfigs().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val profile: StateFlow<Profile?> =
        repository.profileFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun save(configs: List<ActivityConfig>, profile: Profile) = viewModelScope.launch {
        repository.saveConfigs(configs)
        repository.saveProfile(profile)
        DailyNudgeScheduler.scheduleAll(getApplication(), configs)
    }
}
