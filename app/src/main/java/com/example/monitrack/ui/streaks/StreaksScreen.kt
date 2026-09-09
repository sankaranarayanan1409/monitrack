package com.example.monitrack.ui.streaks

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.monitrack.R
import com.example.monitrack.ui.viewmodel.StreaksViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreaksScreen(viewModel: StreaksViewModel = viewModel()) {
    val streaks by viewModel.streaks.collectAsState()

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text(stringResource(R.string.nav_streaks)) }) },
    ) { innerPadding ->
        if (streaks.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.streaks_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            streaks.forEach { item ->
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(item.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(R.string.streak_current, item.streak),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        ContributionGrid(
                            metDays = item.metDays,
                            color = Color(item.streakColor),
                            today = LocalDate.now(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContributionGrid(
    metDays: Set<LocalDate>,
    color: Color,
    today: LocalDate,
    modifier: Modifier = Modifier,
) {
    val cell = 12.dp
    val gap = 3.dp
    val emptyColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)

    // Align the grid start to the Monday on/before one year ago.
    val yearAgo = today.minusWeeks(52)
    val firstMonday = yearAgo.minusDays((yearAgo.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())
    val totalDays = ChronoUnit.DAYS.between(firstMonday, today).toInt() + 1
    val weeks = (totalDays + 6) / 7

    Box(modifier.horizontalScroll(rememberScrollState())) {
        Canvas(Modifier.size(cell * weeks + gap * (weeks - 1), cell * 7 + gap * 6)) {
            val cellPx = cell.toPx()
            val stepPx = cellPx + gap.toPx()
            for (offset in 0 until totalDays) {
                val date = firstMonday.plusDays(offset.toLong())
                val col = offset / 7
                val row = date.dayOfWeek.value - DayOfWeek.MONDAY.value
                drawRect(
                    color = if (date in metDays) color else emptyColor,
                    topLeft = Offset(col * stepPx, row * stepPx),
                    size = Size(cellPx, cellPx),
                )
            }
        }
    }
}
