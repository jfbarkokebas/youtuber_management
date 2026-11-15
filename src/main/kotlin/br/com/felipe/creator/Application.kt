
package br.com.felipe.creator

import br.com.felipe.creator.bootstrap.DependencyContainer
import br.com.felipe.creator.presentation.http.plugins.configureCors
import br.com.felipe.creator.presentation.http.plugins.configureSerialization
import br.com.felipe.creator.presentation.http.routes.configureRouting
import io.ktor.server.application.*

fun Application.configureApplication() {

    configureCors();

    configureSerialization();

    val taskService = DependencyContainer.createTaskService();
    val syncJobService = DependencyContainer.createSyncJobService()

    configureRouting(taskService,syncJobService);
}
