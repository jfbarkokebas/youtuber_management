package br.com.felipe.creator.domain.sync

interface SyncJobRepository {

    fun create(job: SyncJob): Long

    fun findById(id: Long): SyncJob?

    fun findPending(): List<SyncJob>

    fun acquireNext(
        workerId: String,
        leaseDurationSeconds: Long
    ): SyncJob?

    fun updateProgress(
        jobId: Long,
        workerId: String,
        fencingToken: Long,
        processedItems: Int
    ): Boolean

    fun renewLease(
        jobId: Long,
        workerId: String,
        fencingToken: Long,
        leaseDurationSeconds: Long
    ): Boolean

    fun completeJob(
        jobId: Long,
        workerId: String,
        fencingToken: Long
    ): Boolean

    fun failJob(
        jobId: Long,
        workerId: String,
        fencingToken: Long
    ): Boolean
}

