package org.safieddine.ablogistics.data.service

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import org.safieddine.ablogistics.data.BaseResponse
import org.safieddine.ablogistics.data.NotificationListItem
import org.safieddine.ablogistics.data.network.HttpClientFactory

object NotificationService {

    private val client = HttpClientFactory.httpClient

    suspend fun list(
        status: String? = null,
        unreadOnly: Boolean = false,
        limit: Int = 50
    ): Result<BaseResponse<List<NotificationListItem>>> {
        return try {
            val res: BaseResponse<List<NotificationListItem>> = client.get("notifications") {
                if (!status.isNullOrBlank()) parameter("status", status)
                parameter("unreadOnly", unreadOnly)
                parameter("limit", limit)
            }.body()
            if (res.success) Result.success(res) else Result.failure(Exception(res.message))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to list notifications: ${e.message}"))
        }
    }

    suspend fun listUnread(limit: Int = 50): Result<BaseResponse<List<NotificationListItem>>> {
        return try {
            val res: BaseResponse<List<NotificationListItem>> = client.get("notifications/unread") {
                parameter("limit", limit)
            }.body()
            if (res.success) Result.success(res) else Result.failure(Exception(res.message))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to list unread notifications: ${e.message}"))
        }
    }

    suspend fun markAllAsRead(): Result<BaseResponse<Int>> {
        return try {
            val res: BaseResponse<Int> = client.post("notifications/read-all").body()
            if (res.success) Result.success(res) else Result.failure(Exception(res.message))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to mark all as read: ${e.message}"))
        }
    }

    suspend fun listPendingDecisions(limit: Int = 50): Result<BaseResponse<List<NotificationListItem>>> {
        return try {
            val res: BaseResponse<List<NotificationListItem>> = client.get("notifications/decisions/pending") {
                parameter("limit", limit)
            }.body()
            if (res.success) Result.success(res) else Result.failure(Exception(res.message))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to list pending decisions: ${e.message}"))
        }
    }
}
