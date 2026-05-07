---
sidebar_position: 3
---

# Profile Screen

This page explains how the profile screen works in the app. It covers the profile picture, username editing, and the inventory of collected cats.

## Main files

- `ProfileScreen.kt`: profile UI, avatar actions, username editing, and inventory display.
- `UserViewModel.kt`: entry point for saving profile changes.
- `ProfileService.kt`: stores the updated username and profile image.
- `AppNavHost.kt`: registers the `profile` route.

## How it works

1. The screen reads the current user from `UserViewModel.user`.
2. The header shows the avatar, username, cat count, and a small LinkedIn-style badge when the account is connected.
3. Tapping the avatar opens the Android photo picker. The picked image is passed to `ProfileService`, which keeps local `content://` images by copying them into the app cache when needed.
4. The stored image path is saved in Room as the user’s profile image URL.
5. The delete button clears the saved profile picture.
6. Editing the name switches the header into a text field with Save and Cancel actions.
7. The inventory section shows collected cats in a 3-column grid, or a short message when the inventory is empty.

## Notes

- The screen uses a different layout in portrait and landscape mode.
- Collected cat IDs are resolved through `CatList.getCatById(...)` before they are shown.
- Profile images are local-only for now; they are not uploaded to Supabase.
