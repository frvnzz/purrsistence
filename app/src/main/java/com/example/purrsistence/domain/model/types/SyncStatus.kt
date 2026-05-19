package com.example.purrsistence.domain.model.types

enum class SyncStatus {
    NOT_LINKED,
    IN_SYNC,
    CONFLICT_RESOLVED_FROM_LOCAL,
    CONFLICT_RESOLVED_FROM_REMOTE
}