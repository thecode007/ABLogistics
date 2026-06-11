package org.safieddine.ablogistics.data.service

import LogResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.safieddine.ablogistics.data.BaseResponse
import org.safieddine.ablogistics.data.network.HttpClientFactory

object LogService {

    private val client = HttpClientFactory.httpClient

    suspend fun list(
        page: Int = 0,
        size: Int = 50,
        username: String? = null,
        start: Long? = null,
        end: Long? = null,
        sortAsc: Boolean = false
    ): Result<BaseResponse<List<LogResponse>>> = withContext(Dispatchers.IO) {
        try {
            val res: BaseResponse<List<LogResponse>> = client.get("logs") {
                parameter("page", page)
                parameter("size", size)
                if (!username.isNullOrBlank()) parameter("username", username)
                if (start != null && start != 0L) parameter("start", start)
                if (end != null && end != 0L) parameter("end", end)
                parameter("sortAsc", sortAsc)
            }.body()

            if (res.success) Result.success(res)
            else Result.failure(Exception(res.message))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to list logs: ${e.message}"))
        }
    }

    suspend fun undo(id: Long): Result<BaseResponse<String>> = withContext(Dispatchers.IO) {
        try {
            val res: BaseResponse<String> = client.post("logs/$id/undo").body()
            if (res.success) Result.success(res) else Result.failure(Exception(res.message))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to undo log: ${e.message}"))
        }
    }
}
