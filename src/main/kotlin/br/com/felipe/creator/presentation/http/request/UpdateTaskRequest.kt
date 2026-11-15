package br.com.felipe.creator.presentation.http.request

import br.com.felipe.creator.domain.task.TaskStatus
import kotlinx.serialization.Serializable

@Serializable
data class UpdateTaskRequest (
    val title: String,
    val description: String?,
    val status: TaskStatus
)