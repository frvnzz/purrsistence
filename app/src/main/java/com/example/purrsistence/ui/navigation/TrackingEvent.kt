package com.example.purrsistence.ui.navigation

sealed interface TrackingEvent {
    data object NavigateToTrackingScreen : TrackingEvent
    data object NavigateToRewardsScreen : TrackingEvent
    data object NavigateBackHome : TrackingEvent
}