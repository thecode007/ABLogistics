package org.safieddine.ablogistics.data.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.safieddine.ablogistics.data.BaseResponse
import org.safieddine.ablogistics.data.NotificationListItem
import org.safieddine.ablogistics.data.config.AppConfig
import org.safieddine.ablogistics.data.session.SessionStore

object NotificationService {

    private var tokenProvider: TokenProvider = SessionStore

    fun injectTokenProvider(provider: TokenProvider) {
        tokenProvider = provider
    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            level = LogLevel.ALL
            logger = Logger.DEFAULT
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15000L
            connectTimeoutMillis = 15000L
            socketTimeoutMillis = 15000L
        }
        defaultRequest {
            url(AppConfig.baseUrl)
            contentType(ContentType.Application.Json)
            val token = tokenProvider.currentToken()
            if (!token.isNullOrEmpty()) {
                headers.append("Authorization", "Bearer $token")
            }
            accept(ContentType.Application.Json)
        }
    }

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
