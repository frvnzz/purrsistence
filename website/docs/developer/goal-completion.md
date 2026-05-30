# Goal Completion Logic

## Goal Service / Goal ViewModel

### completeGoalIfReached
**Parameters**: (goalWithSessions: GoalWithSessions, now: ZonedDateTime)

1. First checks if the Goal has been completed already in the current time window, depending on the goal type.
2. Then checks if the Goal has been completed by tracking the required time.
3. And if Goal has been reached return _true_ and set _isCompleted_ and _lastCompletedAt_ in DB.

### resetCompletedGoalsIfNewCycle
**Parameters**: (userId: Int, now: ZonedDateTime)

Resets the _isCompleted_ flag for all goals that have a new cycle starting at the current time. For example, if a daily goal was completed yesterday, it will be reset.

**TODO**: Currently only gets done when opening the app in _MainActivity_, but should be implemented more often, e.g. when loading the goals screen.

## Tracking Service

### stopTracking
**Parameters**: (trackingId: Int)

Contains logic to check if a goal has been completed when stopping a tracking session. It calls the _completeGoalIfReached_ function of the Goal Service.
It also starts the process to add the additional reward for completing a goal.

### calculateGoalCompletionReward
**Parameters**: (goal: Goal)

Calculates the additional reward for completing a goal depending on the goal type.
Currently, the rewards are as follows:
- DAILY: 50
- WEEKLY: 200
- MONTHLY: 500

**TODO**: Rewards still need to be balanced.

## Tracking Screen
When stopping a tracking session and if a goal has been completed, an additional text will be shown how much additional coins the player has earned and that the goal was completed.

## Goal With Sessions

### hasCompletedCurrentWindow
**Parameters**: (now: ZonedDateTime)

Checks dependend on the goal type if the goal has already been completed in the current time window. For example, if it's a daily goal, it checks if the goal was completed today.

### isCurrentlyAtOrAboveTarget
**Parameters**: (now: ZonedDateTime)

Checks if the total tracked time in the current time window is at or above the target time for the goal.

### trackedDurationInWindow
**Parameters**: (window: TimeWindow)

Calculates the total tracked duration for sessions that started within the given time window.
TimeWindow is a helper data class that contains a start and end time in one.

### currentProgress
**Parameters**: (now: ZonedDateTime)

Returns the current progress towards the goal from 0 to 1. For example, if the goal is to track 2 hours and the user has tracked 1 hour in the current time window, it will return 0.5.

## Time Window Helper
The Time Window Helper contains functions to calculate the start and end times for the current time window depending on the goal type. For example, for a daily goal, it calculates the start of the day and the end of the day.
The **cutoff time** (currently 3 AM) is also set here.
It also has the custom data class TimeWindow that contains a start and end time in one.
### TimeWindow
```
data class TimeWindow (
    val start: Instant,
    val end: Instant
)
```