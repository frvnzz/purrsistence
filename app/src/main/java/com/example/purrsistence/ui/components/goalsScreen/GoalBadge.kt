package com.example.purrsistence.ui.components.goalsScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.theme.Spacing

@Composable
fun GoalBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    icon: ImageVector? = null
) {
    Surface(
        shape = CircleShape,
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = Spacing.md,
                vertical = Spacing.xs
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = contentColor
                )

                Spacer(
                    modifier = Modifier.width(Spacing.xs)
                )
            }

            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
        }
    }
}