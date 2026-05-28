package com.example.purrsistence.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.purrsistence.domain.model.DailyStat
import com.example.purrsistence.ui.util.formatMinutesForAccessibility
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.ui.text.TextStyle as ComposeTextStyle

private fun formatYAxisLabel(hours: Double): String {
    return if (hours < 1.0) {
        "${(hours * 60).toInt()}m"
    } else {
        "${hours.toInt()}h"
    }
}

@Composable
fun WeeklyChart(dailyStats: List<DailyStat>) {

    val modelProducer = remember { CartesianChartModelProducer() }

    val axisLabelComponent = rememberAxisLabelComponent(
        style = ComposeTextStyle(
            color = MaterialTheme.colorScheme.onSurface
        )
    )

    LaunchedEffect(dailyStats) {
        if (dailyStats.isEmpty()) return@LaunchedEffect

        modelProducer.runTransaction {
            val values = dailyStats
                .sortedBy { it.dayOfWeek.value }
                .map { it.totalMinutes / 60f } // Convert minutes to hours

            columnSeries {
                series(values)
            }
        }
    }

    // Check if all values are zero (empty week)
    val hasData = dailyStats.any { it.totalMinutes > 0 }

    val chartDescription = remember(dailyStats) {
        if (!hasData) {
            "No tracking data for this week"
        } else {
            val summary = dailyStats
                .sortedBy { it.dayOfWeek.value }
                .joinToString(", ") { stat ->
                    val dayName = stat.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
                    val time = formatMinutesForAccessibility(stat.totalMinutes)
                    "$dayName: $time"
                }
            "Weekly tracking summary: $summary"
        }
    }

    Column {
        Text(
            text = "Tracked Time per Day",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.semantics { heading() }
        )

        if (!hasData) {
            // Show empty state message
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No tracking data for this week",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Show chart only when there's data
            CartesianChartHost(
                modifier = Modifier.semantics {
                    contentDescription = chartDescription
                },
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(
                        ColumnCartesianLayer.ColumnProvider.series(
                            rememberLineComponent(
                                shape = RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                ),
                                thickness = 7.dp,
                                fill = Fill(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                                        )
                                    )
                                )
                            )
                        )
                    ),
                    startAxis = VerticalAxis.rememberStart(
                        guideline = null,
                        label = axisLabelComponent,
                        valueFormatter = { _, y, _ ->
                            formatYAxisLabel(y)
                        }
                    ),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        label = axisLabelComponent,
                        valueFormatter = { _, x, _ ->
                            val dayIndex = x.toInt()
                            val day = DayOfWeek.of(dayIndex + 1)

                            day.getDisplayName(
                                TextStyle.SHORT,
                                Locale.getDefault()
                            )
                        }
                    )
                ),
                modelProducer = modelProducer
            )
        }
    }
}
