package com.example.monitrack.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.monitrack.repository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class RootViewModel(application: Application) : AndroidViewModel(application) {
    // null = still loading; false = needs setup; true = ready.
    val setupComplete: StateFlow<Boolean?> =
        application.repository.profileFlow()
            .map { it?.setupComplete ?: false }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
