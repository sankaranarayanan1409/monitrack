package com.example.monitrack.ui.progress

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import com.example.monitrack.R
import com.example.monitrack.data.enums.Sex
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMeasurementDialog(
    bodyFatEnabled: Boolean,
    sex: Sex?,
    onConfirm: (date: LocalDate, weight: Double?, neck: Double?, waist: Double?, hip: Double?) -> Unit,
    onDismiss: () -> Unit,
) {
    var date by remember { mutableStateOf(LocalDate.now()) }
    var weight by remember { mutableStateOf("") }
    var neck by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    var hip by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val weightValue = weight.toDoubleOrNull()
    val neckValue = neck.toDoubleOrNull()
    val waistValue = waist.toDoubleOrNull()
    val hipValue = hip.toDoubleOrNull()
    // The US Navy formula needs hip for females, so a female body-fat entry (waist filled)
    // must include hip; weight/neck-only logging stays unaffected.
    val hipRequiredMissing = bodyFatEnabled && sex == Sex.FEMALE && waistValue != null && hipValue == null
    val canSave = listOfNotNull(weightValue, neckValue, waistValue, hipValue).isNotEmpty() &&
        !hipRequiredMissing

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_measurement)) },
        text = {
            Column {
                TextButton(onClick = { showDatePicker = true }) {
                    Text(stringResource(R.string.measurement_date) + ": " + date.toString())
                }
                NumberField(weight, { weight = it }, R.string.measurement_weight)
                NumberField(neck, { neck = it }, R.string.measurement_neck)
                if (bodyFatEnabled) {
                    NumberField(waist, { waist = it }, R.string.measurement_waist)
                    if (sex == Sex.FEMALE) {
                        NumberField(hip, { hip = it }, R.string.measurement_hip)
                        if (hipRequiredMissing) {
                            Text(
                                text = stringResource(R.string.hip_required_female),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = { onConfirm(date, weightValue, neckValue, waistValue, hipValue) },
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun NumberField(value: String, onValueChange: (String) -> Unit, labelRes: Int) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(labelRes)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
}
