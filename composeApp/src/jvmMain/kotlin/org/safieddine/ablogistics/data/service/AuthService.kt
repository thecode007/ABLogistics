package org.safieddine.ablogistics.data.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.plugins.logging.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.safieddine.ablogistics.data.BaseResponse
import org.safieddine.ablogistics.data.LoginData
import org.safieddine.ablogistics.data.LoginRequest
import org.safieddine.ablogistics.data.config.AppConfig
import org.safieddine.ablogistics.data.LogoutRequest

class AuthService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15000L
            connectTimeoutMillis = 15000L
            socketTimeoutMillis = 15000L
        }
        install(Logging) {
            level = LogLevel.ALL
            logger = Logger.DEFAULT
        }
        defaultRequest {
            url(AppConfig.baseUrl)
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }
    }
    
    suspend fun login(username: String, password: String, deviceToken: String? = null, platform: String? = "DESKTOP"): Result<BaseResponse<LoginData>> {
        return try {
            val httpResponse = client.post("auth/login") {
                setBody(LoginRequest(username, password))
            }
            
            if (httpResponse.status.value in 200..299) {
                val response: BaseResponse<LoginData> = httpResponse.body()
                Result.success(response)
            } else {
                val errorBody = try { httpResponse.bodyAsText() } catch(_: Exception) { "No body" }
                Result.failure(Exception("HTTP ${httpResponse.status}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Login failed: ${e.message}"))
        }
    }

    @Serializable
    data class TokenValidationData(
        val valid: Boolean,
        val username: String? = null
    )

    suspend fun validateToken(token: String): Boolean {
        return try {
            val response: BaseResponse<TokenValidationData> = client.post("auth/validate") {
                header("Authorization", "Bearer $token")
            }.body()
            response.success
        } catch (e: Exception) {
            println("Validation failed $e")
            false
        }
    }
    fun close() {
        client.close()
    }

    suspend fun logout(token: String, deviceToken: String? = null): Boolean {
        return try {
            val response: BaseResponse<String> = client.post("auth/logout") {
                header("Authorization", "Bearer $token")
                // Pass device token so backend can unregister it
                setBody(LogoutRequest(token = deviceToken))
            }.body()
            response.success
        } catch (e: Exception) {
            println("Logout failed $e")
            false
        }
    }
}
