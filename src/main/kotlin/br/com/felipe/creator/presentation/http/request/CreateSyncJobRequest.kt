
package br.com.felipe.creator.presentation.http.requests

import kotlinx.serialization.Serializable

@Serializable
data class CreateSyncJobRequest(
    val totalItems: Int
)
