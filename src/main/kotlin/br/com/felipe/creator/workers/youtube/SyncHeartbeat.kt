
package br.com.felipe.creator.workers.youtube

import br.com.felipe.creator.domain.sync.SyncJobRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

class SyncHeartbeat(val repository: SyncJobRepository) {

    suspend fun run(jobId: Long, workerId: String, fencingToken: Long,
        leaseDurationSeconds: Long, heartbeatIntervalSeconds: Long) {

        require(jobId > 0)
        require(workerId.isNotBlank())
        require(fencingToken > 0)
        require(leaseDurationSeconds > 0)
        require(heartbeatIntervalSeconds > 0)
        require(heartbeatIntervalSeconds < leaseDurationSeconds)

        while (currentCoroutineContext().isActive) {

            delay(heartbeatIntervalSeconds * 1000)

            val renewed = withContext(Dispatchers.IO) {
                repository.renewLease(
                    jobId = jobId,
                    workerId = workerId,
                    fencingToken = fencingToken,
                    leaseDurationSeconds = leaseDurationSeconds
                )
            }

            if (!renewed) {
                throw LeaseLostException(
                    "O Worker perdeu a autorização para processar o job $jobId"
                )
            }
        }
    }
}

class LeaseLostException(message: String) :
    RuntimeException(message)
