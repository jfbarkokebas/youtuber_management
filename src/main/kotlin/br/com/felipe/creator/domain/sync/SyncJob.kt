package br.com.felipe.creator.domain.sync

import java.time.LocalDateTime

data class SyncJob(
    val id: Long,
    val status: SyncJobStatus,
    val totalItems: Int,
    val processedItems: Int,
    val createdAt: LocalDateTime,
    val startedAt: LocalDateTime?,
    val finishedAt: LocalDateTime?,
    val workerId: String?,
    val lockedUntil: LocalDateTime?,
    val fencingToken: Long
)