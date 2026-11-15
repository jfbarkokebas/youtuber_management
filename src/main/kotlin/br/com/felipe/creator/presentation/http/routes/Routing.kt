package br.com.felipe.creator.presentation.http.routes

import br.com.felipe.creator.application.sync.SyncJobService
import br.com.felipe.creator.application.task.TaskService
import br.com.felipe.creator.domain.sync.SyncJobStatus
import br.com.felipe.creator.domain.task.Task
import br.com.felipe.creator.presentation.http.request.CreateTaskRequest
import br.com.felipe.creator.presentation.http.request.UpdateTaskRequest
import br.com.felipe.creator.presentation.http.requests.CreateSyncJobRequest
import br.com.felipe.creator.presentation.http.response.CreateSyncJobResponse
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.http.*

fun Application.configureRouting(
    taskService: TaskService,
    syncJobService: SyncJobService
) {

    routing {
        get("/") {
            call.respondText("API de tarefas funcionando!")
        }

        get("/api/tasks") {

            val tasks = taskService.findAll()

            call.respond(tasks)
        }


        get("/api/tasks/{id}") {

            // Recupera o ID informado na URL
            val id = call.parameters["id"]?.toLongOrNull()

            // Verifica se o ID é válido
            if (id == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "ID inválido")
                )
                return@get
            }

            // Consulta a tarefa
            val task = taskService.findById(id)

            // Verifica se a tarefa existe
            if (task == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Tarefa não encontrada")
                )
                return@get
            }

            // Retorna a tarefa encontrada
            call.respond(task)
        }


        post("/api/tasks") {

            val request = call.receive<CreateTaskRequest>()

            val task = Task(
                id = 0L,
                title = request.title,
                description = request.description,
                status = request.status
            )

            val id = taskService.create(task)

            call.respond(
                HttpStatusCode.Created,
                mapOf("id" to id)
            )
        }

        post("/api/sync/jobs") {

            val request = call.receive<CreateSyncJobRequest>()

            if (request.totalItems < 0) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "A quantidade de itens não pode ser negativa")
                )
                return@post
            }

            val id = syncJobService.createJob(request.totalItems)

            val responseDto = CreateSyncJobResponse(
                id = id,
                status = SyncJobStatus.PENDING,
                totalItems = request.totalItems,
                processedItems = 0
            )

            call.respond(
                HttpStatusCode.Created,
                responseDto
            )
        }

        put("/api/tasks/{id}") {

            val id = call.parameters["id"]?.toLongOrNull()

            if (id == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "ID inválido")
                )
                return@put
            }

            val request = call.receive<UpdateTaskRequest>()

            val existingTask = taskService.findById(id)

            if (existingTask == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Tarefa não encontrada")
                )
                return@put
            }

            val updatedTask = Task(
                id = id,
                title = request.title,
                description = request.description,
                status = request.status
            )

            taskService.update(updatedTask)

            call.respond(
                HttpStatusCode.OK,
                mapOf("message" to "Tarefa atualizada com sucesso")
            )
        }


        delete("/api/tasks/{id}") {

            val id = call.parameters["id"]?.toLongOrNull()

            if (id == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "ID inválido")
                )
                return@delete
            }

            val existingTask = taskService.findById(id)

            if (existingTask == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Tarefa não encontrada")
                )
                return@delete
            }

            taskService.delete(id)

            call.respond(HttpStatusCode.NoContent)
        }


    }

}