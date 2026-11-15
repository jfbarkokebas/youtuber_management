package br.com.felipe.creator.infrastructure.persistence

import br.com.felipe.creator.domain.task.Task
import br.com.felipe.creator.domain.task.TaskRepository
import br.com.felipe.creator.domain.task.TaskStatus
import java.sql.Statement

class TaskRepositoryMySQL: TaskRepository {

    override fun create(task: Task): Long {

        val sql = """
            INSERT INTO tasks (title, description, status)
            VALUES (?, ?, ?)
            """.trimIndent()

        DatabaseFactory.getConnection().use { connection ->

            connection.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
            ).use { stmt ->

                stmt.setString(1, task.title)
                stmt.setString(2, task.description)
                stmt.setString(3, task.status.name)

                stmt.executeUpdate()

                stmt.generatedKeys.use { keys ->
                    if (keys.next()) {
                        return keys.getLong(1)
                    }
                }
            }
        }

        throw IllegalStateException(
            "Não foi possível obter o ID da tarefa criada."
        )
    }

    override fun findAll(): List<Task> {

        val sql = """
            SELECT id, title, description, status
            FROM tasks
            ORDER BY id DESC
            """.trimIndent()

        val tasks = mutableListOf<Task>()

        DatabaseFactory.getConnection().use { connection ->

            connection.prepareStatement(sql).use { statement ->

                statement.executeQuery().use { result ->

                    while (result.next()) {

                        val task = Task(
                            id = result.getLong("id"),
                            title = result.getString("title"),
                            description = result.getString("description"),
                            status = TaskStatus.valueOf(
                                result.getString("status")
                            )
                        )

                        tasks.add(task)
                    }
                }
            }
        }

        return tasks
    }

    override fun findById(id: Long): Task? {

        val sql = """
            SELECT id, title, description, status
            FROM tasks
            WHERE id = ?
            """.trimIndent()

        DatabaseFactory.getConnection().use { connection ->

            connection.prepareStatement(sql).use { stmt ->

                stmt.setLong(1, id)

                stmt.executeQuery().use { result ->

                    if (result.next()) {

                        return Task(
                            id = result.getLong("id"),
                            title = result.getString("title"),
                            description = result.getString("description"),
                            status = TaskStatus.valueOf(
                                result.getString("status")
                            )
                        )
                    }
                }
            }
        }

        return null
    }

    override fun update(task: Task): Boolean {

        val sql = """
            UPDATE tasks
            SET title = ?, description = ?, status = ?
            WHERE id = ?
            """.trimIndent()

        DatabaseFactory.getConnection().use { connection ->

            connection.prepareStatement(sql).use { stmt ->

                stmt.setString(1, task.title)
                stmt.setString(2, task.description)
                stmt.setString(3, task.status.name)
                stmt.setLong(4, task.id)

                val rowsAffected = stmt.executeUpdate()

                return rowsAffected > 0
            }
        }
    }

    override fun delete(id: Long): Boolean {

        val sql = """
            DELETE FROM tasks
            WHERE id = ?
            """.trimIndent()

        DatabaseFactory.getConnection().use { connection ->

            connection.prepareStatement(sql).use { statement ->

                statement.setLong(1, id)

                val rowsAffected = statement.executeUpdate()

                return rowsAffected > 0
            }
        }
    }
}