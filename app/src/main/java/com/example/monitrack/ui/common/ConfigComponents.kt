package com.example.monitrack.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.monitrack.R
import com.example.monitrack.data.entity.ActivityConfig
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** Warm-family palette users pick streak colors from (no color-picker dependency). */
val StreakSwatches: List<Int> = listOf(
    0xFF8A7CA8.toInt(), // indigo
    0xFFC8873C.toInt(), // amber
    0xFFB5643C.toInt(), // terracotta
    0xFF7A8B6F.toInt(), // sage
    0xFFB56576.toInt(), // rose
    0xFF4C8577.toInt(), // teal
    0xFFD9A441.toInt(), // gold
)

/** Full editor for one activity's configuration, reused by Setup and Settings. */
@Composable
fun ActivityConfigCard(config: ActivityConfig, onChange: (ActivityConfig) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(config.type, style = MaterialTheme.typography.titleMedium)

            Text(stringResource(R.string.config_target), style = MaterialTheme.typography.labelMedium)
            DurationPicker(config.targetMinutes) { onChange(config.copy(targetMinutes = it)) }

            LabeledSwitch(
                label = stringResource(R.string.config_aggregates_daily),
                checked = config.aggregatesDaily,
                onCheckedChange = { onChange(config.copy(aggregatesDaily = it)) },
            )
            LabeledSwitch(
                label = stringResource(R.string.config_requires_target),
                checked = config.requiresTargetForCompletion,
                onCheckedChange = { onChange(config.copy(requiresTargetForCompletion = it)) },
            )
            LabeledSwitch(
                label = stringResource(R.string.config_overrun),
                checked = config.overrunAlertEnabled,
                onCheckedChange = { onChange(config.copy(overrunAlertEnabled = it)) },
            )
            LabeledSwitch(
                label = stringResource(R.string.config_daily_nudge),
                checked = config.dailyNudgeEnabled,
                onCheckedChange = { onChange(config.copy(dailyNudgeEnabled = it)) },
            )
            if (config.dailyNudgeEnabled) {
                NudgeTimeField(config.nudgeTime) { onChange(config.copy(nudgeTime = it)) }
            }
            LabeledSwitch(
                label = stringResource(R.string.config_streak),
                checked = config.streakEnabled,
                onCheckedChange = { onChange(config.copy(streakEnabled = it)) },
            )
            if (config.streakEnabled) {
                Text(stringResource(R.string.config_streak_color), style = MaterialTheme.typography.labelMedium)
                ColorSwatchPicker(config.streakColor) { onChange(config.copy(streakColor = it)) }
            }
        }
    }
}

@Composable
fun DurationPicker(totalMinutes: Int, onChange: (Int) -> Unit) {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = hours.toString(),
            onValueChange = { onChange((it.toIntOrNull() ?: 0) * 60 + minutes) },
            label = { Text(stringResource(R.string.hours_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(120.dp),
        )
        OutlinedTextField(
            value = minutes.toString(),
            onValueChange = { onChange(hours * 60 + (it.toIntOrNull() ?: 0).coerceIn(0, 59)) },
            label = { Text(stringResource(R.string.minutes_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(120.dp),
        )
    }
}

@Composable
fun NudgeTimeField(time: LocalTime, onChange: (LocalTime) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { showPicker = true }) {
        Text(stringResource(R.string.config_nudge_time) + ": " + time.format(TIME_FORMAT))
    }
    if (showPicker) {
        TimePickerDialog(
            initial = time,
            onConfirm = { onChange(it); showPicker = false },
            onDismiss = { showPicker = false },
        )
    }
}

@Composable
fun ColorSwatchPicker(selectedArgb: Int, onChange: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StreakSwatches.forEach { argb ->
            val selected = argb == selectedArgb
            Spacer(
                Modifier
                    .size(32.dp)
                    .border(
                        BorderStroke(if (selected) 3.dp else 1.dp, MaterialTheme.colorScheme.onSurface),
                        CircleShape,
                    )
                    .padding(3.dp)
                    .background(Color(argb), CircleShape)
                    .clickable { onChange(argb) },
            )
        }
    }
}

@Composable
fun LabeledSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initial: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(initial.hour, initial.minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
        text = { TimePicker(state = state) },
    )
}

private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
