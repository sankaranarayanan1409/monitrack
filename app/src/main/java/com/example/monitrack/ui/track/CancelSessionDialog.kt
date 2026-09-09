package com.example.monitrack.ui.track

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.monitrack.R

/** Confirmation before discarding an in-progress session (nothing gets logged). */
@Composable
fun CancelSessionDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.cancel_session_title)) },
        text = { Text(stringResource(R.string.cancel_session_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.discard)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.keep_tracking)) }
        },
    )
}
