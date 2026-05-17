package org.safieddine.ablogistics.data.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.safieddine.ablogistics.data.BaseResponse
import org.safieddine.ablogistics.data.CreateUserRequest
import org.safieddine.ablogistics.data.UpdateUserRequest
import org.safieddine.ablogistics.data.UserDTO
import org.safieddine.ablogistics.data.session.SessionStore
import org.safieddine.ablogistics.data.config.AppConfig
import org.safieddine.ablogistics.data.network.HttpClientFactory
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object UserService {

    // Token provider injected from SessionStore
    private var tokenProvider: TokenProvider = SessionStore

    // Global reusable HttpClient with JSON + logging
    private val client = HttpClientFactory.httpClient

    // --- User Operations ---
    suspend fun blockUser(username: String): BaseResponse<UserDTO> {
        return client.put("users/block") {
            // DO NOT pre-encode query params
            url.parameters.append("username", username)
        }.body()
    }

    suspend fun unblockUser(username: String): BaseResponse<UserDTO> {
        return client.put("users/unblock") {
            url.parameters.append("username", username)
        }.body()
    }

    suspend fun deleteUser(username: String): Result<BaseResponse<String>> {
        return try {
            val response: BaseResponse<String> = client.delete("users") {
                url.parameters.append("username", username)
            }.body()
            if (response.success) Result.success(response)
            else Result.failure(Exception(response.message))
        } catch (e: Exception) {
            Result.failure(Exception("Delete user failed: ${e.message}"))
        }
    }

    suspend fun createAdminUser(request: CreateUserRequest): Result<BaseResponse<UserDTO>> {
        return try {
            val response: BaseResponse<UserDTO> = client.post("users/admin") {
                setBody(request)
            }.body()
            if (response.success) Result.success(response)
            else Result.failure(Exception(response.message))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to create admin user: ${e.message}"))
        }
    }

    suspend fun getAllUsers(): Result<BaseResponse<List<UserDTO>>> {
        return try {
            val response: BaseResponse<List<UserDTO>> = client.get("users").body()
            if (response.success) Result.success(response)
            else Result.failure(Exception(response.message))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get users: ${e.message}"))
        }
    }

    suspend fun getUser(username: String): Result<BaseResponse<UserDTO>> {
        return try {
            val response: BaseResponse<UserDTO> = client.get("users/${username.safeEnc()}").body()
            if (response.success) Result.success(response)
            else Result.failure(Exception(response.message))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get user: ${e.message}"))
        }
    }

    suspend fun unassignUserFromWarehouses(username: String): Result<BaseResponse<String>> {
        return try {
            val response: BaseResponse<String> = client.delete("users/${username.safeEnc()}/warehouse").body()
            if (response.success) Result.success(response)
            else Result.failure(Exception(response.message))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to unassign warehouses: ${e.message}"))
        }
    }

    suspend fun checkUserBlocked(username: String): Result<BaseResponse<Boolean>> {
        return try {
            val response: BaseResponse<Boolean> = client.get("users/${username.safeEnc()}/blocked").body()
            if (response.success) Result.success(response)
            else Result.failure(Exception(response.message))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get if blocked: ${e.message}"))
        }
    }

    suspend fun updateAdminUser(request: UpdateUserRequest): Result<BaseResponse<UserDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response: BaseResponse<UserDTO> = client.put("users/${request.username.safeEnc()}") {
                    setBody(request)
                    contentType(ContentType.Application.Json)
                }.body()
                if (response.success) Result.success(response)
                else Result.failure(Exception(response.message))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to update user: ${e.message}"))
            }
        }
    }

    fun close() {
        client.close()
    }
}

private fun String.safeEnc(): String {
    return URLEncoder.encode(this, StandardCharsets.UTF_8.toString())
        .replace("+", "%20")
}

