# Architecture

Purrsistence follows a layered architecture inspired by Clean Architecture principles, ensuring a separation of concerns and making the codebase easier to test and maintain.

## Layered Structure

The application is divided into several layers, each with a specific responsibility:

### 1. UI Layer (Presentation)
- **Framework**: Jetpack Compose.
- **Components**: Composables located in `com.example.purrsistence.ui`.
- **State Management**: ViewModels (`com.example.purrsistence.ui.viewmodel`) expose UI state using Kotlin `StateFlow`.

### 2. Domain Layer (Business Logic)
- **Models**: Plain Kotlin classes in `com.example.purrsistence.domain.model`.
- **Interfaces**: Repository and Service interfaces that define the "what" without specifying the "how".
- **Services**: Higher-level business logic that orchestrates multiple repositories (e.g., `GoalService`, `TrackingService`).

### 3. Data Layer (Infrastructure)
- **Local Data**: Room database (`AppDatabase`) and DAOs.
- **Remote Data**: Supabase integration via `SupabaseClient`.
- **Repositories**: Implementations of domain interfaces (e.g., `UserRepositoryImpl`, `GoalRepositoryImpl`). These handle data mapping between Entities/DTOs and Domain Models.

---

## Dependency Injection

The project uses **Manual Dependency Injection** via a central `AppContainer`.

### AppContainer
Located at `com.example.purrsistence.AppContainer`, this class:
1. Manages the lifecycle of singletons (Database, Repositories, Services).
2. Uses `lazy` initialization to avoid overhead during app startup.
3. Provides a single source of truth for dependencies across the application.

### AppViewModelFactory
Since we use manual DI, we use a custom `ViewModelProvider.Factory` (`AppViewModelFactory`) to inject dependencies from the `AppContainer` into our ViewModels.

---

## Reactive Data Flow

The app leverages Kotlin Coroutines and Flows for asynchronous and reactive programming:

1. **DAOs**: Return `Flow<T>` from Room queries to observe database changes.
2. **Repositories**: Transform database entities into domain models while preserving the `Flow`.
3. **ViewModels**: Collect these flows and convert them into `StateFlow` using `stateIn` for the UI to observe.
4. **UI**: Uses `collectAsStateWithLifecycle()` to reactively update Composables when the underlying data changes.

---

## Key Components

- **Cats System**: Managed by `CatCollectionRepository` and `ShopService`.
- **Goal Tracking**: Orchestrated by `TrackingService`, linking goals, sessions, and rewards.
- **Supabase Sync**: Handled by `SupabaseSyncService` to bridge local and remote states.
- **Deep Focus**: Implemented via an Accessibility Service for system-level app blocking.
