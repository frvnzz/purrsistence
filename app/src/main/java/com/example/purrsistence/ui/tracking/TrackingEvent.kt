package com.example.purrsistence.ui.tracking

sealed interface TrackingEvent {
    data object NavigateToTrackingScreen : TrackingEvent
    data object NavigateBackHome : TrackingEvent
}