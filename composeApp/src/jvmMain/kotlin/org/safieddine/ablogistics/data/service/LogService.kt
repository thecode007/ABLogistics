package org.safieddine.ablogistics.data.service

import LogResponse
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
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.safieddine.ablogistics.data.BaseResponse
import org.safieddine.ablogistics.data.config.AppConfig
import org.safieddine.ablogistics.data.session.SessionStore

object LogService {

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
                header("Authorization", "Bearer $token")
            }
            accept(ContentType.Application.Json)
        }
    }

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
