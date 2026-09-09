package com.example.monitrack.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.monitrack.R
import com.example.monitrack.ui.history.HistoryScreen
import com.example.monitrack.ui.progress.ProgressScreen
import com.example.monitrack.ui.settings.SettingsScreen
import com.example.monitrack.ui.setup.SetupScreen
import com.example.monitrack.ui.streaks.StreaksScreen
import com.example.monitrack.ui.track.TrackScreen
import com.example.monitrack.ui.viewmodel.RootViewModel

@Composable
fun MonitrackApp(viewModel: RootViewModel = viewModel()) {
    when (viewModel.setupComplete.collectAsState().value) {
        null -> Unit // brief loading; keep the splash-like empty frame
        false -> SetupScreen()
        true -> MainScaffold()
    }
}

private sealed interface Overlay {
    data object Settings : Overlay
    data class History(val activityId: Long, val name: String) : Overlay
}

@Composable
private fun MainScaffold() {
    // Overlay routing is in-memory: on rotation we fall back to the main tabs, which is fine.
    var overlay by androidx.compose.runtime.remember { mutableStateOf<Overlay?>(null) }

    when (val current = overlay) {
        Overlay.Settings -> SettingsScreen(onBack = { overlay = null })
        is Overlay.History -> HistoryScreen(
            activityId = current.activityId,
            name = current.name,
            onBack = { overlay = null },
        )
        null -> {
            var destination by rememberSaveable { mutableStateOf(Destination.TRACK) }
            NavigationSuiteScaffold(
                navigationSuiteItems = {
                    Destination.entries.forEach { d ->
                        item(
                            icon = { Icon(painterResource(d.icon), contentDescription = stringResource(d.label)) },
                            label = { Text(stringResource(d.label)) },
                            selected = d == destination,
                            onClick = { destination = d },
                        )
                    }
                },
            ) {
                when (destination) {
                    Destination.TRACK -> TrackScreen(
                        onOpenSettings = { overlay = Overlay.Settings },
                        onOpenHistory = { activityId, name -> overlay = Overlay.History(activityId, name) },
                    )
                    Destination.PROGRESS -> ProgressScreen()
                    Destination.STREAKS -> StreaksScreen()
                }
            }
        }
    }
}
