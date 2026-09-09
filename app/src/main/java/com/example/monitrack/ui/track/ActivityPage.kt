package com.example.monitrack.ui.track

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.monitrack.R
import com.example.monitrack.formatDuration
import com.example.monitrack.ui.theme.accentFor
import com.example.monitrack.ui.viewmodel.ActivityUiState
import kotlinx.coroutines.delay

@Composable
fun ActivityPage(
    state: ActivityUiState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onCancel: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = accentFor(state.name)
    val config = state.config
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            state.icon?.takeIf { it != 0 }?.let { iconRes ->
                Surface(color = accent.copy(alpha = 0.15f), shape = CircleShape) {
                    Icon(
                        painterResource(iconRes),
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.padding(12.dp).size(28.dp),
                    )
                }
                Spacer(Modifier.size(12.dp))
            }
            Text(
                state.name,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onOpenHistory) {
                Icon(painterResource(R.drawable.ic_track), contentDescription = stringResource(R.string.history))
            }
        }

        if (config != null) {
            Text(
                stringResource(R.string.target_label, formatDuration(config.targetMs)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val fraction = (state.todayTotalMs.toFloat() / config.targetMs).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { fraction },
                color = accent,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                stringResource(
                    R.string.today_progress,
                    formatDuration(state.todayTotalMs),
                    formatDuration(config.targetMs),
                ),
                style = MaterialTheme.typography.bodySmall,
            )
        }

        state.streak?.takeIf { it > 0 }?.let { streak ->
            Surface(color = accent.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
                Text(
                    stringResource(R.string.streak_label, streak),
                    color = accent,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        if (state.isRunning && state.runningSince != null) {
            RunningTimer(state.runningSince, accent = accent)
            OutlinedButton(onClick = onStop, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.stop))
            }
            OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.cancel_session))
            }
        } else {
            Button(
                onClick = onStart,
                enabled = config != null,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.start)) }
        }
    }
}

@Composable
private fun RunningTimer(runningSince: Long, accent: androidx.compose.ui.graphics.Color) {
    var now by remember(runningSince) { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(runningSince) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    Text(
        text = formatDuration((now - runningSince).coerceAtLeast(0)),
        style = MaterialTheme.typography.displayMedium,
        color = accent,
        textAlign = TextAlign.Center,
    )
}
