package com.example.purrsistence.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.purrsistence.ui.state.StatisticsUiState
import com.example.purrsistence.ui.viewmodel.StatisticsViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun WeekSelector(viewModel: StatisticsViewModel, state: StatisticsUiState) {

    //for showing date in selector
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val start = today.with(DayOfWeek.MONDAY)
        .plusWeeks(state.weekOffset.toLong())
    val end = start.plusDays(6)

    val formatter = DateTimeFormatter.ofPattern("d.M.yyyy")

    val formattedStart = start.format(formatter)
    val formattedEnd = end.format(formatter)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Button(onClick = { viewModel.previousWeek() }) {
            Text("<")
        }

        Text("$formattedStart - $formattedEnd")

        Button(onClick = { viewModel.nextWeek() }) {
            Text(">")
        }
    }
}