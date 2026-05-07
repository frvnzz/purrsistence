# Profile Screen

This page explains how the profile screen works in the app. It covers the profile picture, username editing, and the inventory of collected cats.

## Main files

- `ProfileScreen.kt`: screen wiring: state, effects and layout (portrait/landscape).
- `ProfileHeader.kt`: header, username editing and status badge.
- `EditableProfileAvatar.kt`: avatar, change/remove actions and image preview.
- `ProfileActions.kt`: Settings / Friends action buttons.
- `Inventory.kt`: inventory grid and cat card.
- `UserViewModel.kt`: exposes `user` flow and methods to update username/profile image.
- `ProfileService.kt`: persists profile updates (copies local URIs into app cache and updates Room).
- `AppNavHost.kt`: registers the `profile` route.

## How it works

- `ProfileScreen` observes `UserViewModel.user` and keeps local UI state (editing, selected image URI, etc.).
- Avatar tap launches the Android photo picker; picked URI is forwarded to `UserViewModel.updateProfileImage(...)`, which delegates to `ProfileService` to persist the image reference.
- Username edits are saved via `UserViewModel.updateUsername(...)` → `ProfileService.updateProfile(...)` which updates Room.
- Inventory shows collected cat IDs; each ID is resolved with `CatList.getCatById(...)` before rendering.

## Notes

- The screen uses a different layout in portrait and landscape mode.
- Collected cat IDs are resolved through `CatList.getCatById(...)` before they are shown.
- Profile images are local-only for now; they are not uploaded to Supabase.
