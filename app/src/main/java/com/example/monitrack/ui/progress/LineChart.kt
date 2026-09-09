package com.example.monitrack.ui.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** A single point: [dayFraction] is the position (0..1) across the selected date range. */
data class ChartEntry(val dayFraction: Float, val value: Float)

/** Minimal line chart drawn with Canvas — no external charting dependency. */
@Composable
fun LineChart(
    title: String,
    entries: List<ChartEntry>,
    lineColor: Color,
    emptyText: String,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)

            if (entries.isEmpty()) {
                Text(
                    text = emptyText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp),
                )
                return@Column
            }

            val minValue = entries.minOf { it.value }
            val maxValue = entries.maxOf { it.value }
            val axisColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)

            Box(Modifier.fillMaxWidth()) {
                Text(
                    text = formatValue(maxValue),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.TopStart),
                )
                Text(
                    text = formatValue(minValue),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.BottomStart),
                )
                Canvas(
                    Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(start = 36.dp, top = 8.dp, bottom = 8.dp),
                ) {
                    val range = (maxValue - minValue).takeIf { it > 0f } ?: 1f
                    val w = size.width
                    val h = size.height

                    drawLine(axisColor, Offset(0f, 0f), Offset(0f, h), strokeWidth = 2f)
                    drawLine(axisColor, Offset(0f, h), Offset(w, h), strokeWidth = 2f)

                    val points = entries
                        .sortedBy { it.dayFraction }
                        .map { entry ->
                            Offset(
                                x = entry.dayFraction * w,
                                y = h - ((entry.value - minValue) / range) * h,
                            )
                        }

                    for (i in 1 until points.size) {
                        drawLine(lineColor, points[i - 1], points[i], strokeWidth = 4f)
                    }
                    points.forEach { drawCircle(lineColor, radius = 6f, center = it) }
                }
            }
        }
    }
}

private fun formatValue(value: Float): String =
    if (value == value.toLong().toFloat()) value.toLong().toString() else "%.1f".format(value)
