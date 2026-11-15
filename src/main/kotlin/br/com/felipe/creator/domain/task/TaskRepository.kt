
package br.com.felipe.creator.domain.task

interface TaskRepository {

    fun create(task: Task): Long

    fun findAll(): List<Task>

    fun findById(id: Long): Task?

    fun update(task: Task): Boolean

    fun delete(id: Long): Boolean
}
