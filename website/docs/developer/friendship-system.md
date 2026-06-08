# Friendship & Social System

The Friendship system allows users to interact socially within the Purrsistence application. Users can search for others by username, send friend requests, manage incoming and outgoing invitations, and view their friends' active room layouts and collected cat inventories.

---

## Main Files

- [FriendshipRepository.kt](file:///Users/marcusfichtinger/StudioProjects/purrsistence/app/src/main/java/com/example/purrsistence/data/remote/supabase/repository/FriendshipRepository.kt): The domain repository and implementation bridging the data layer and view models.
- [SupabaseFriendshipRemoteDataSource.kt](file:///Users/marcusfichtinger/StudioProjects/purrsistence/app/src/main/java/com/example/purrsistence/data/remote/supabase/datasource/SupabaseFriendshipRemoteDataSource.kt): Executes database queries and updates against the Supabase backend.
- [FriendViewModel.kt](file:///Users/marcusfichtinger/StudioProjects/purrsistence/app/src/main/java/com/example/purrsistence/ui/viewmodel/FriendViewModel.kt): Manages social UI states, including friend requests, search results, and details for selected friends.
- [FriendsScreen.kt](file:///Users/marcusfichtinger/StudioProjects/purrsistence/app/src/main/java/com/example/purrsistence/ui/screens/FriendsScreen.kt): Displays sections for active friends, incoming request approvals, and outgoing pending requests.
- [FriendSearchScreen.kt](file:///Users/marcusfichtinger/StudioProjects/purrsistence/app/src/main/java/com/example/purrsistence/ui/screens/FriendSearchScreen.kt): UI interface to search for users by username and initiate requests.
- [FriendProfileScreen.kt](file:///Users/marcusfichtinger/StudioProjects/purrsistence/app/src/main/java/com/example/purrsistence/ui/screens/FriendProfileScreen.kt): Displays a friend's details (username, active room cats, and collection inventory).

---

## Database Schema & Relations

Friendships are stored remotely in Supabase using the `friendships` and `profiles` tables.

### Friendships Table (`friendships`)
Stores relationships between two user IDs:
- `id` (Long, Primary Key)
- `requester_id` (UUID, Foreign Key pointing to `profiles.id`)
- `addressee_id` (UUID, Foreign Key pointing to `profiles.id`)
- `status` (String Enum: `PENDING`, `ACCEPTED`, `DECLINED`)
- `created_at` (Timestamp)

### Data Mappings
- **`Friendship`**: Domain model representing a request. Contains requester and addressee IDs along with usernames.
- **`FriendProfile`**: Domain model representing a friend's basic profile details (e.g. ID, username).
- **`FriendProfileDetails`**: Domain model representing a friend's complete state including:
  - `profile`: `FriendProfile` metadata.
  - `collectedCatIds`: List of all cat IDs owned by the friend.
  - `selectedCatIds`: List of up to 5 cat IDs currently placed in their room.

---

## Social Flows

```text
1. SEARCH & ADD FLOW
   [ FriendSearchScreen ] ──► (Type query >= 2 chars) ──► (Query Supabase Profiles)
                                                               │
                                                               ▼
   [ Supabase Database ] ◄── (Insert row: PENDING) ◄── (Click "Add" Button)

2. INVITATION MANAGEMENT FLOW
   [ Supabase Database ] ──► (Fetch Incoming) ──► [ FriendsScreen (Requests Section) ]
                                                        │
                                                        ├─► Accept  ──► (Set status = ACCEPTED)
                                                        └─► Decline ──► (Delete friendship row)

3. FRIENDSHIP & PROFILE INSPECTION
   [ FriendsScreen (Your Friends) ] ──► (Click Friend) ──► [ FriendProfileScreen ]
                                                                 │
                                                                 ▼
                                                    (Fetch details from Supabase)
                                                                 │
                                                                 ▼
                                                   (Load Friend's Cats & Room Layout)
```

### 1. Searching & Adding Friends
- Users search for other users on the `FriendSearchScreen` by typing a query.
- **Throttle / Validation**: Searches are triggered only when the query length is **2 or more characters** to avoid redundant queries.
- The results list shows matches returned from Supabase, filtering out the current user and users who are already friends.
- Tapping **Add** calls `FriendshipRepository.sendFriendRequest(...)`, inserting a new row into the `friendships` table with status `PENDING`.

### 2. Request Management
The `FriendsScreen` divides lists into three distinct sections:
- **Friend Requests (Incoming)**: Displays requests where `addressee_id` is the current user and status is `PENDING`. Users can:
  - **Accept**: Changes status to `ACCEPTED` in Supabase. The user will then appear in the "Your Friends" section.
  - **Decline**: Updates status to `DECLINED` or deletes the friendship record.
- **Outgoing Requests (Pending)**: Displays requests where `requester_id` is the current user and status is `PENDING`. Shows who is yet to accept.
- **Your Friends**: Displays relationships with status `ACCEPTED`. Includes an option to remove the friend (which deletes the friendship row).

### 3. Browsing Friend Collections
Tapping on a friend from the friends list opens the `FriendProfileScreen`:
- It initiates `loadFriendProfile(friendUserId)` inside the `FriendViewModel`.
- It queries the remote Supabase tables (`profiles` and cat collections) to fetch the friend's ownership data.
- The screen resolves these IDs using `CatList.getCatById(catId)` to dynamically construct the UI displaying:
  - **Selected Cats**: The specific cats they have chosen to place in their room.
  - **Cat Collection**: The full catalog of cats they have unlocked.
