# Sprite Animation System

The system consists of three main parts:
1. **`SpriteSheetData`**: A configuration class defining the grid and timing.
2. **`SpriteAnimation`**: The core Composable that handles the rendering logic.
3. **`CatImage` Integration**: The high-level UI component that switches between static images and animations based on available data.

## Core Components

### SpriteSheetData
Located in `ui.components.animation`, this data class stores the metadata required to slice a spritesheet correctly.

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `columns` | `Int` | Number of sprites horizontally in the sheet. |
| `rows` | `Int` | Number of sprites vertically in the sheet. |
| `totalFrames` | `Int` | The total number of animation frames (can be less than `columns * rows`). |
| `frameDurationMs` | `Long` | Time each frame is displayed (default is `100ms`). |

### SpriteAnimation
The core rendering component. It uses a `LaunchedEffect` to manage the animation timer and a `Canvas` for performance.

```kotlin
@Composable
fun SpriteAnimation(
    spriteSheetRes: Int,
    data: SpriteSheetData,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    initialFrame: Int = 0
)
```

**How it works:**
- **State Tracking**: Uses `remember { mutableIntStateOf(initialFrame) }` to track the current frame index.
- **Timer Loop**: A `while(true)` loop inside a `LaunchedEffect` increments the frame index and uses `delay(data.frameDurationMs)` to control speed.
- **Slicing**: Calculates the source rectangle (`srcOffset` and `srcSize`) by dividing the `ImageBitmap` dimensions by the number of columns and rows.
- **Canvas Rendering**: Uses `drawImage` to extract only the current frame from the sheet and scale it to the destination size.

## Usage in CatList

To enable animation for a cat, add `animationData` to its `ShopItem` definition in `CatList.kt`:

```kotlin
ShopItem(
    "cat_1",
    "Orange Cat",
    2,
    R.drawable.cat_idle, // This resource should be the spritesheet
    SpriteSheetData(columns = 4, rows = 3, totalFrames = 10, frameDurationMs = 150L)
)
```

## UI Integration

The `CatImage` component acts as a wrapper. It checks if the `ShopItem` has `animationData`:

- **If yes**: It delegates rendering to `SpriteAnimation`.
- **If no**: It renders a standard static `Image`.

This ensures backward compatibility with existing static cat assets while allowing a progressive rollout of animated ones.