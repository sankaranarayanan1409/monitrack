package com.example.monitrack.ui.track

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.monitrack.R
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId

/**
 * Shown on stop. Lets the user adjust the stop time; editing it flags the session as
 * manually logged rather than tracked in real time.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustStopTimeDialog(
    activityLabel: String,
    startTime: Long,
    proposedEndTime: Long,
    onConfirm: (endTime: Long, manuallyAdjusted: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val zone = ZoneId.systemDefault()
    val proposed = remember(proposedEndTime) {
        Instant.ofEpochMilli(proposedEndTime).atZone(zone)
    }
    val timeState = rememberTimePickerState(
        initialHour = proposed.hour,
        initialMinute = proposed.minute,
        is24Hour = true,
    )

    // The picker only has minute resolution. Only override the real stop time when the user
    // actually changed the hour/minute; otherwise keep the exact proposed time (seconds intact).
    val edited by remember {
        derivedStateOf { timeState.hour != proposed.hour || timeState.minute != proposed.minute }
    }
    val endTime by remember {
        derivedStateOf {
            if (edited) {
                proposed.withHour(timeState.hour).withMinute(timeState.minute)
                    .withSecond(0).withNano(0)
                    .toInstant().toEpochMilli()
            } else {
                proposedEndTime
            }
        }
    }
    var currentTime = remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime.longValue = System.currentTimeMillis()
            delay(1000)
        }
    }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.adjust_stop_title, activityLabel)) },
        text = {
            Column {
                Text(stringResource(R.string.adjust_stop_message))
                Spacer(Modifier.height(16.dp))
                TimePicker(state = timeState)
            }
        },
        confirmButton = {
            TextButton(
                enabled = (endTime > startTime) && (endTime <= currentTime.longValue),
                onClick = { onConfirm(endTime, edited) },
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}
