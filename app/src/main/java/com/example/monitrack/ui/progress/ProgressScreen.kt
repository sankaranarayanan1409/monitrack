package com.example.monitrack.ui.progress

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.monitrack.R
import com.example.monitrack.data.entity.BodyMeasurement
import com.example.monitrack.data.enums.Sex
import com.example.monitrack.ui.theme.ExerciseAccent
import com.example.monitrack.ui.theme.Sage
import com.example.monitrack.ui.theme.SleepAccent
import com.example.monitrack.ui.theme.WaistAccent
import com.example.monitrack.ui.theme.WorkAccent
import com.example.monitrack.ui.viewmodel.DateRange
import com.example.monitrack.ui.viewmodel.ProgressViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(viewModel: ProgressViewModel = viewModel()) {
    val range by viewModel.range.collectAsState()
    val measurements by viewModel.measurements.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val bodyFatEnabled = profile?.bodyFatEnabled == true
    var adding by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text(stringResource(R.string.nav_progress)) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { adding = true }) {
                Text(stringResource(R.string.add_measurement))
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DateRange.entries.forEach { option ->
                    FilterChip(
                        selected = option == range,
                        onClick = { viewModel.setRange(option) },
                        label = { Text(stringResource(option.label)) },
                    )
                }
            }

            val emptyText = stringResource(R.string.no_measurements)
            LineChart(
                title = stringResource(R.string.chart_weight),
                entries = measurements.toEntries(range) { it.weightKg },
                lineColor = ExerciseAccent,
                emptyText = emptyText,
            )
            if (bodyFatEnabled) {
                LineChart(
                    title = stringResource(R.string.chart_body_fat),
                    entries = measurements.toEntries(range) { it.bodyFatPercent },
                    lineColor = WorkAccent,
                    emptyText = emptyText,
                )
                LineChart(
                    title = stringResource(R.string.chart_waist),
                    entries = measurements.toEntries(range) { it.waistCm },
                    lineColor = WaistAccent,
                    emptyText = emptyText,
                )
                if (profile?.sex == Sex.FEMALE) {
                    LineChart(
                        title = stringResource(R.string.chart_hip),
                        entries = measurements.toEntries(range) { it.hipCm },
                        lineColor = SleepAccent,
                        emptyText = emptyText,
                    )
                }
            }
            LineChart(
                title = stringResource(R.string.chart_neck),
                entries = measurements.toEntries(range) { it.neckCm },
                lineColor = Sage,
                emptyText = emptyText,
            )
        }
    }

    if (adding) {
        AddMeasurementDialog(
            bodyFatEnabled = bodyFatEnabled,
            sex = profile?.sex,
            onConfirm = { date, weight, neck, waist, hip ->
                viewModel.addMeasurement(date, weight, neck, waist, hip)
                adding = false
            },
            onDismiss = { adding = false },
        )
    }
}

/** Maps measurements with a non-null metric onto chart positions across the selected range. */
private fun List<BodyMeasurement>.toEntries(
    range: DateRange,
    selector: (BodyMeasurement) -> Double?,
): List<ChartEntry> {
    val startDay = LocalDate.now().minusDays(range.days).toEpochDay()
    val span = range.days.toFloat()
    return mapNotNull { measurement ->
        selector(measurement)?.let { value ->
            val fraction = ((measurement.date.toEpochDay() - startDay) / span).coerceIn(0f, 1f)
            ChartEntry(dayFraction = fraction, value = value.toFloat())
        }
    }
}
