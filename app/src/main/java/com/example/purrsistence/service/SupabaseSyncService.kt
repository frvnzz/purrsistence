package com.example.purrsistence.service

import com.example.purrsistence.data.local.repository.GoalRepository
import com.example.purrsistence.data.local.repository.TrackingRepository
import com.example.purrsistence.data.local.repository.UserRepository
import com.example.purrsistence.data.remote.supabase.model.SupabaseRemoteUserData
import com.example.purrsistence.data.remote.supabase.repository.AuthRepository
import com.example.purrsistence.data.remote.supabase.repository.CatCollectionRepository
import com.example.purrsistence.data.remote.supabase.repository.FriendshipRepository
import com.example.purrsistence.data.remote.supabase.repository.GoalTrackingRepository
import com.example.purrsistence.data.remote.supabase.repository.ProfileRepository
import com.example.purrsistence.data.remote.supabase.repository.SyncSnapshotRepository
import com.example.purrsistence.domain.model.FriendProfile
import com.example.purrsistence.domain.model.Friendship
import com.example.purrsistence.domain.model.Goal
import com.example.purrsistence.domain.model.TrackingSession
import com.example.purrsistence.domain.model.User
import com.example.purrsistence.domain.model.types.SyncStatus
import kotlinx.coroutines.flow.firstOrNull
import java.net.URL
import java.time.Instant


interface TrackingSyncService {
    fun isSignedIn(): Boolean

    fun currentSupabaseUserId(): String?

    suspend fun signUp(
        email: String,
        password: String,
        username: String
    )

    suspend fun signIn(
        email: String,
        password: String
    )

    suspend fun signOut()

    suspend fun syncAfterLocalGoalChanged(): SyncStatus

    suspend fun syncAfterLocalTrackingSessionChanged(): SyncStatus

    suspend fun checkAndSyncIfNeeded(): SyncStatus

    suspend fun forceUploadLocalToSupabase(): SyncStatus

    suspend fun forceDownloadFromSupabase(): SyncStatus

    suspend fun addCollectedCatToSupabaseAndLocal(
        catId: String
    )

    suspend fun updateUsername(
        username: String
    )

    suspend fun updateAvatarPath(
        avatarPath: String?
    )

    suspend fun resetTrackingSessions(
        userId: Int
    )

    suspend fun getFriends(): List<FriendProfile>

    suspend fun getIncomingFriendRequests(): List<Friendship>

    suspend fun getOutgoingFriendRequests(): List<Friendship>

    suspend fun sendFriendRequest(
        addresseeId: String
    )

    suspend fun acceptFriendRequest(
        friendshipId: Long
    )

    suspend fun declineFriendRequest(
        friendshipId: Long
    )

    suspend fun deleteFriendship(
        friendshipId: Long
    )
}


