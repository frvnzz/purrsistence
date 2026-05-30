# Supabase Integration

This documentation outlines how Supabase is integrated into the Purrsistence application.

## Overview
Purrsistence uses Supabase for its backend services, including:
- **Authentication**: User signup, login, and session management.
- **Database (Postgrest)**: Storing user profiles, cat collections, goals, tracking sessions, and friendships.
- **Storage**: (Planned/Active) For user-generated content like avatars.

## Client Configuration
The `SupabaseClient` is centrally managed by the `SupabaseClientProvider`.

- **Initialization**: Configured in `SupabaseClientProvider.kt` using `createSupabaseClient`.
- **Security**: Uses `SUPABASE_URL` and `SUPABASE_PUBLISHABLE_KEY` from `BuildConfig`.
- **Modules**:
    - `Auth`: Configured with `SettingsSessionManager` for persistent sessions using `SharedPreferences`.
    - `Postgrest`: Enables CRUD operations on the PostgreSQL database.
    - `Storage`: Enables interaction with Supabase buckets.

## Data Layer Architecture
The integration follows a clear separation:

### 1. DTOs (Data Transfer Objects)
Located in `com.example.purrsistence.data.remote.supabase.dto`. These classes use `kotlinx.serialization` to map database rows to Kotlin objects.
Key DTOs include:
- `ProfileDto`: User profile information.
- `FriendshipDto`: Status of relationships between users.
- `FriendProfileDto`: Profile data for friends.
- `CatDto`: Metadata for collectible cats.
- `UserCatDto`: Junction table mapping users to their collected cats.
- `SelectedCatDto`: Tracking which cats a user currently has "selected".
- `GoalsDto`: Data for user-defined goals.
- `TrackingSessionDto`: Data for completed focus sessions.
- `UserSyncStateDto`: Tracking the `remoteUpdatedAt` timestamp for a user's data.

### 2. Remote Data Sources
Interfaces and their Supabase implementations are located in `com.example.purrsistence.data.remote.supabase.datasource`.

| Data Source | Responsibility |
| :--- | :--- |
| `SupabaseAuthRemoteDataSource` | Wraps `supabase.auth` for user management. |
| `SupabaseProfileRemoteDataSource` | Manages user profiles and `user_sync_state`. |
| `SupabaseFriendshipRemoteDataSource` | Handles friend requests (pending, accepted, declined). |
| `SupabaseCatRemoteDataSource` | Manages user-cat ownership records. |
| `SupabaseGoalTrackingRemoteDataSource` | Syncs local goals and sessions to the cloud. |

### 3. Repositories
Located in `com.example.purrsistence.data.remote.supabase.repository`. Repositories bridge the gap between Remote Data Sources and the Domain layer, handling the mapping between DTOs and Domain Models.

| Repository | Responsibility |
| :--- | :--- |
| `AuthRepository` | Higher-level auth operations. |
| `ProfileRepository` | Mapping `ProfileDto` and `UserSyncStateDto` to domain models. |
| `FriendshipRepository` | Managing friend lists and requests. |
| `CatCollectionRepository` | Handling cat ownership and selection. |
| `GoalTrackingRepository` | Managing goals and tracking sessions. |
| `SyncSnapshotRepository` | Provides atomic "snapshots" of user data for synchronization. |

## Implementation Patterns

### Repositories as Mappers
Repositories wrap the Data Sources and are responsible for converting DTOs (like `ProfileDto`) into Domain Models (like `FriendProfile`). This keeps the UI and Domain logic clean of Supabase-specific details.

### Updating Records
Updates target specific columns and use filters to ensure data integrity:
```kotlin
override suspend fun updateUsername(userId: String, username: String) {
    supabase
        .from("profiles")
        .update({
            set("username", username)
        }) {
            filter {
                eq("id", userId)
            }
        }
}
```

### Complex Relationships
For features like Friendships, multiple queries are often required to resolve many-to-many relationships manually, as seen in `SupabaseFriendshipRemoteDataSource.fetchAcceptedFriendProfiles`.

## Synchronization Logic
The application uses synchronization implemented in `SupabaseSyncService`. It ensures that local and remote data remain consistent across devices.

### State Comparison
Instead of comparing every field individually, the system generates "signatures" or hashes for complex data like goals and tracking sessions. A `SyncComparableUserData` object is used to compare the local state with a remote "snapshot" fetched via `SyncSnapshotRepository`.

If the signatures match, the system is considered `IN_SYNC`.

### Conflict Resolution
When changes are detected, the system uses a **"Last Write Wins"** strategy based on timestamps:

1.  **Local Changes**: If `localUser.hasPendingLocalChanges` is true and `localUpdatedAt` is after `remoteUpdatedAt`, the local state is pushed to Supabase.
2.  **Remote Changes**: In all other cases (e.g., first sync, remote changes detected, or remote is newer), the remote dataset is applied to the local database.

### Sync Triggers
The app checks and syncs data on specific lifecycle events (e.g., after sign-in, or when local goals/sessions are modified).
