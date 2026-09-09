package com.example.monitrack.ui.track

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.monitrack.R
import com.example.monitrack.ui.viewmodel.ActivityUiState
import com.example.monitrack.ui.viewmodel.TrackViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackScreen(
    onOpenSettings: () -> Unit,
    onOpenHistory: (Long, String) -> Unit,
    viewModel: TrackViewModel = viewModel(),
) {
    val activities by viewModel.activities.collectAsState()
    val bodyComposition by viewModel.bodyComposition.collectAsState()
    var stopping by remember { mutableStateOf<ActivityUiState?>(null) }
    var cancelling by remember { mutableStateOf<ActivityUiState?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.nav_track)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            painterResource(R.drawable.ic_settings),
                            contentDescription = stringResource(R.string.settings),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        val pagerState = rememberPagerState(pageCount = { activities.size })
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            bodyComposition?.let { state ->
                BodyCompositionCard(
                    state = state,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            if (activities.isNotEmpty()) {
                HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                    val state = activities[page]
                    ActivityPage(
                        state = state,
                        onStart = { viewModel.start(state.activityId, state.name) },
                        onStop = { stopping = state },
                        onCancel = { cancelling = state },
                        onOpenHistory = { onOpenHistory(state.activityId, state.name) },
                    )
                }
                PagerDots(count = activities.size, selected = pagerState.currentPage)
            }
        }
    }

    stopping?.let { state ->
        val sessionId = state.runningSessionId
        val startTime = state.runningSince
        if (sessionId != null && startTime != null) {
            AdjustStopTimeDialog(
                activityLabel = state.name,
                startTime = startTime,
                proposedEndTime = System.currentTimeMillis(),
                onConfirm = { endTime, manuallyAdjusted ->
                    viewModel.stop(sessionId, endTime, manuallyAdjusted)
                    stopping = null
                },
                onDismiss = { stopping = null },
            )
        }
    }

    cancelling?.let { state ->
        state.runningSessionId?.let { sessionId ->
            CancelSessionDialog(
                onConfirm = { viewModel.cancel(sessionId); cancelling = null },
                onDismiss = { cancelling = null },
            )
        }
    }
}

@Composable
private fun PagerDots(count: Int, selected: Int) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(count) { index ->
            val color = if (index == selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            }
            Surface(color = color, shape = CircleShape, modifier = Modifier.padding(horizontal = 4.dp)) {
                Box(Modifier.size(8.dp))
            }
        }
    }
}
