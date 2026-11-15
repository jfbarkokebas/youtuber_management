
package br.com.felipe.creator.domain.sync

interface SyncJobRepository {

    fun create(job: SyncJob): Long

    fun findById(id: Long): SyncJob?

    fun findPending(): List<SyncJob>

    fun update(job: SyncJob): Boolean
}
