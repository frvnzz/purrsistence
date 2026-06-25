# Data Schema

Purrsistence uses Room for local data persistence. The schema is designed to support offline-first functionality, with data being synchronized to Supabase when a connection is available and the user is logged in.

## Key Entities

### 1. UserEntity (`users`)
Stores user profile information and gamification state.
- `userId`: Primary Key
- `username`: User's display name
- `balance`: Current coin balance
- `friends`: List of friend IDs
- `collectedCatsIds`: IDs of cats the user has purchased
- `selectedCatIds`: IDs of cats currently displayed in the room (max 5)
- `isSupabaseLinked`: Boolean flag for sync status
- `supabaseUserId`: Remote ID for Supabase mapping

### 2. GoalEntity (`goals`)
Represents user-defined focus goals.
- `id`: Primary Key
- `userId`: Foreign Key to `UserEntity`
- `title`: Goal name
- `type`: Category (Weekly, Daily, ..)
- `targetDuration`: Desired focus time
- `deepFocus`: Whether app blocking is enabled for this goal
- `isCompleted`: Status flag
- `lastCompletedAt`: Timestamp of the last completion of set goal

### 3. TrackingSessionEntity (`tracking_sessions`)
Logs individual focus sessions.
- `id`: Primary Key
- `goalId`: Foreign Key to `GoalEntity`
- `userId`: Foreign Key to `UserEntity`
- `startTime`: Start timestamp
- `endTime`: End timestamp
- `deepFocus`: Whether deep focus was active during the session
- `puaseHistory`: Custom string put together to store total pause time, pause intervals and currency earned during the session with multiplier, if paused too long
- `currentPauseStart`: Timestamp of the start of the current pause
- `lastResetTime`: Timestamp of the last time the multiplier was reset

---

## Data Mapping

The application uses different models for different layers to ensure decoupling:

| Layer | Model Type | Example |
| :--- | :--- | :--- |
| **Data (Local)** | Room Entity | `GoalEntity` |
| **Data (Remote)** | Supabase DTO | `GoalDto` |
| **Domain** | Kotlin Data Class | `Goal` |

## Converters
Room type converters are used for complex types like `Instant`, `URL`, and `List<String>` (stored as JSON strings in the database). These are defined in `com.example.purrsistence.data.local.converter`.
