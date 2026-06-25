# App Setup

This guide will help you set up the development environment for the Purrsistence Android application.

## Prerequisites

- **Android Studio**
- **JDK**
- **SDK**

## Initial Configuration

The app requires connection details for Supabase. These are sensitive and should be kept in your `local.properties` file.

1.  Open `local.properties` in the root of the project.
2.  Add the following lines (replace with actual values):

```properties
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_PUBLISHABLE_KEY=your-key
```

> [!NOTE]
> These values are injected into the app's `BuildConfig` during compilation by the `app/build.gradle.kts` script.

## Building the App

1.  Open the project in Android Studio.
2.  Wait for the Gradle sync to finish.
3.  Select the `app` run configuration.
4.  Click **Run** to build and deploy to an emulator or physical device.

## Troubleshooting

### Gradle Sync Fails
- Check that your `local.properties` contains the required Supabase keys.
- Try **File -> Invalidate Caches / Restart**.

### Supabase Connectivity
- If the app crashes on launch or fails to sync, verify your `SUPABASE_URL` and `SUPABASE_PUBLISHABLE_KEY`.
- Ensure the Supabase project is active.
