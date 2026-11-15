
package br.com.felipe.creator.bootstrap

import br.com.felipe.creator.application.sync.SyncJobService
import br.com.felipe.creator.application.task.TaskService
import br.com.felipe.creator.infrastructure.persistence.SyncJobRepositoryMySQL
import br.com.felipe.creator.infrastructure.persistence.TaskRepositoryMySQL

object DependencyContainer {

    fun createTaskService(): TaskService {
        val taskRepository = TaskRepositoryMySQL()

        return TaskService(taskRepository)
    }

    fun createSyncJobService(): SyncJobService {
        val syncJobRepository = SyncJobRepositoryMySQL()
        return SyncJobService(syncJobRepository)
    }
}
