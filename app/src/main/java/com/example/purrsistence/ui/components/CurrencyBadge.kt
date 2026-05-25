package com.example.purrsistence.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.purrsistence.R
import com.example.purrsistence.ui.theme.Elevation
import com.example.purrsistence.ui.theme.Spacing

@Composable
fun CurrencyBadge(balance: Int) {
    Surface(
        modifier = Modifier.height(40.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        tonalElevation = Elevation.Lvl2
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                // TODO: replace with actual currency icon later
                painter = painterResource(id = R.drawable.fish_blue2_24),
                contentDescription = "Currency",
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(Spacing.sm))

            Text(
                text = balance.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}