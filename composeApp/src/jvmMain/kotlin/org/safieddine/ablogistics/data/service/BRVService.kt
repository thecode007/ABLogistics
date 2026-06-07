package org.safieddine.ablogistics.data.service

import io.ktor.client.statement.bodyAsText

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

object BRVService {

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

    suspend fun getFleetStatus(): Result<BaseResponse<FleetStatusResponse>> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<FleetStatusResponse> = client.get("/api/v1/logistics/fleet-status").body()
                if (res.success) Result.success(res)
                else Result.failure(Exception(res.message))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to get fleet status: ${e.message}"))
            }
        }

    suspend fun saveBRV(brv: BRVDTO): Result<BaseResponse<BRVDTO>> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<BRVDTO> = client.post("/api/v1/logistics/brv") {
                    setBody(brv)
                }.body()
                if (res.success) Result.success(res)
                else Result.failure(Exception(res.message))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to save BRV: ${e.message}"))
            }
        }

    suspend fun updateBRV(id: Long, brv: BRVDTO): Result<BaseResponse<BRVDTO>> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<BRVDTO> = client.put("/api/v1/logistics/brv/$id") {
                    setBody(brv)
                }.body()
                if (res.success) Result.success(res)
                else Result.failure(Exception(res.message))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to update BRV: ${e.message}"))
            }
        }

    suspend fun deleteBRV(id: Long): Result<BaseResponse<String>> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<String> = client.delete("/api/v1/logistics/brv/$id").body()
                if (res.success) Result.success(res)
                else Result.failure(Exception(res.message))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to delete BRV: ${e.message}"))
            }
        }

    suspend fun getBRVHistory(id: Long): Result<BaseResponse<List<ReceiptResponse>>> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<List<ReceiptResponse>> = client.get("/api/v1/logistics/brv/$id/history").body()
                if (res.success) Result.success(res)
                else Result.failure(Exception(res.message))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to get BRV history: ${e.message}"))
            }
        }
    
    suspend fun processLoad(req: ProcessLoadRequest): Result<BaseResponse<List<ReceiptResponse>>> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<List<ReceiptResponse>> = client.post("/api/v1/logistics/load") {
                    setBody(req)
                }.body()
                if (res.success) Result.success(res)
                else Result.failure(Exception(res.message))
            } catch (e: io.ktor.client.plugins.ResponseException) {
                val errorBody = try { e.response.bodyAsText() } catch (_: Exception) { "" }
                Result.failure(Exception("Server error (${e.response.status.value}): $errorBody"))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to process load: ${e.message}"))
            }
        }

    suspend fun finalizeDelivery(req: FinalizeDeliveryRequest): Result<BaseResponse<ReceiptResponse>> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<ReceiptResponse> = client.post("/api/v1/logistics/finalize") {
                    setBody(req)
                }.body()
                if (res.success) Result.success(res)
                else Result.failure(Exception(res.message))
            } catch (e: io.ktor.client.plugins.ResponseException) {
                val errorBody = try { e.response.bodyAsText() } catch (_: Exception) { "" }
                Result.failure(Exception("Server error (${e.response.status.value}): $errorBody"))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to finalize delivery: ${e.message}"))
            }
        }

    suspend fun reverseFinalization(id: Long): Result<BaseResponse<ReceiptResponse>> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<ReceiptResponse> = client.post("/api/v1/logistics/reverse-finalize/$id").body()
                if (res.success) Result.success(res)
                else Result.failure(Exception(res.message))
            } catch (e: io.ktor.client.plugins.ResponseException) {
                val errorBody = try { e.response.bodyAsText() } catch (_: Exception) { "" }
                Result.failure(Exception("Server error (${e.response.status.value}): $errorBody"))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to reverse finalization: ${e.message}"))
            }
        }

    suspend fun getProfitAnalysis(): Result<BaseResponse<List<ProfitAnalysisResponse>>> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<List<ProfitAnalysisResponse>> = client.get("/api/v1/logistics/profit-analysis").body()
                if (res.success) Result.success(res)
                else Result.failure(Exception(res.message))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to get profit analysis: ${e.message}"))
            }
        }

    suspend fun getBrvPayments(): Result<BaseResponse<List<BrvPaymentTodoResponse>>> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<List<BrvPaymentTodoResponse>> = client.get("/api/v1/logistics/brv-payments").body()
                if (res.success) Result.success(res)
                else Result.failure(Exception(res.message))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to get BRV payments: ${e.message}"))
            }
        }

    suspend fun tickBrvPayment(id: Long): Result<BaseResponse<BrvPaymentTodoResponse>> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<BrvPaymentTodoResponse> = client.put("/api/v1/logistics/brv-payments/$id/tick").body()
                if (res.success) Result.success(res)
                else Result.failure(Exception(res.message))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to tick BRV payment: ${e.message}"))
            }
        }

    suspend fun confirmBrvPayments(): Result<BaseResponse<ConfirmPaymentsResponse>> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<ConfirmPaymentsResponse> = client.post("/api/v1/logistics/brv-payments/confirm").body()
                if (res.success) Result.success(res)
                else Result.failure(Exception(res.message))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to confirm BRV payments: ${e.message}"))
            }
        }
}
