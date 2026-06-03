package com.example.purrsistence.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.example.purrsistence.R
import com.example.purrsistence.ui.theme.Spacing
import com.example.purrsistence.ui.util.formatLocalizedInteger

@Composable
fun CurrencyBadge(
    balance: Int,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .height(40.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
            .clearAndSetSemantics {
                contentDescription = "$balance fish"
            },
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.fish_blue2_24),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(Spacing.sm))

            Text(
                text = formatLocalizedInteger(balance),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}