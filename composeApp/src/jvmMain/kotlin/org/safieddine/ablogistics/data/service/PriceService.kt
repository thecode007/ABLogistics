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
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.safieddine.ablogistics.data.*
import org.safieddine.ablogistics.data.config.AppConfig
import org.safieddine.ablogistics.data.session.SessionStore

object PriceService {

    private var tokenProvider: TokenProvider = SessionStore

    private val client get() = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            level = LogLevel.INFO
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

    suspend fun getGlobalPrices(): Result<BaseResponse<List<MaterialPriceDTO>>> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<List<MaterialPriceDTO>> = client.get("/api/v1/settings/prices").body()
                if (res.success) Result.success(res)
                else Result.failure(Exception(res.message))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to fetch material prices: ${e.message}"))
            }
        }

    suspend fun updateGlobalPrices(prices: List<MaterialPriceDTO>): Result<BaseResponse<List<MaterialPriceDTO>>> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<List<MaterialPriceDTO>> = client.patch("/api/v1/settings/prices") {
                    setBody(prices)
                }.body()
                if (res.success) Result.success(res)
                else Result.failure(Exception(res.message))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to update material prices: ${e.message}"))
            }
        }
}
