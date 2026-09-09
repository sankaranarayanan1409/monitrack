package com.example.monitrack.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.monitrack.R
import com.example.monitrack.data.entity.ActivityConfig
import com.example.monitrack.data.entity.Session
import com.example.monitrack.formatDuration
import com.example.monitrack.repository
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(type: String, onBack: () -> Unit) {
    val repository = LocalContext.current.repository
    val sessions by remember(type) { repository.completedSessions(type) }.collectAsState(initial = emptyList())
    val config by remember(type) { repository.configFlow(type) }.collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title, type)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_back), contentDescription = stringResource(R.string.cancel))
                    }
                },
            )
        },
    ) { innerPadding ->
        if (sessions.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.history_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(sessions, key = { it.id }) { session ->
                SessionRow(session, config)
            }
        }
    }
}

@Composable
private fun SessionRow(session: Session, config: ActivityConfig?) {
    val zone = ZoneId.systemDefault()
    val start = Instant.ofEpochMilli(session.startTime).atZone(zone)
    val incomplete = config != null &&
        config.requiresTargetForCompletion &&
        !config.aggregatesDaily &&
        (session.durationMs ?: 0) < config.targetMs

    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(start.format(DATE_FORMAT), style = MaterialTheme.typography.bodyMedium)
                Text(
                    session.durationMs?.let { formatDuration(it) } ?: "",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (incomplete) Badge(stringResource(R.string.incomplete))
                if (session.manuallyAdjusted) Badge(stringResource(R.string.manually_logged))
            }
        }
    }
}

@Composable
private fun Badge(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

private val DATE_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
