package br.com.felipe.creator.infrastructure.persistence

import br.com.felipe.creator.domain.sync.SyncJob
import br.com.felipe.creator.domain.sync.SyncJobRepository
import br.com.felipe.creator.domain.sync.SyncJobStatus
import java.sql.Connection
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
                        finished_at,
                        worker_id,
                        locked_until,
                        fencing_token
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
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

                statement.setString(7, job.workerId)

                statement.setTimestamp(
                    8,
                    job.lockedUntil?.let { Timestamp.valueOf(it) }
                )

                statement.setLong(9, job.fencingToken)

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
            SELECT
                id,
                status,
                total_items,
                processed_items,
                created_at,
                started_at,
                finished_at,
                worker_id,
                locked_until,
                fencing_token
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
            SELECT
                id,
                status,
                total_items,
                processed_items,
                created_at,
                started_at,
                finished_at,
                worker_id,
                locked_until,
                fencing_token
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



    override fun updateProgress(
        jobId: Long,
        workerId: String,
        fencingToken: Long,
        processedItems: Int
    ): Boolean {
        require(jobId > 0)
        require(workerId.isNotBlank())
        require(fencingToken > 0)
        require(processedItems >= 0)

        val sql = """
        UPDATE sync_jobs
        SET processed_items = ?
        WHERE id = ?
          AND status = 'RUNNING'
          AND worker_id = ?
          AND fencing_token = ?
          AND locked_until > CURRENT_TIMESTAMP
          AND processed_items <= ?
          AND total_items >= ?
    """.trimIndent()

        return DatabaseFactory.getConnection().use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setInt(1, processedItems)
                statement.setLong(2, jobId)
                statement.setString(3, workerId)
                statement.setLong(4, fencingToken)
                statement.setInt(5, processedItems)
                statement.setInt(6, processedItems)

                statement.executeUpdate() == 1
            }
        }
    }



    override fun renewLease(
        jobId: Long,
        workerId: String,
        fencingToken: Long,
        leaseDurationSeconds: Long
    ): Boolean {

        require(jobId > 0) {
            "O ID do processamento deve ser maior que zero"
        }

        require(workerId.isNotBlank()) {
            "O identificador do Worker não pode ser vazio"
        }

        require(fencingToken > 0) {
            "O fencing token deve ser maior que zero"
        }

        require(leaseDurationSeconds > 0) {
            "A duração do lease deve ser maior que zero"
        }

        val sql = """
                UPDATE sync_jobs
                SET locked_until = DATE_ADD(
                    CURRENT_TIMESTAMP,
                    INTERVAL ? SECOND
                )
                WHERE id = ?
                  AND status = 'RUNNING'
                  AND worker_id = ?
                  AND fencing_token = ?
                  AND locked_until > CURRENT_TIMESTAMP
            """.trimIndent()

        return DatabaseFactory.getConnection().use { connection ->

            connection.prepareStatement(sql).use { statement ->

                statement.setLong(1, leaseDurationSeconds)
                statement.setLong(2, jobId)
                statement.setString(3, workerId)
                statement.setLong(4, fencingToken)

                statement.executeUpdate() == 1
            }
        }
    }


    override fun completeJob(
        jobId: Long,
        workerId: String,
        fencingToken: Long
    ): Boolean {
        require(jobId > 0)
        require(workerId.isNotBlank())
        require(fencingToken > 0)

        val sql = """
                    UPDATE sync_jobs
                    SET status = 'SUCCEEDED',
                        finished_at = CURRENT_TIMESTAMP,
                        worker_id = NULL,
                        locked_until = NULL
                    WHERE id = ?
                      AND status = 'RUNNING'
                      AND worker_id = ?
                      AND fencing_token = ?
                      AND locked_until > CURRENT_TIMESTAMP
                      AND processed_items = total_items
                """.trimIndent()

        return DatabaseFactory.getConnection().use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setLong(1, jobId)
                statement.setString(2, workerId)
                statement.setLong(3, fencingToken)

                statement.executeUpdate() == 1
            }
        }
    }


    override fun failJob(
        jobId: Long,
        workerId: String,
        fencingToken: Long
    ): Boolean {
        require(jobId > 0)
        require(workerId.isNotBlank())
        require(fencingToken > 0)

        val sql = """
        UPDATE sync_jobs
        SET status = 'FAILED',
            finished_at = CURRENT_TIMESTAMP,
            worker_id = NULL,
            locked_until = NULL
        WHERE id = ?
          AND status = 'RUNNING'
          AND worker_id = ?
          AND fencing_token = ?
          AND locked_until > CURRENT_TIMESTAMP
    """.trimIndent()

        return DatabaseFactory.getConnection().use { connection ->
            connection.prepareStatement(sql).use { statement ->
                statement.setLong(1, jobId)
                statement.setString(2, workerId)
                statement.setLong(3, fencingToken)

                statement.executeUpdate() == 1
            }
        }
    }


    override fun acquireNext(
        workerId: String,
        leaseDurationSeconds: Long
    ): SyncJob? {

        require(workerId.isNotBlank()) {
            "O identificador do Worker não pode ser vazio"
        }

        require(leaseDurationSeconds > 0) {
            "A duração do lease deve ser maior que zero"
        }

        return DatabaseFactory.getConnection().use { connection ->

            connection.autoCommit = false

            try {

                val jobId = findAndLockAvailableJob(connection)

                if (jobId == null) {
                    connection.commit()
                    null
                } else {

                    updateJobLease(
                        connection,
                        jobId,
                        workerId,
                        leaseDurationSeconds
                    )

                    val job = findJobById(connection, jobId)

                    connection.commit()

                    job
                }

            } catch (e: Exception) {

                try {
                    connection.rollback()
                } catch (rollbackError: Exception) {
                    e.addSuppressed(rollbackError)
                }

                throw e
            }
        }
    }

    private fun findAndLockAvailableJob(
        connection: Connection
    ): Long? {

        val sql = """
        SELECT id
        FROM sync_jobs
        WHERE
            status = 'PENDING'
            OR (
                status = 'RUNNING'
                AND locked_until <= CURRENT_TIMESTAMP
            )
        ORDER BY created_at ASC, id ASC
        LIMIT 1
        FOR UPDATE SKIP LOCKED
    """.trimIndent()

        return connection.prepareStatement(sql).use { statement ->

            statement.executeQuery().use { result ->

                if (result.next()) {
                    result.getLong("id")
                } else {
                    null
                }
            }
        }
    }

    private fun updateJobLease(
        connection: Connection,
        jobId: Long,
        workerId: String,
        leaseDurationSeconds: Long
    ) {

        val sql = """
        UPDATE sync_jobs
        SET
            status = 'RUNNING',
            worker_id = ?,
            locked_until = DATE_ADD(
                CURRENT_TIMESTAMP,
                INTERVAL ? SECOND
            ),
            fencing_token = fencing_token + 1,
            started_at = COALESCE(
                started_at,
                CURRENT_TIMESTAMP
            )
        WHERE id = ?
    """.trimIndent()

        connection.prepareStatement(sql).use { statement ->

            statement.setString(1, workerId)
            statement.setLong(2, leaseDurationSeconds)
            statement.setLong(3, jobId)

            val updatedRows = statement.executeUpdate()

            if (updatedRows != 1) {
                throw java.sql.SQLException(
                    "Não foi possível atualizar o job $jobId"
                )
            }
        }
    }

    private fun findJobById(
        connection: Connection,
        jobId: Long
    ): SyncJob {

        val sql = """
            SELECT
                id,
                status,
                total_items,
                processed_items,
                created_at,
                started_at,
                finished_at,
                worker_id,
                locked_until,
                fencing_token
            FROM sync_jobs
            WHERE id = ?
        """.trimIndent()

        return connection.prepareStatement(sql).use { statement ->

            statement.setLong(1, jobId)

            statement.executeQuery().use { result ->

                if (result.next()) {
                    mapRow(result)
                } else {
                    throw java.sql.SQLException(
                        "Job $jobId não encontrado após aquisição"
                    )
                }
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
            finishedAt = result.getTimestamp("finished_at")?.toLocalDateTime(),
            workerId = result.getString("worker_id"),
            lockedUntil = result.getTimestamp("locked_until")?.toLocalDateTime(),
            fencingToken = result.getLong("fencing_token")
        )
    }
}
