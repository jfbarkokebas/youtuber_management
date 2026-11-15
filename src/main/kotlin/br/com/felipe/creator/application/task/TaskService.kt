package br.com.felipe.creator.application.task

import br.com.felipe.creator.domain.task.Task
import br.com.felipe.creator.domain.task.TaskRepository

class TaskService(
    private val taskRepository: TaskRepository
) {

    fun create(task: Task): Long {

        require(task.title.isNotBlank()) {
            "O título da tarefa é obrigatório."
        }

        require(task.title.length <= 150) {
            "O título deve ter no máximo 150 caracteres."
        }

        return taskRepository.create(task)
    }

    fun findAll(): List<Task> {
        return taskRepository.findAll()
    }

    fun findById(id: Long): Task? {
        return taskRepository.findById(id)
    }

    fun update(task: Task): Boolean {

        require(task.title.isNotBlank()) {
            "O título da tarefa é obrigatório."
        }

        require(task.title.length <= 150) {
            "O título deve ter no máximo 150 caracteres."
        }

        return taskRepository.update(task)
    }

    fun delete(id: Long): Boolean {
        return taskRepository.delete(id)
    }


}
