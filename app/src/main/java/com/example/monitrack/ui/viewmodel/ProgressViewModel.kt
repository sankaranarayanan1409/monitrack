package com.example.monitrack.ui.viewmodel

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.monitrack.R
import com.example.monitrack.data.util.Metrics
import com.example.monitrack.data.Profile
import com.example.monitrack.data.entity.BodyMeasurement
import com.example.monitrack.repository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class DateRange(@StringRes val label: Int, val days: Long) {
    WEEK(R.string.range_week, 7),
    FORTNIGHT(R.string.range_fortnight, 14),
    MONTH(R.string.range_month, 30),
    YEAR(R.string.range_year, 365),
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = application.repository

    private val _range = MutableStateFlow(DateRange.WEEK)
    val range: StateFlow<DateRange> = _range.asStateFlow()

    val profile: StateFlow<Profile?> =
        repository.profileFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val measurements: StateFlow<List<BodyMeasurement>> =
        _range.flatMapLatest { range ->
            val end = LocalDate.now()
            repository.measurementsBetween(end.minusDays(range.days), end)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setRange(range: DateRange) {
        _range.value = range
    }

    fun addMeasurement(
        date: LocalDate,
        weightKg: Double?,
        neckCm: Double?,
        waistCm: Double?,
        hipCm: Double?,
    ) = viewModelScope.launch {
        val profile = repository.getProfile()
        val bodyFat = if (profile?.bodyFatEnabled == true && profile.sex != null) {
            Metrics.bodyFatNavy(profile.sex, profile.heightCm, neckCm, waistCm, hipCm)
        } else {
            null
        }
        repository.addMeasurement(
            BodyMeasurement(
                date = date,
                weightKg = weightKg,
                neckCm = neckCm,
                waistCm = waistCm,
                hipCm = hipCm,
                bodyFatPercent = bodyFat,
            ),
        )
    }
}
