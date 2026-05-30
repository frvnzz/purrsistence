Debug database seeding (debug builds)
===================================

`SeedDebugData.kt` is a small debug-only helper to seed the Room database with sample goals and tracking sessions.

Location
--------
- The code lives in: `app/src/debug/java/com/example/purrsistence/debug/SeedDebugData.kt`.
  Files placed under `app/src/debug/` are only compiled into debug builds and won't be included in release artifacts.

Usage
-----
Call the seeder from any debug-only UI or from `MainActivity` when running a debug build. Example (in an Activity):

```kotlin
lifecycleScope.launch {
    SeedDebugData.seedDebugData(this@MainActivity)
}
```

Notes
-----
- The seeder inserts a test `UserEntity` (id = 1) and several `GoalEntity` + `TrackingSessionEntity` rows with timestamps.