class SupabaseSyncService(
    private val userRepository: UserRepository,
    private val goalRepository: GoalRepository,
    private val trackingRepository: TrackingRepository,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val catRepository: CatCollectionRepository,
    private val friendshipRepository: FriendshipRepository,
    private val syncSnapshotRepository: SyncSnapshotRepository,
    private val goalTrackingRepository: GoalTrackingRepository,
    private val localUserId: Int = 1
) : TrackingSyncService {

    override fun isSignedIn(): Boolean {
        return authRepository.isSignedIn()
    }

    override fun currentSupabaseUserId(): String? {
        return authRepository.currentUserId()
    }

    override suspend fun signUp(
        email: String,
        password: String,
        username: String
    ) {
        authRepository.signUp(
            email = email,
            password = password,
            username = username
        )

        if (isSignedIn()) {
            syncUserAfterSignIn()
        }
    }

    override suspend fun signIn(
        email: String,
        password: String
    ) {
        authRepository.signIn(
            email = email,
            password = password
        )

        syncUserAfterSignIn()
    }

    override suspend fun signOut() {
        authRepository.signOut()
    }

    override suspend fun syncAfterLocalGoalChanged(): SyncStatus {
        return syncGoalsAndTrackingAfterLocalChange()
    }

    override suspend fun syncAfterLocalTrackingSessionChanged(): SyncStatus {
        return syncGoalsAndTrackingAfterLocalChange()
    }

    private suspend fun syncGoalsAndTrackingAfterLocalChange(): SyncStatus {
        if (!isSignedIn()) {
            return SyncStatus.NOT_LINKED
        }

        val supabaseUserId = requireSupabaseUserId()

        uploadLocalGoalsAndTrackingToSupabase(
            supabaseUserId = supabaseUserId
        )

        return SyncStatus.CONFLICT_RESOLVED_FROM_LOCAL
    }

    private suspend fun syncUserAfterSignIn(): SyncStatus {
        if (!isSignedIn()) {
            return SyncStatus.NOT_LINKED
        }

        val localUser = requireLocalUser()
        val supabaseUserId = requireSupabaseUserId()

        val remoteData = syncSnapshotRepository.fetchUserData(
            supabaseUserId = supabaseUserId,
            localUserId = localUserId
        )

        val localUpdatedAt = localUser.localUpdatedAt ?: Instant.EPOCH
        val remoteUpdatedAt = remoteData.remoteUpdatedAt

        val useLocalData =
            localUser.hasPendingLocalChanges &&
                    localUpdatedAt.isAfter(remoteUpdatedAt)

        val mergedCollectedCatIds =
            (localUser.collectedCatsIds + remoteData.collectedCatIds)
                .distinct()

        val mergedSelectedCatIds =
            (localUser.selectedCatIds + remoteData.selectedCatIds)
                .distinct()
                .filter { catId -> catId in mergedCollectedCatIds }
                .take(3)

        val mergedUsername =
            if (useLocalData) {
                localUser.username
            } else {
                remoteData.profile.username
            }

        val mergedAvatarPath =
            if (useLocalData) {
                localUser.profileImageUrl?.toString()
            } else {
                remoteData.profile.avatarPath
            }

        val localGoals =
            goalRepository.getGoalsForSync(localUserId)

        val localTrackingSessions =
            trackingRepository.getTrackingSessionsForSync(localUserId)

        val mergedGoals =
            mergeGoals(
                localGoals = localGoals,
                remoteGoals = remoteData.goals,
                localWinsOnConflict = useLocalData
            )

        val mergedTrackingSessions =
            mergeTrackingSessions(
                localSessions = localTrackingSessions,
                remoteSessions = remoteData.trackingSessions,
                localWinsOnConflict = useLocalData
            )

        profileRepository.updateUsername(
            userId = supabaseUserId,
            username = mergedUsername
        )

        profileRepository.updateAvatarPath(
            userId = supabaseUserId,
            avatarPath = mergedAvatarPath
        )

        catRepository.uploadCollectedCats(
            userId = supabaseUserId,
            catIds = mergedCollectedCatIds
        )

        catRepository.replaceSelectedCats(
            userId = supabaseUserId,
            selectedCatIds = mergedSelectedCatIds
        )

        syncSnapshotRepository.uploadGoalsAndTracking(
            supabaseUserId = supabaseUserId,
            goals = mergedGoals,
            trackingSessions = mergedTrackingSessions
        )

        val mergedUser = localUser.copy(
            username = mergedUsername,
            profileImageUrl = mergedAvatarPath.toUrlOrNull(),
            collectedCatsIds = mergedCollectedCatIds,
            selectedCatIds = mergedSelectedCatIds,
            isSupabaseLinked = true,
            supabaseUserId = supabaseUserId
        )

        userRepository.updateUserFromRemoteSync(mergedUser)

        goalRepository.replaceGoalsFromRemoteSync(
            userId = localUserId,
            goals = mergedGoals
        )

        trackingRepository.replaceTrackingSessionsFromRemoteSync(
            userId = localUserId,
            sessions = mergedTrackingSessions
        )

        userRepository.markUserSynced(localUserId)

        return SyncStatus.IN_SYNC
    }

    override suspend fun checkAndSyncIfNeeded(): SyncStatus {
        if (!isSignedIn()) {
            return SyncStatus.NOT_LINKED
        }

        val localUser = requireLocalUser()
        val supabaseUserId = requireSupabaseUserId()

        val remoteData = syncSnapshotRepository.fetchUserData(
            supabaseUserId = supabaseUserId,
            localUserId = localUserId
        )

        val localComparable = SyncComparableUserData.fromLocal(
            user = localUser,
            goals = goalRepository.getGoalsForSync(localUserId),
            trackingSessions = trackingRepository.getTrackingSessionsForSync(localUserId)
        )

        val remoteComparable =
            SyncComparableUserData.fromRemote(remoteData)

        if (localComparable == remoteComparable) {
            userRepository.markUserSynced(localUserId)
            return SyncStatus.IN_SYNC
        }

        val localUpdatedAt = localUser.localUpdatedAt ?: Instant.EPOCH
        val remoteUpdatedAt = remoteData.remoteUpdatedAt

        return when {
            localUser.hasPendingLocalChanges &&
                    localUpdatedAt.isAfter(remoteUpdatedAt) -> {

                uploadLocalUserToSupabase(
                    localUser = localUser,
                    supabaseUserId = supabaseUserId
                )

                userRepository.markUserSynced(localUserId)

                SyncStatus.CONFLICT_RESOLVED_FROM_LOCAL
            }

            else -> {
                applyRemoteDatasetToLocal(
                    localUser = localUser,
                    supabaseUserId = supabaseUserId,
                    remoteData = remoteData
                )

                SyncStatus.CONFLICT_RESOLVED_FROM_REMOTE
            }
        }
    }

    override suspend fun forceUploadLocalToSupabase(): SyncStatus {
        if (!isSignedIn()) {
            return SyncStatus.NOT_LINKED
        }

        val localUser = requireLocalUser()
        val supabaseUserId = requireSupabaseUserId()

        uploadLocalUserToSupabase(
            localUser = localUser,
            supabaseUserId = supabaseUserId
        )

        userRepository.markUserSynced(localUserId)

        return SyncStatus.CONFLICT_RESOLVED_FROM_LOCAL
    }

    override suspend fun forceDownloadFromSupabase(): SyncStatus {
        if (!isSignedIn()) {
            return SyncStatus.NOT_LINKED
        }

        val localUser = requireLocalUser()
        val supabaseUserId = requireSupabaseUserId()

        val remoteData = syncSnapshotRepository.fetchUserData(
            supabaseUserId = supabaseUserId,
            localUserId = localUserId
        )

        applyRemoteDatasetToLocal(
            localUser = localUser,
            supabaseUserId = supabaseUserId,
            remoteData = remoteData
        )

        return SyncStatus.CONFLICT_RESOLVED_FROM_REMOTE
    }

    override suspend fun addCollectedCatToSupabaseAndLocal(
        catId: String
    ) {
        val supabaseUserId = requireSupabaseUserId()
        val localUser = requireLocalUser()

        catRepository.addCollectedCat(
            userId = supabaseUserId,
            catId = catId
        )

        val updatedUser = localUser.copy(
            collectedCatsIds = (localUser.collectedCatsIds + catId).distinct(),
            isSupabaseLinked = true,
            supabaseUserId = supabaseUserId
        )

        userRepository.updateUserFromRemoteSync(updatedUser)
    }

    override suspend fun updateUsername(
        username: String
    ) {
        val supabaseUserId = requireSupabaseUserId()
        val localUser = requireLocalUser()

        profileRepository.updateUsername(
            userId = supabaseUserId,
            username = username
        )

        userRepository.updateUserFromRemoteSync(
            localUser.copy(
                username = username,
                isSupabaseLinked = true,
                supabaseUserId = supabaseUserId
            )
        )
    }

    override suspend fun updateAvatarPath(
        avatarPath: String?
    ) {
        val supabaseUserId = requireSupabaseUserId()
        val localUser = requireLocalUser()

        profileRepository.updateAvatarPath(
            userId = supabaseUserId,
            avatarPath = avatarPath
        )

        userRepository.updateUserFromRemoteSync(
            localUser.copy(
                profileImageUrl = avatarPath.toUrlOrNull(),
                isSupabaseLinked = true,
                supabaseUserId = supabaseUserId
            )
        )
    }

    override suspend fun resetTrackingSessions(userId: Int) {
        //Delete local tracking sessions
        trackingRepository.deleteAllTrackingSessions(userId)

        //Reset local goal statuses
        goalRepository.resetGoalsStatus(userId)

        //mark user as having pending local changes to ensure sync picks up the reset goals
        val localUser = requireLocalUser()
        userRepository.updateUserFromLocalAction(localUser)

        //Handle remote sync if signed in
        if (isSignedIn()) {
            val supabaseUserId = requireSupabaseUserId()

            //delete remote tracking sessions
            goalTrackingRepository.deleteTrackingSessions(supabaseUserId)

            //update remote goals with the reset status
            val localGoals = goalRepository.getGoalsForSync(userId)
            goalTrackingRepository.upsertGoals(supabaseUserId, localGoals)

            userRepository.markUserSynced(userId)
        }
    }

    override suspend fun getFriends(): List<FriendProfile> {
        return friendshipRepository.getFriends(
            userId = requireSupabaseUserId()
        )
    }

    override suspend fun getIncomingFriendRequests(): List<Friendship> {
        return friendshipRepository.getIncomingRequests(
            userId = requireSupabaseUserId()
        )
    }

    override suspend fun getOutgoingFriendRequests(): List<Friendship> {
        return friendshipRepository.getOutgoingRequests(
            userId = requireSupabaseUserId()
        )
    }

    override suspend fun sendFriendRequest(
        addresseeId: String
    ) {
        friendshipRepository.sendFriendRequest(
            currentUserId = requireSupabaseUserId(),
            friendUserId = addresseeId
        )
    }

    override suspend fun acceptFriendRequest(
        friendshipId: Long
    ) {
        friendshipRepository.acceptFriendRequest(friendshipId)
    }

    override suspend fun declineFriendRequest(
        friendshipId: Long
    ) {
        friendshipRepository.declineFriendRequest(friendshipId)
    }

    override suspend fun deleteFriendship(
        friendshipId: Long
    ) {
        friendshipRepository.deleteFriendship(friendshipId)
    }

    private suspend fun uploadLocalUserToSupabase(
        localUser: User,
        supabaseUserId: String
    ) {
        val localGoals =
            goalRepository.getGoalsForSync(localUserId)

        val localTrackingSessions =
            trackingRepository.getTrackingSessionsForSync(localUserId)

        syncSnapshotRepository.uploadUserData(
            supabaseUserId = supabaseUserId,
            localUser = localUser,
            goals = localGoals,
            trackingSessions = localTrackingSessions
        )
    }

    private suspend fun uploadLocalGoalsAndTrackingToSupabase(
        supabaseUserId: String
    ) {
        val localGoals =
            goalRepository.getGoalsForSync(localUserId)

        val localTrackingSessions =
            trackingRepository.getTrackingSessionsForSync(localUserId)

        syncSnapshotRepository.uploadGoalsAndTracking(
            supabaseUserId = supabaseUserId,
            goals = localGoals,
            trackingSessions = localTrackingSessions
        )
    }

    private suspend fun applyRemoteDatasetToLocal(
        localUser: User,
        supabaseUserId: String,
        remoteData: SupabaseRemoteUserData
    ) {
        val remoteUser = localUser.copy(
            username = remoteData.profile.username,
            profileImageUrl = remoteData.profile.avatarPath.toUrlOrNull(),
            collectedCatsIds = remoteData.collectedCatIds,
            selectedCatIds = remoteData.selectedCatIds,
            isSupabaseLinked = true,
            supabaseUserId = supabaseUserId
        )

        userRepository.updateUserFromRemoteSync(remoteUser)

        goalRepository.replaceGoalsFromRemoteSync(
            userId = localUserId,
            goals = remoteData.goals
        )

        trackingRepository.replaceTrackingSessionsFromRemoteSync(
            userId = localUserId,
            sessions = remoteData.trackingSessions
        )
    }

    private suspend fun requireLocalUser(): User {
        return userRepository.getUser(localUserId).firstOrNull()
            ?: error("Local user $localUserId does not exist.")
    }

    private fun requireSupabaseUserId(): String {
        return currentSupabaseUserId()
            ?: error("No authenticated Supabase user.")
    }

    private fun String?.toUrlOrNull(): URL? {
        return this
            ?.takeIf { value -> value.isNotBlank() }
            ?.let { value ->
                runCatching { URL(value) }.getOrNull()
            }
    }

    private fun mergeGoals(
        localGoals: List<Goal>,
        remoteGoals: List<Goal>,
        localWinsOnConflict: Boolean
    ): List<Goal> {
        val result = linkedMapOf<Int, Goal>()

        if (localWinsOnConflict) {
            remoteGoals.forEach { goal ->
                result[goal.id] = goal
            }

            localGoals.forEach { goal ->
                result[goal.id] = goal
            }
        } else {
            localGoals.forEach { goal ->
                result[goal.id] = goal
            }

            remoteGoals.forEach { goal ->
                result[goal.id] = goal
            }
        }

        return result.values.toList()
    }

    private fun mergeTrackingSessions(
        localSessions: List<TrackingSession>,
        remoteSessions: List<TrackingSession>,
        localWinsOnConflict: Boolean
    ): List<TrackingSession> {
        val result = linkedMapOf<Int, TrackingSession>()

        if (localWinsOnConflict) {
            remoteSessions.forEach { session ->
                result[session.id] = session
            }

            localSessions.forEach { session ->
                result[session.id] = session
            }
        } else {
            localSessions.forEach { session ->
                result[session.id] = session
            }

            remoteSessions.forEach { session ->
                result[session.id] = session
            }
        }

        return result.values.toList()
    }

    private data class SyncComparableUserData(
        val username: String,
        val profileImageUrl: String?,
        val collectedCatIds: List<String>,
        val selectedCatIds: List<String>,
        val goalSignatures: List<String>,
        val trackingSessionSignatures: List<String>
    ) {
        companion object {
            fun fromLocal(
                user: User,
                goals: List<Goal>,
                trackingSessions: List<TrackingSession>
            ): SyncComparableUserData {
                return SyncComparableUserData(
                    username = user.username,
                    profileImageUrl = user.profileImageUrl?.toString(),
                    collectedCatIds = user.collectedCatsIds.sorted(),
                    selectedCatIds = user.selectedCatIds,
                    goalSignatures = goals
                        .sortedBy { goal -> goal.id }
                        .map { goal ->
                            listOf(
                                goal.id,
                                goal.title,
                                goal.type,
                                goal.targetDuration,
                                goal.deepFocus,
                                goal.inactive,
                                goal.createdAt,
                                goal.isCompleted,
                                goal.lastCompletedAt
                            ).joinToString("|")
                        },
                    trackingSessionSignatures = trackingSessions
                        .sortedBy { session -> session.id }
                        .map { session ->
                            listOf(
                                session.id,
                                session.goalId,
                                session.userId,
                                session.pauseReminder,
                                session.deepFocus,
                                session.startTime,
                                session.endTime
                            ).joinToString("|")
                        }
                )
            }

            fun fromRemote(
                remoteData: SupabaseRemoteUserData
            ): SyncComparableUserData {
                return SyncComparableUserData(
                    username = remoteData.profile.username,
                    profileImageUrl = remoteData.profile.avatarPath,
                    collectedCatIds = remoteData.collectedCatIds.sorted(),
                    selectedCatIds = remoteData.selectedCatIds,
                    goalSignatures = remoteData.goals
                        .sortedBy { goal -> goal.id }
                        .map { goal ->
                            listOf(
                                goal.id,
                                goal.title,
                                goal.type,
                                goal.targetDuration,
                                goal.deepFocus,
                                goal.inactive,
                                goal.createdAt,
                                goal.isCompleted,
                                goal.lastCompletedAt
                            ).joinToString("|")
                        },
                    trackingSessionSignatures = remoteData.trackingSessions
                        .sortedBy { session -> session.id }
                        .map { session ->
                            listOf(
                                session.id,
                                session.goalId,
                                session.userId,
                                session.pauseReminder,
                                session.deepFocus,
                                session.startTime,
                                session.endTime
                            ).joinToString("|")
                        }
                )
            }
        }
    }
}