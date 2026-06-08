# Room & Shop System

The Room and Shop systems form the core gamification loop of Purrsistence. Users earn currency (coins) by completing focus sessions and goals, which they can spend in the Shop to buy cats. Purchased cats can then be selected to roam around the user's virtual Room.

---

## Main Files

- [ShopService.kt](file:///Users/marcusfichtinger/StudioProjects/purrsistence/app/src/main/java/com/example/purrsistence/service/ShopService.kt): Handles transactions, balances, and selection logic for Room cats.
- [RoomService.kt](file:///Users/marcusfichtinger/StudioProjects/purrsistence/app/src/main/java/com/example/purrsistence/service/RoomService.kt): Defines the coordinates of available room spots and randomizes spot assignments.
- [RoomView.kt](file:///Users/marcusfichtinger/StudioProjects/purrsistence/app/src/main/java/com/example/purrsistence/ui/components/homeScreen/RoomView.kt): Renders the interactive Room layout, scaling background textures, placing cats, and managing tap animations.
- [CatList.kt](file:///Users/marcusfichtinger/StudioProjects/purrsistence/app/src/main/java/com/example/purrsistence/domain/cats/CatList.kt): Houses the catalog of all available shop items and cat asset configurations.
- [ShopScreen.kt](file:///Users/marcusfichtinger/StudioProjects/purrsistence/app/src/main/java/com/example/purrsistence/ui/screens/ShopScreen.kt): UI interface showing purchaseable items.
- [HomeScreen.kt](file:///Users/marcusfichtinger/StudioProjects/purrsistence/app/src/main/java/com/example/purrsistence/ui/screens/HomeScreen.kt): Main screen containing the `RoomView` overlay.

---

## Shop & Purchasing Rules

The shop logic resides inside `ShopService`. Transactions follow these restrictions:

1. **Duplicate Prevention**: Users cannot purchase a cat they already own.
2. **Balance Validation**: If the user's current coin balance is less than the cat's price, the purchase is rejected.
3. **Auto-Selection**:
   - Up to **5 cats** can be active in the Room simultaneously.
   - Upon buying a new cat, if the active list has fewer than 5 cats, the new cat is automatically selected and placed in the room.
   - Local state is updated first, then synchronized to Supabase.

---

## Room Spot Assignment

Cats are positioned in 2.5D space using coordinate offsets mapping to a static room background image (`home_room1.png`).

### Preset Locations
`RoomService` defines 5 static `RoomSpot` coordinates inside the room:

| Spot ID | X Coordinate (Percent) | Y Coordinate (Percent) | Mirror Image Orientation |
| :--- | :--- | :--- | :--- |
| `cat_tree_left` | `0.221` (22.1%) | `0.445` (44.5%) | Yes |
| `bed_back` | `0.400` (40.0%) | `0.672` (67.2%) | No |
| `floor_center` | `0.596` (59.6%) | `0.723` (72.3%) | No |
| `floor_right` | `0.750` (75.0%) | `0.850` (85.0%) | Yes |
| `floor_front` | `0.391` (39.1%) | `0.906` (90.6%) | No |

### Randomization
To keep the Room dynamic, the `RoomService.assignCatsToSpots` method shuffles the list of spots before mapping selected cat IDs to them. If the user has selected more than 5 cats (though capped at 5 in UI), the spot assignment wraps around using a modulo operation:
```kotlin
val shuffledSpots = spots.shuffled()
return ownedCatIds.mapIndexed { index, catId ->
    val spot = shuffledSpots[index % shuffledSpots.size]
    PlacedCat(catId, spot.id, spot.isMirrored)
}
```

---

## Room Rendering & Tap Animations

### Aspect Ratio Constrained Scaling
To guarantee coordinates match the background texture across varying device sizes, `RoomView` measures the parent bounds inside `BoxWithConstraints`. It determines if the layout is constrained vertically or horizontally, calculates scaling parameters relative to the original image asset aspect ratio, and sizes a inner coordinate container box accordingly:

```kotlin
val imageAspect = imageSize.width / imageSize.height
val containerAspect = containerWidth.value / containerHeight.value

val actualWidth: Dp
val actualHeight: Dp

if (containerAspect > imageAspect) {
    actualHeight = containerHeight
    actualWidth = containerHeight * imageAspect
} else {
    actualWidth = containerWidth
    actualHeight = containerWidth / imageAspect
}
```

### Positioning & Z-Indexing
- **Offset Offset**: Cats are placed using offsets relative to the parent bounds:
  - `x = (actualWidth * spot.xPercent) - 50.dp`
  - `y = (actualHeight * spot.yPercent) - 100.dp`
  - *Note: the -50.dp and -100.dp offsets align the center-bottom of the cat sprite directly to the coordinates (anchoring their paws to the floor).*
- **Natural Depth Ordering**: Cats are layered using `.zIndex(spot.yPercent)`. This ensures that cats occupying lower vertical space (closer to the front) are drawn on top of cats occupying higher vertical space (further back).

### Heart Particle Bursts
Tapping on a cat in the RoomView spawns a heart particle animation:
- **State Tracking**: `activeBursts` is a mutable state list containing `HeartBurstState` entities.
- **Trigger**: When a cat box registers a tap gesture, a new `HeartBurstState` is added at the tap coordinate.
- **Rendering**: The `HeartParticleEffect` composable uses canvas-drawn particles that drift upwards and fade out. Once the particle lifecycle is complete, it calls `onAnimationComplete`, removing itself from `activeBursts`.
- **Cleanup**: If the selected cats change, a `LaunchedEffect` triggers to remove active bursts associated with any cats that were removed from the room layout.
