package br.com.felipe.creator.domain.task

import kotlinx.serialization.Serializable

@Serializable
data class Task (
    val id: Long,
    val title: String,
    val description: String?,
    val status: TaskStatus
)