
package br.com.felipe.creator.presentation.http.response

import br.com.felipe.creator.domain.sync.SyncJobStatus
import kotlinx.serialization.Serializable

@Serializable
data class CreateSyncJobResponse(
    val id: Long,
    val status: SyncJobStatus,
    val totalItems: Int,
    val processedItems: Int
)
