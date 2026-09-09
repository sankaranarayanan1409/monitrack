package com.example.monitrack.ui.track

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.monitrack.R
import com.example.monitrack.data.enums.Sex
import com.example.monitrack.ui.theme.AlertRed
import com.example.monitrack.ui.theme.Amber
import com.example.monitrack.ui.theme.HealthyGreen
import com.example.monitrack.ui.viewmodel.BodyCompositionUiState

private val BmiHealthy = 18.5f..24.9f
private val BmiAxis = 15f..35f

private val MaleFatHealthy = 8f..19f
private val FemaleFatHealthy = 21f..32f
private val FatAxis = 5f..45f

/** Latest BMI and body-fat readings shown against their healthy bands. */
@Composable
fun BodyCompositionCard(state: BodyCompositionUiState, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                stringResource(R.string.body_composition),
                style = MaterialTheme.typography.titleMedium,
            )
            state.bmi?.let { bmi ->
                MetricGauge(
                    label = stringResource(R.string.metric_bmi),
                    value = bmi.toFloat(),
                    valueText = "%.1f".format(bmi),
                    healthy = BmiHealthy,
                    axis = BmiAxis,
                )
            }
            state.bodyFatPercent?.let { fat ->
                MetricGauge(
                    label = stringResource(R.string.metric_body_fat),
                    value = fat.toFloat(),
                    valueText = "%.1f%%".format(fat),
                    healthy = healthyFatRange(state.sex),
                    axis = FatAxis,
                )
            }
        }
    }
}

private fun healthyFatRange(sex: Sex?) = when (sex) {
    Sex.FEMALE -> FemaleFatHealthy
    else -> MaleFatHealthy
}

@Composable
private fun MetricGauge(
    label: String,
    value: Float,
    valueText: String,
    healthy: ClosedFloatingPointRange<Float>,
    axis: ClosedFloatingPointRange<Float>,
) {
    val inHealthyRange = value in healthy
    val valueColor = if (inHealthyRange) HealthyGreen else AlertRed
    val span = axis.endInclusive - axis.start
    val healthyStart = (healthy.start - axis.start) / span
    val healthyEnd = (healthy.endInclusive - axis.start) / span
    val marker = ((value - axis.start) / span).coerceIn(0f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(valueText, style = MaterialTheme.typography.titleMedium, color = valueColor)
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
        ) {
            val barHeight = 10.dp.toPx()
            val barTop = size.height - barHeight
            val track = Brush.horizontalGradient(
                0f to AlertRed,
                (healthyStart / 2f) to Amber,
                healthyStart to HealthyGreen,
                healthyEnd to HealthyGreen,
                (healthyEnd + (1f - healthyEnd) / 2f) to Amber,
                1f to AlertRed,
            )
            drawRoundRect(
                brush = track,
                topLeft = Offset(0f, barTop),
                size = Size(size.width, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barHeight / 2f),
            )

            val x = marker * size.width
            val notch = 7.dp.toPx()
            val pointer = Path().apply {
                moveTo(x, barTop)
                lineTo(x - notch, barTop - notch)
                lineTo(x + notch, barTop - notch)
                close()
            }
            drawPath(pointer, color = valueColor)
        }

        Text(
            stringResource(
                R.string.healthy_range,
                "%.1f".format(healthy.start),
                "%.1f".format(healthy.endInclusive),
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
