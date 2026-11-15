
package br.com.felipe.creator.infrastructure.persistence

import br.com.felipe.creator.domain.sync.SyncJob
import br.com.felipe.creator.domain.sync.SyncJobRepository
import br.com.felipe.creator.domain.sync.SyncJobStatus
import java.sql.Timestamp

class SyncJobRepositoryMySQL : SyncJobRepository {

    override fun create(job: SyncJob): Long {
        val sql = """
            INSERT INTO sync_jobs (
                status,
                total_items,
                processed_items,
                created_at,
                started_at,
                finished_at
            ) VALUES (?, ?, ?, ?, ?, ?)
        """.trimIndent()

        DatabaseFactory.getConnection().use { connection ->
            connection.prepareStatement(
                sql,
                java.sql.Statement.RETURN_GENERATED_KEYS
            ).use { statement ->

                statement.setString(1, job.status.name)
                statement.setInt(2, job.totalItems)
                statement.setInt(3, job.processedItems)
                statement.setTimestamp(4, Timestamp.valueOf(job.createdAt))

                if (job.startedAt != null) {
                    statement.setTimestamp(
                        5,
                        Timestamp.valueOf(job.startedAt)
                    )
                } else {
                    statement.setTimestamp(5, null)
                }

                if (job.finishedAt != null) {
                    statement.setTimestamp(
                        6,
                        Timestamp.valueOf(job.finishedAt)
                    )
                } else {
                    statement.setTimestamp(6, null)
                }

                statement.executeUpdate()

                statement.generatedKeys.use { keys ->
                    if (keys.next()) {
                        return keys.getLong(1)
                    }
                }
            }
        }

        error("Não foi possível obter o ID do SyncJob criado")
    }

    override fun findById(id: Long): SyncJob? {
        val sql = """
            SELECT *
            FROM sync_jobs
            WHERE id = ?
        """.trimIndent()

        DatabaseFactory.getConnection().use { connection ->
            connection.prepareStatement(sql).use { statement ->

                statement.setLong(1, id)

                statement.executeQuery().use { result ->
                    if (result.next()) {
                        return mapRow(result)
                    }
                }
            }
        }

        return null
    }

    override fun findPending(): List<SyncJob> {
        val sql = """
            SELECT *
            FROM sync_jobs
            WHERE status = ?
            ORDER BY created_at ASC, id ASC
        """.trimIndent()

        val jobs = mutableListOf<SyncJob>()

        DatabaseFactory.getConnection().use { connection ->
            connection.prepareStatement(sql).use { stmt ->

                stmt.setString(1, SyncJobStatus.PENDING.name)

                stmt.executeQuery().use { result ->
                    while (result.next()) {
                        jobs.add(mapRow(result))
                    }
                }
            }
        }

        return jobs
    }

    override fun update(job: SyncJob): Boolean {
        val sql = """
            UPDATE sync_jobs
            SET
                status = ?,
                total_items = ?,
                processed_items = ?,
                created_at = ?,
                started_at = ?,
                finished_at = ?
            WHERE id = ?
        """.trimIndent()

        DatabaseFactory.getConnection().use { connection ->
            connection.prepareStatement(sql).use { stmt ->

                stmt.setString(1, job.status.name)
                stmt.setInt(2, job.totalItems)
                stmt.setInt(3, job.processedItems)
                stmt.setTimestamp(4, Timestamp.valueOf(job.createdAt))

                stmt.setTimestamp(
                    5,
                    job.startedAt?.let { Timestamp.valueOf(it) }
                )

                stmt.setTimestamp(
                    6,
                    job.finishedAt?.let { Timestamp.valueOf(it) }
                )

                stmt.setLong(7, job.id)

                return stmt.executeUpdate() > 0
            }
        }
    }

    private fun mapRow(result: java.sql.ResultSet): SyncJob {
        return SyncJob(
            id = result.getLong("id"),
            status = SyncJobStatus.valueOf(
                result.getString("status")
            ),
            totalItems = result.getInt("total_items"),
            processedItems = result.getInt("processed_items"),
            createdAt = result.getTimestamp("created_at").toLocalDateTime(),
            startedAt = result.getTimestamp("started_at")?.toLocalDateTime(),
            finishedAt = result.getTimestamp("finished_at")?.toLocalDateTime()
        )
    }
}
