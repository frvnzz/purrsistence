# Focus Notifications & Reminders

Purrsistence manages two categories of background tasks and notifications:
1. **Active Session Tracking**: Handled via an Android Foreground Service to keep tracking sessions alive when the app is in the background.
2. **Focus Reminders**: Handled via Android WorkManager to remind the user to return to their goals after being idle.

---

## Main Files

- [NotificationChannels.kt](file:///Users/marcusfichtinger/StudioProjects/purrsistence/app/src/main/java/com/example/purrsistence/notifications/NotificationChannels.kt): Registers notification channels with the system.
- [TrackingForegroundService.kt](file:///Users/marcusfichtinger/StudioProjects/purrsistence/app/src/main/java/com/example/purrsistence/service/TrackingForegroundService.kt): The Android Foreground Service managing active tracking notifications.
- [TrackingStopReceiver.kt](file:///Users/marcusfichtinger/StudioProjects/purrsistence/app/src/main/java/com/example/purrsistence/service/TrackingStopReceiver.kt): Broadcast receiver handling the "Stop" button click directly from the notification shade.
- [SessionReminderScheduler.kt](file:///Users/marcusfichtinger/StudioProjects/purrsistence/app/src/main/java/com/example/purrsistence/notifications/SessionReminderScheduler.kt): Schedules and cancels focus reminders.
- [SessionReminderWorker.kt](file:///Users/marcusfichtinger/StudioProjects/purrsistence/app/src/main/java/com/example/purrsistence/notifications/SessionReminderWorker.kt): The CoroutineWorker executing the reminder task.

---

## Notification Channels

All notifications are directed to specific channels initialized at app launch in `NotificationChannels.kt`:

- **Tracking Channel (`tracking`)**:
  - Used for ongoing focus sessions.
  - Configured with high importance, but silent sound characteristics to avoid irritating the user while they focus.
- **Reminders Channel (`reminders`)**:
  - Used for inactive or overdue focus prompts.
  - Configured with default importance (sound and vibration enabled by default).

---

## Active Tracking (Foreground Service)

To prevent the Android OS from reclaiming memory and killing the focus timer when the app is minimized, active tracking is run inside `TrackingForegroundService`.

```
[App / TrackingViewModel]
     │
     ├─► start() ────► [TrackingForegroundService] (onStartCommand)
     │                      │
     │                      ├─► Create Notification (Chronometer running)
     │                      └─► Call startForeground(...)
     │
     ├─► update() ───► Updates notification (Chronometer paused, shows static time)
     │
     └─► stop() ─────► Calls stopSelf() -> removes notification
```

### Foreground Service Lifecycle
- **Start**: `TrackingForegroundService.start(context, trackingId, goalTitle, startTimeMillis)` launches the service using `ContextCompat.startForegroundService`. It binds itself as a foreground service using `startForeground(...)`.
- **System Compatibility (Android 14+)**: For devices running API 34 (Upside Down Cake) and above, the service specifies `ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE` to comply with strict system permissions.
- **Update**: If a user pauses tracking, `update(...)` is called, which rebuilds the notification to update its layout and timer display.
- **Stop**: Calling `stop(context)` halts the service and removes the persistent notification from the system shade.

### Chronometer & Timer Logic
To optimize battery usage, the service does not wake up the CPU every second to tick the notification. Instead, it utilizes the system UI chronometer:
- **Running State**: Sets `.setUsesChronometer(true)` and `.setShowWhen(true)`. The base time is offset: `System.currentTimeMillis() - elapsedMillis`. The Android system UI then handles updating the tick dynamically on the screen.
- **Paused State**: Sets `.setUsesChronometer(false)`. The notification description is updated to a static string containing the formatted elapsed duration (e.g. `05:20 elapsed`).

### Notification Action Handler
The notification includes an action button to "Stop" tracking:
- Tapping **Stop** triggers a broadcast Intent targeting `TrackingStopReceiver`.
- `TrackingStopReceiver` fetches the `trackingId` from the intent extras, calls `trackingService.stopTracking(trackingId)` asynchronously in a coroutine scope, and stops the foreground service.

---

## Focus Reminders (WorkManager)

If a user has been inactive, Purrsistence sends a reminder to prompt them to return. This is managed using the Android Jetpack WorkManager library.

### Scheduling Reminders
- `SessionReminderScheduler.scheduleReminder(...)` enqueues a `OneTimeWorkRequest` containing custom parameters (title, description) passed in the `Data` bundle.
- **Job Uniqueness**: It enqueues the request using `enqueueUniqueWork` with `ExistingWorkPolicy.REPLACE` and the unique work name `session_reminder_work`. This ensures that scheduling a new reminder (e.g., when ending a focus session or interacting with the app) automatically replaces and resets any existing scheduled reminders.
- **Delay**: Reminders are scheduled with an initial delay of **180 minutes** (3 hours) by default.

### Worker Execution
When the WorkManager delay completes, the system executes `SessionReminderWorker.doWork()` in the background:
- It checks for notification permissions (required on Android 13+ / API 33+).
- It extracts the custom title and message from the input data bundle (using defaults if null).
- It builds a user-dismissible notification on the `reminders` channel with `R.drawable.fish_blue2_24` as the icon.
- It attaches a `PendingIntent` that opens `MainActivity` when tapped, bringing the user back into the application.
