package com.example.monitrack.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.monitrack.R
import com.example.monitrack.data.enums.Sex
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Body-fat opt-in plus the one-time profile the US Navy formula needs. */
@Composable
fun ProfileSection(
    bodyFatEnabled: Boolean,
    onBodyFatEnabledChange: (Boolean) -> Unit,
    sex: Sex?,
    onSexChange: (Sex) -> Unit,
    height: String,
    onHeightChange: (String) -> Unit,
    dateOfBirth: LocalDate?,
    onDateOfBirthChange: (LocalDate) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LabeledSwitch(
            label = stringResource(R.string.bodyfat_enable),
            checked = bodyFatEnabled,
            onCheckedChange = onBodyFatEnabledChange,
        )
        if (bodyFatEnabled) {
            Text(
                stringResource(R.string.bodyfat_privacy),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(stringResource(R.string.profile_sex), style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = sex == Sex.MALE,
                    onClick = { onSexChange(Sex.MALE) },
                    label = { Text(stringResource(R.string.sex_male)) },
                )
                FilterChip(
                    selected = sex == Sex.FEMALE,
                    onClick = { onSexChange(Sex.FEMALE) },
                    label = { Text(stringResource(R.string.sex_female)) },
                )
            }
            OutlinedTextField(
                value = height,
                onValueChange = onHeightChange,
                label = { Text(stringResource(R.string.profile_height)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            DateOfBirthField(dateOfBirth, onDateOfBirthChange)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateOfBirthField(value: LocalDate?, onChange: (LocalDate) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { showPicker = true }) {
        Text(stringResource(R.string.profile_dob) + ": " + (value?.toString() ?: "—"))
    }
    if (showPicker) {
        val initialMillis = (value ?: LocalDate.of(1990, 1, 1))
            .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        onChange(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    showPicker = false
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text(stringResource(R.string.cancel)) }
            },
        ) { DatePicker(state = state) }
    }
}
