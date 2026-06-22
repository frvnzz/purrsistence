package com.example.purrsistence.ui.components.homeScreen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
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
import kotlinx.coroutines.launch

@Composable
fun GoalBottomDrawer(
    modifier: Modifier = Modifier,
    goals: List<GoalWithSessions>,
    selectedGoalId: Int?,
    onGoalSelected: (Int) -> Unit,
    onStartClick: (Int, String) -> Unit,
    onAddGoalClick: () -> Unit,
    alwaysExpanded: Boolean = false,
    onStartButtonPositioned: (LayoutCoordinates) -> Unit = {}
) {
    // get all inactive = false goals
    val activeGoals = goals.filter { !it.goal.inactive }
    // goals that are listed in the LazyColumn (all but selected goal)
    val selectableGoals = activeGoals.filter {
        it.goal.id != selectedGoalId
    }

    val selectedGoal = activeGoals.find { it.goal.id == selectedGoalId }?.goal
    val hasSelectedGoal = selectedGoal != null

    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    // HEIGHT
    val collapsedHeight = 127.dp
    val expandedHeight = 500.dp

    // PROGRESS: 0f = collapsed, 1f = expanded
    val progressAnimatable = remember {
        Animatable(if (alwaysExpanded) 1f else 0f)
    }
    val progress = progressAnimatable.value

    // force expanded if HomeScreen is in landscape
    LaunchedEffect(alwaysExpanded) {
        if (alwaysExpanded) {
            progressAnimatable.snapTo(1f)
        }
    }

    val height =
        if (alwaysExpanded) {
            expandedHeight
        } else {
            lerp(collapsedHeight, expandedHeight, progress)
        }

    val isExpanded = progress > 0.5f

    //handle system back button to collapse drawer if it's expanded
    BackHandler(enabled = !alwaysExpanded && progress > 0f) {
        focusManager.clearFocus()
        coroutineScope.launch {
            progressAnimatable.animateTo(0f)
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // SCRIM
        if (!alwaysExpanded && progress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f * progress))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            focusManager.clearFocus()
                            coroutineScope.launch {
                                progressAnimatable.animateTo(0f)
                            }
                        }
                    )
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(height)
                .semantics {
                    contentDescription = "Goal selection drawer"
                    if (!alwaysExpanded) {
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
                            focusManager.clearFocus()
                            coroutineScope.launch {
                                progressAnimatable.animateTo(if (isExpanded) 0f else 1f)
                            }
                            true
                        }
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

                        if (delta != 0f) {
                            focusManager.clearFocus()
                        }

                        coroutineScope.launch {
                            progressAnimatable.snapTo(
                                (progress - delta / heightPx).coerceIn(
                                    0f,
                                    1f
                                )
                            )
                        }
                    },
                    onDragStopped = {
                        coroutineScope.launch {
                            progressAnimatable.animateTo(if (progress > 0.5f) 1f else 0f)
                        }
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
                    color = MaterialTheme.colorScheme.onSecondary,
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
                                if (alwaysExpanded) {
                                    null
                                } else if (isExpanded) {
                                    "Collapse goal list"
                                } else {
                                    "Expand goal list"
                                },
                            onClick = {
                                if (!alwaysExpanded) {
                                    coroutineScope.launch {
                                        progressAnimatable.animateTo(if (isExpanded) 0f else 1f)
                                    }
                                }
                            }
                        )
                        .semantics {
                            if (!alwaysExpanded) {
                                role = Role.Button
                                stateDescription =
                                    if (isExpanded) {
                                        "Expanded Goal Selection"
                                    } else {
                                        "Collapsed Goal Selection"
                                    }
                            }
                        }
                        .padding(horizontal = Spacing.lg),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Selected Goal
                    AnimatedContent(
                        targetState = selectedGoal,
                        transitionSpec = {
                            fadeIn().togetherWith(fadeOut())
                        },
                        modifier = Modifier
                            .weight(1f)
                            .draggable(
                                orientation = Orientation.Vertical,
                                state = rememberDraggableState { delta ->
                                    val heightPx =
                                        with(density) { (expandedHeight - collapsedHeight).toPx() }
                                    coroutineScope.launch {
                                        progressAnimatable.snapTo(
                                            (progress - delta / heightPx).coerceIn(
                                                0f,
                                                1f
                                            )
                                        )
                                    }
                                },
                                onDragStopped = {
                                    coroutineScope.launch {
                                        progressAnimatable.animateTo(if (progress > 0.5f) 1f else 0f)
                                    }
                                }
                            ),
                        label = "SelectedGoalTransition"
                    ) { goal ->
                        GoalListItem(
                            title = goal?.title ?: "Create a Goal to select here",
                            type = goal?.type,
                            targetDuration = goal?.targetDuration,
                            deepFocus = goal?.deepFocus ?: false,
                            backgroundColor = MaterialTheme.colorScheme.background,
                            modifier = Modifier.fillMaxWidth(),
                            isPlaceholder = goal == null
                        )
                    }

                    Spacer(modifier = Modifier.width(Spacing.lg))

                    // Play Button
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        tonalElevation = Elevation.Lvl2,
                        modifier = Modifier.onGloballyPositioned { onStartButtonPositioned(it) }
                    ) {
                        IconButton(
                            modifier = Modifier
                                .padding(Spacing.xxs)
                                .semantics {
                                    role = Role.Button
                                },
                            onClick = {
                                if (activeGoals.isEmpty()) {
                                    onAddGoalClick()
                                } else {
                                    selectedGoal?.let {
                                        onStartClick(it.id, it.title)
                                    }
                                }
                            },
                            enabled = hasSelectedGoal || activeGoals.isEmpty()
                        ) {
                            Icon(
                                if (activeGoals.isEmpty()) Icons.Default.Add else Icons.Default.PlayArrow,
                                contentDescription = if (activeGoals.isEmpty()) "Add new goal" else "Start tracking",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                // Expanded content (LazyColumn of other goals to select)
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.weight(1f)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
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
                                        coroutineScope.launch {
                                            progressAnimatable.animateTo(0f)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
