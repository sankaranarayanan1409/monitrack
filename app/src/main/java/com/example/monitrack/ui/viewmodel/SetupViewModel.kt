package com.example.monitrack.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.monitrack.data.Profile
import com.example.monitrack.data.entity.ActivityConfig
import com.example.monitrack.reminder.DailyNudgeScheduler
import com.example.monitrack.repository
import kotlinx.coroutines.launch

class SetupViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = application.repository

    fun save(configs: List<ActivityConfig>, profile: Profile) = viewModelScope.launch {
        // Link each config to its Activity (matched by name) so the join is populated.
        val idByName = repository.getActivitiesNow().associate { it.name to it.id }
        val linked = configs.map { it.copy(activityId = idByName[it.type]) }
        repository.saveConfigs(linked)
        repository.saveProfile(profile)
        DailyNudgeScheduler.scheduleAll(getApplication(), linked)
    }
}
