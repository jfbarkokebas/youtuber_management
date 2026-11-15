
package br.com.felipe.creator.application.sync

import br.com.felipe.creator.domain.sync.SyncJob
import br.com.felipe.creator.domain.sync.SyncJobRepository
import br.com.felipe.creator.domain.sync.SyncJobStatus
import java.time.LocalDateTime

class SyncJobService(
    private val repository: SyncJobRepository
) {

    fun createJob(totalItems: Int): Long {

        require(totalItems >= 0) {
            "A quantidade de itens não pode ser negativa"
        }

        val job = SyncJob(
            id = 0L,
            status = SyncJobStatus.PENDING,
            totalItems = totalItems,
            processedItems = 0,
            createdAt = LocalDateTime.now(),
            startedAt = null,
            finishedAt = null
        )

        return repository.create(job)
    }

    fun findJob(id: Long): SyncJob? {
        require(id > 0) {
            "O ID do processamento deve ser maior que zero"
        }

        return repository.findById(id)
    }

    fun findPendingJobs(): List<SyncJob> {
        return repository.findPending()
    }
}
