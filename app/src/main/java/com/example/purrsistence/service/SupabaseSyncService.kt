package com.example.purrsistence.service

import com.example.purrsistence.data.local.mapping.toDomain
import com.example.purrsistence.data.local.mapping.toSupabaseDto
import com.example.purrsistence.data.local.repository.GoalRepository
import com.example.purrsistence.data.local.repository.TrackingRepository
import com.example.purrsistence.data.local.repository.UserRepository
import com.example.purrsistence.data.remote.supabase.datasource.SupabaseAuthRemoteDataSource
import com.example.purrsistence.data.remote.supabase.datasource.SupabaseCatRemoteDataSource
import com.example.purrsistence.data.remote.supabase.datasource.SupabaseFriendshipRemoteDataSource
import com.example.purrsistence.data.remote.supabase.datasource.SupabaseGoalTrackingRemoteDataSource
import com.example.purrsistence.data.remote.supabase.datasource.SupabaseProfileRemoteDataSource
import com.example.purrsistence.data.remote.supabase.dto.FriendshipDto
import com.example.purrsistence.data.remote.supabase.dto.GoalsDto
import com.example.purrsistence.data.remote.supabase.dto.ProfileDto
import com.example.purrsistence.data.remote.supabase.dto.TrackingSessionDto
import com.example.purrsistence.domain.model.User
import com.example.purrsistence.domain.model.types.SyncStatus
import kotlinx.coroutines.flow.firstOrNull
import java.net.URL
import java.time.Instant
import java.time.OffsetDateTime

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
    suspend fun getFriends(): List<ProfileDto>
    suspend fun getIncomingFriendRequests(): List<FriendshipDto>
    suspend fun getOutgoingFriendRequests(): List<FriendshipDto>
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
    private val authRemoteDataSource: SupabaseAuthRemoteDataSource,
    private val profileRemoteDataSource: SupabaseProfileRemoteDataSource,
    private val catRemoteDataSource: SupabaseCatRemoteDataSource,
    private val friendshipRemoteDataSource: SupabaseFriendshipRemoteDataSource,
    private val goalTrackingRemoteDataSource: SupabaseGoalTrackingRemoteDataSource,
    private val goalRepository: GoalRepository,
    private val trackingRepository: TrackingRepository,
    private val localUserId: Int = 1
) : TrackingSyncService {

    override fun isSignedIn(): Boolean {
        return authRemoteDataSource.currentUserId() != null
    }

    override fun currentSupabaseUserId(): String? {
        return authRemoteDataSource.currentUserId()
    }

    override suspend fun signUp(
        email: String,
        password: String,
        username: String
    ) {
        authRemoteDataSource.signUp(
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
        authRemoteDataSource.signIn(
            email = email,
            password = password
        )

        syncUserAfterSignIn()
    }

    override suspend fun signOut() {
        authRemoteDataSource.signOut()
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
        val remoteData = fetchRemoteUserData(supabaseUserId)

        val localUpdatedAt = localUser.localUpdatedAt ?: Instant.EPOCH
        val remoteUpdatedAt = parseSupabaseTimestamp(remoteData.remoteUpdatedAt)

        val useLocalData =
            localUser.hasPendingLocalChanges &&
                    localUpdatedAt.isAfter(remoteUpdatedAt)

        val mergedCollectedCatIds =
            (localUser.collectedCatsIds + remoteData.collectedCatIds)
                .distinct()

        val mergedSelectedCatIds =
            (localUser.selectedCatIds + remoteData.selectedCatIds)
                .distinct()
                .filter { it in mergedCollectedCatIds }
                .take(3)

        val mergedUsername =
            if (useLocalData) localUser.username else remoteData.profile.username

        val mergedAvatarPath =
            if (useLocalData) {
                localUser.profileImageUrl?.toString()
            } else {
                remoteData.profile.avatarPath
            }

        val localGoals = goalRepository.getGoalsForSync(localUserId)
        val localTrackingSessions =
            trackingRepository.getTrackingSessionsForSync(localUserId)

        val localGoalDtos =
            localGoals.map { it.toSupabaseDto(supabaseUserId) }

        val localTrackingDtos =
            localTrackingSessions.map { it.toSupabaseDto(supabaseUserId) }

        val mergedGoals =
            mergeGoalDtos(
                localGoals = localGoalDtos,
                remoteGoals = remoteData.goals,
                localWinsOnConflict = useLocalData
            )

        val mergedTrackingSessions =
            mergeTrackingSessionDtos(
                localSessions = localTrackingDtos,
                remoteSessions = remoteData.trackingSessions,
                localWinsOnConflict = useLocalData
            )

        profileRemoteDataSource.updateUsername(
            userId = supabaseUserId,
            username = mergedUsername
        )

        profileRemoteDataSource.updateAvatarPath(
            userId = supabaseUserId,
            avatarPath = mergedAvatarPath
        )

        catRemoteDataSource.uploadLocalCollectedCats(
            userId = supabaseUserId,
            catIds = mergedCollectedCatIds
        )

        catRemoteDataSource.replaceSelectedCats(
            userId = supabaseUserId,
            selectedCatIds = mergedSelectedCatIds
        )

        goalTrackingRemoteDataSource.upsertGoals(mergedGoals)
        goalTrackingRemoteDataSource.upsertTrackingSessions(mergedTrackingSessions)

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
            goals = mergedGoals.map { it.toDomain(localUserId) }
        )

        trackingRepository.replaceTrackingSessionsFromRemoteSync(
            userId = localUserId,
            sessions = mergedTrackingSessions.map { it.toDomain(localUserId) }
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
        val remoteData = fetchRemoteUserData(supabaseUserId)

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
        val remoteUpdatedAt = parseSupabaseTimestamp(remoteData.remoteUpdatedAt)

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
        val remoteData = fetchRemoteUserData(supabaseUserId)

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

        catRemoteDataSource.addCollectedCat(
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

        profileRemoteDataSource.updateUsername(
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

        profileRemoteDataSource.updateAvatarPath(
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

    override suspend fun getFriends(): List<ProfileDto> {
        return friendshipRemoteDataSource.fetchAcceptedFriendProfiles(
            userId = requireSupabaseUserId()
        )
    }

    override suspend fun getIncomingFriendRequests(): List<FriendshipDto> {
        return friendshipRemoteDataSource.fetchIncomingRequests(
            userId = requireSupabaseUserId()
        )
    }

    override suspend fun getOutgoingFriendRequests(): List<FriendshipDto> {
        return friendshipRemoteDataSource.fetchOutgoingRequests(
            userId = requireSupabaseUserId()
        )
    }

    override suspend fun sendFriendRequest(
        addresseeId: String
    ) {
        friendshipRemoteDataSource.sendFriendRequest(
            currentUserId = requireSupabaseUserId(),
            friendUserId = addresseeId
        )
    }

    override suspend fun acceptFriendRequest(
        friendshipId: Long
    ) {
        friendshipRemoteDataSource.acceptFriendRequest(friendshipId)
    }

    override suspend fun declineFriendRequest(
        friendshipId: Long
    ) {
        friendshipRemoteDataSource.declineFriendRequest(friendshipId)
    }

    override suspend fun deleteFriendship(
        friendshipId: Long
    ) {
        friendshipRemoteDataSource.deleteFriendship(friendshipId)
    }

    private suspend fun fetchRemoteUserData(
        supabaseUserId: String
    ): RemoteUserData {
        val profile =
            profileRemoteDataSource.fetchProfile(supabaseUserId)

        val collectedCatIds =
            catRemoteDataSource.fetchCollectedCatIds(supabaseUserId)
                .distinct()

        val selectedCatIds =
            catRemoteDataSource.fetchSelectedCatIds(supabaseUserId)
                .distinct()
                .take(3)

        val goals =
            goalTrackingRemoteDataSource.fetchGoals(supabaseUserId)

        val trackingSessions =
            goalTrackingRemoteDataSource.fetchTrackingSessions(supabaseUserId)

        val syncState =
            profileRemoteDataSource.fetchUserSyncState(supabaseUserId)

        return RemoteUserData(
            profile = profile,
            collectedCatIds = collectedCatIds,
            selectedCatIds = selectedCatIds,
            goals = goals,
            trackingSessions = trackingSessions,
            remoteUpdatedAt = syncState.remoteUpdatedAt
        )
    }

    private suspend fun uploadLocalUserToSupabase(
        localUser: User,
        supabaseUserId: String
    ) {
        profileRemoteDataSource.updateUsername(
            userId = supabaseUserId,
            username = localUser.username
        )

        profileRemoteDataSource.updateAvatarPath(
            userId = supabaseUserId,
            avatarPath = localUser.profileImageUrl?.toString()
        )

        catRemoteDataSource.uploadLocalCollectedCats(
            userId = supabaseUserId,
            catIds = localUser.collectedCatsIds
        )

        catRemoteDataSource.replaceSelectedCats(
            userId = supabaseUserId,
            selectedCatIds = localUser.selectedCatIds
        )

        uploadLocalGoalsAndTrackingToSupabase(
            supabaseUserId = supabaseUserId
        )
    }

    private suspend fun uploadLocalGoalsAndTrackingToSupabase(
        supabaseUserId: String
    ) {
        val localGoals =
            goalRepository.getGoalsForSync(localUserId)

        val localTrackingSessions =
            trackingRepository.getTrackingSessionsForSync(localUserId)

        goalTrackingRemoteDataSource.upsertGoals(
            localGoals.map { goal ->
                goal.toSupabaseDto(supabaseUserId)
            }
        )

        goalTrackingRemoteDataSource.upsertTrackingSessions(
            localTrackingSessions.map { session ->
                session.toSupabaseDto(supabaseUserId)
            }
        )
    }

    private suspend fun applyRemoteDatasetToLocal(
        localUser: User,
        supabaseUserId: String,
        remoteData: RemoteUserData
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
            goals = remoteData.goals.map { goal ->
                goal.toDomain(localUserId)
            }
        )

        trackingRepository.replaceTrackingSessionsFromRemoteSync(
            userId = localUserId,
            sessions = remoteData.trackingSessions.map { session ->
                session.toDomain(localUserId)
            }
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

    private fun parseSupabaseTimestamp(value: String): Instant {
        return runCatching {
            Instant.parse(value)
        }.getOrElse {
            OffsetDateTime.parse(value).toInstant()
        }
    }

    private fun String?.toUrlOrNull(): URL? {
        return this
            ?.takeIf { it.isNotBlank() }
            ?.let { value ->
                runCatching { URL(value) }.getOrNull()
            }
    }

    private fun mergeGoalDtos(
        localGoals: List<GoalsDto>,
        remoteGoals: List<GoalsDto>,
        localWinsOnConflict: Boolean
    ): List<GoalsDto> {
        val result = linkedMapOf<Int, GoalsDto>()

        if (localWinsOnConflict) {
            remoteGoals.forEach { result[it.goalId] = it }
            localGoals.forEach { result[it.goalId] = it }
        } else {
            localGoals.forEach { result[it.goalId] = it }
            remoteGoals.forEach { result[it.goalId] = it }
        }

        return result.values.toList()
    }

    private fun mergeTrackingSessionDtos(
        localSessions: List<TrackingSessionDto>,
        remoteSessions: List<TrackingSessionDto>,
        localWinsOnConflict: Boolean
    ): List<TrackingSessionDto> {
        val result = linkedMapOf<Int, TrackingSessionDto>()

        if (localWinsOnConflict) {
            remoteSessions.forEach { result[it.trackingId] = it }
            localSessions.forEach { result[it.trackingId] = it }
        } else {
            localSessions.forEach { result[it.trackingId] = it }
            remoteSessions.forEach { result[it.trackingId] = it }
        }

        return result.values.toList()
    }

    private data class RemoteUserData(
        val profile: ProfileDto,
        val collectedCatIds: List<String>,
        val selectedCatIds: List<String>,
        val goals: List<GoalsDto>,
        val trackingSessions: List<TrackingSessionDto>,
        val remoteUpdatedAt: String
    )

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
                goals: List<com.example.purrsistence.domain.model.Goal>,
                trackingSessions: List<com.example.purrsistence.domain.model.TrackingSession>
            ): SyncComparableUserData {
                return SyncComparableUserData(
                    username = user.username,
                    profileImageUrl = user.profileImageUrl?.toString(),
                    collectedCatIds = user.collectedCatsIds.sorted(),
                    selectedCatIds = user.selectedCatIds,
                    goalSignatures = goals
                        .sortedBy { it.id }
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
                        .sortedBy { it.id }
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
                remoteData: RemoteUserData
            ): SyncComparableUserData {
                return SyncComparableUserData(
                    username = remoteData.profile.username,
                    profileImageUrl = remoteData.profile.avatarPath,
                    collectedCatIds = remoteData.collectedCatIds.sorted(),
                    selectedCatIds = remoteData.selectedCatIds,
                    goalSignatures = remoteData.goals
                        .sortedBy { it.goalId }
                        .map { goal ->
                            listOf(
                                goal.goalId,
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
                        .sortedBy { it.trackingId }
                        .map { session ->
                            listOf(
                                session.trackingId,
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