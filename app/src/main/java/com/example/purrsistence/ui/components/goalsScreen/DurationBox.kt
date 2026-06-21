package com.example.purrsistence.ui.components.goalsScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.theme.Elevation
import com.example.purrsistence.ui.theme.Shapes
import com.example.purrsistence.ui.theme.Spacing

@Composable
fun DurationBox(
    value: String,
    label: String,
    maxValue: Int,
    onValueChange: (String) -> Unit
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        ElevatedCard(
            shape = Shapes.cards,

            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = Elevation.Lvl2
            ),

            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {

            Box(
                modifier = Modifier
                    .size(
                        width = 132.dp,
                        height = 132.dp
                    ),

                contentAlignment = Alignment.Center
            ) {

                BasicTextField(
                    value = value,

                    onValueChange = { input ->
                        val filtered =
                            input
                                .filter { char -> char.isDigit() }
                                .take(2)

                        if (filtered.isEmpty()) {
                            onValueChange("")
                        } else {
                            val number = filtered.toIntOrNull() ?: 0

                            if (number <= maxValue) {
                                onValueChange(
                                    number.toString().padStart(2, '0')
                                )
                            }
                        }
                    },

                    modifier = Modifier.semantics {
                        contentDescription = "$label input"
                    },

                    singleLine = true,

                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),

                    textStyle = TextStyle(
                        fontSize = MaterialTheme.typography.displayLarge.fontSize,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    ),

                    decorationBox = { innerTextField ->

                        Box(
                            contentAlignment = Alignment.Center
                        ) {

                            if (value.isEmpty()) {

                                Text(
                                    text = "00",
                                    style = MaterialTheme.typography.displayLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            innerTextField()
                        }
                    }
                )
            }
        }

        Spacer(
            modifier = Modifier.height(Spacing.sm)
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.clearAndSetSemantics { }
        )
    }
}
