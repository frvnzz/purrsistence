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
import androidx.compose.ui.unit.dp
import com.example.purrsistence.domain.model.DailyStat
import com.example.purrsistence.ui.util.formatLocalizedNumber
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
        "${formatLocalizedNumber(hours * 60, useGrouping = false)}m"
    } else {
        val hasFraction = hours % 1.0 != 0.0
        val minFractionDigits = if (hasFraction) 1 else 0
        "${formatLocalizedNumber(hours, minFractionDigits = minFractionDigits, maxFractionDigits = 1, useGrouping = false)}h"
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

    Column {
        Text("Tracked Time per Day", style = MaterialTheme.typography.labelLarge)

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
