package com.example.purrsistence.ui.components.homeScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.example.purrsistence.domain.model.GoalWithSessions
import com.example.purrsistence.ui.theme.Elevation
import com.example.purrsistence.ui.theme.Spacing

@Composable
fun GoalBottomDrawer(
    modifier: Modifier = Modifier,
    goals: List<GoalWithSessions>,
    selectedGoalId: Int?,
    onGoalSelected: (Int) -> Unit,
    onStartClick: (Int, String) -> Unit,
    alwaysExpanded: Boolean = false
) {
    // get all inactive = false goals
    val activeGoals = goals.filter { !it.goal.inactive }
    // goals that are listed in the LazyColumn (all but selected goal)
    val selectableGoals = activeGoals.filter {
        it.goal.id != selectedGoalId
    }

    val selectedGoal = activeGoals.find { it.goal.id == selectedGoalId }?.goal
    val hasSelectedGoal = selectedGoal != null

    // TODO: change this to be responsible (hardcoded height of the drawer)
    val collapsedHeight = 127.dp
    val expandedHeight = 500.dp

    val density = LocalDensity.current

    // progress: 0f = collapsed, 1f = expanded
    var progress by remember {
        mutableFloatStateOf(
            if (alwaysExpanded) 1f else 0f
        )
    }
    // force expanded if HomeScreen is in landscape
    if (alwaysExpanded) {
        progress = 1f
    }

    val height =
        if (alwaysExpanded) {
            expandedHeight
        } else {
            lerp(collapsedHeight, expandedHeight, progress)
        }

    val isExpanded = progress > 0.5f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .semantics {
                contentDescription = "Goal selection drawer"
                role = Role.Button
                stateDescription = if (isExpanded) "Expanded" else "Collapsed"

                onClick(
                    label =
                        if (isExpanded) {
                            "Collapse goal list"
                        } else {
                            "Expand goal list"
                        }
                ) {
                    if (!alwaysExpanded) {
                        progress = if (isExpanded) 0f else 1f
                    }
                    true
                }
            }
            .background(
                MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp
                )
            )
            .draggable(
                enabled = !alwaysExpanded,
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    val heightPx = with(density) {
                        (expandedHeight - collapsedHeight).toPx()
                    }

                    progress -= delta / heightPx
                    progress = progress.coerceIn(0f, 1f)
                },
                onDragStopped = {
                    progress = if (progress > 0.5f) 1f else 0f
                }
            )
    ) {

        Column(
            modifier = Modifier.fillMaxHeight()
        ) {
            // drag handle
            if (!alwaysExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.md),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }
            }

            Text(
                text = "Choose Goal to track:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Spacing.xl,
                        top = Spacing.md,
                        bottom = Spacing.sm
                    )
            )

            // HEADER (always visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        enabled = !alwaysExpanded,
                        onClickLabel =
                            if (isExpanded) {
                                "Collapse goal list"
                            } else {
                                "Expand goal list"
                            },
                        onClick = {
                            progress = if (isExpanded) 0f else 1f
                        }
                    )
                    .semantics {
                        role = Role.Button
                        stateDescription =
                            if (isExpanded) {
                                "Expanded Goal Selection"
                            } else {
                                "Collapsed Goal Selection"
                            }
                    }
                    .padding(horizontal = Spacing.lg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selected Goal
                GoalListItem(
                    title = selectedGoal?.title ?: "Create or select a Goal",
                    type = selectedGoal?.type,
                    targetDuration = selectedGoal?.targetDuration,
                    deepFocus = selectedGoal?.deepFocus ?: false,
                    backgroundColor = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .weight(1f),
                    isPlaceholder = !hasSelectedGoal
                )

                Spacer(modifier = Modifier.width(Spacing.lg))

                // Play Button
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = Elevation.Lvl2
                ) {
                    IconButton(
                        modifier = Modifier.padding(Spacing.xxs),
                        onClick = {
                            selectedGoal?.let {
                                onStartClick(it.id, it.title)
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Start tracking",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            // Expanded content (LazyColumn of other goals to select)
            if (isExpanded) {
                LazyColumn(
                    contentPadding = PaddingValues(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    items(selectableGoals) { goalWithSessions ->
                        val goal = goalWithSessions.goal

                        GoalListItem(
                            title = goal.title,
                            type = goal.type,
                            targetDuration = goal.targetDuration,
                            deepFocus = goal.deepFocus,
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                onGoalSelected(goal.id)
                                if (!alwaysExpanded) {
                                    progress = 0f
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}