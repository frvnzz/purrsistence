package com.example.purrsistence.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import com.example.purrsistence.ui.theme.Spacing

@Composable
fun TopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val topBarHeight = if (isLandscape) 56.dp else 64.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .statusBarsPadding()
            .semantics { isTraversalGroup = true }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(topBarHeight)
                .padding(horizontal = Spacing.lg)
                .semantics { isTraversalGroup = true },
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                modifier = Modifier
                    .weight(1f)
                    .semantics { isTraversalGroup = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                if (onBackClick != null) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clickable(onClick = onBackClick)
                            .semantics { traversalIndex = 0f },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }

                    Spacer(
                        modifier = Modifier.width(Spacing.md)
                    )
                }
                // Screen Header
                val focusRequester = remember { FocusRequester() }

                LaunchedEffect(title) {
                    focusRequester.requestFocus()
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .semantics { 
                            heading()
                            traversalIndex = 1f
                        }
                        .focusRequester(focusRequester)
                        .focusable()
                )
            }

            // Right slot
            Box(
                modifier = Modifier.semantics { 
                    isTraversalGroup = true
                    traversalIndex = 2f 
                },
                contentAlignment = Alignment.CenterEnd
            ) {
                if (actions != null) {
                    actions()
                }
            }
        }
    }
}
