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
import org.safieddine.ablogistics.data.ProfitAnalysisResponse
import org.safieddine.ablogistics.data.config.AppConfig
import org.safieddine.ablogistics.data.session.SessionStore

object ReceiptService {

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

    suspend fun create(req: CreateReceiptRequest): Result<BaseResponse<ReceiptResponse>> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<ReceiptResponse> = client.post("receipts") {
                    setBody(req)
                }.body()
                if (res.success) Result.success(res)
                else Result.failure(Exception(res.message))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to create receipt: ${e.message}"))
            }
        }

    suspend fun listWarehouse(
        warehouseId: Long,
        type: ReceiptType? = null,
        start: Long? = null,
        end: Long? = null,
        page: Int = 0,
        size: Int = 50,
        receiptId: String? = null
    ): Result<BaseResponse<WarehouseReceiptsSummary>> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<WarehouseReceiptsSummary> = client.get("receipts/warehouse") {
                    parameter("warehouseId", warehouseId)
                    if (type != null) parameter("type", type.name)
                    if (start != null && start != 0L) parameter("start", start)
                    if (end != null && end != 0L) parameter("end", end)
                    if (!receiptId.isNullOrBlank()) parameter("receiptId", receiptId)
                    parameter("page", page)
                    parameter("size", size)
                }.body()

                if (res.success) Result.success(res)
                else Result.failure(Exception(res.message))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to list warehouse receipts: ${e.message}"))
            }
        }

    // Detailed warehouse summary with split by entity type
    suspend fun listWarehouseDetailed(
        warehouseId: Long,
        type: ReceiptType? = null,
        start: Long? = null,
        end: Long? = null,
        page: Int = 0,
        size: Int = 50,
        receiptId: String? = null
    ): Result<BaseResponse<WarehouseReceiptsSummaryDetailed>> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<WarehouseReceiptsSummaryDetailed> = client.get("receipts/warehouse/summary") {
                    parameter("warehouseId", warehouseId)
                    if (type != null) parameter("type", type.name)
                    if (start != null && start != 0L) parameter("start", start)
                    if (end != null && end != 0L) parameter("end", end)
                    if (!receiptId.isNullOrBlank()) parameter("receiptId", receiptId)
                    parameter("page", page)
                    parameter("size", size)
                }.body()

                if (res.success) Result.success(res)
                else Result.failure(Exception(res.message))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to list warehouse receipts (detailed): ${e.message}"))
            }
        }

    // 🔹 LIST CUSTOMER (fixed: start/end now Long?)
    suspend fun listCustomer(
        warehouseId: Long,
        customerId: Long,
        type: ReceiptType? = null,
        start: Long? = null,
        end: Long? = null,
        page: Int = 0,
        size: Int = 50,
        receiptId: String? = null
    ): Result<BaseResponse<WarehouseReceiptsSummary>> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<WarehouseReceiptsSummary> = client.get("receipts/customer") {
                    parameter("warehouseId", warehouseId)
                    parameter("customerId", customerId)
                    if (type != null) parameter("type", type.name)
                    if (start != null && start != 0L) parameter("start", start)
                    if (end != null && end != 0L) parameter("end", end)
                    if (!receiptId.isNullOrBlank()) parameter("receiptId", receiptId)
                    parameter("page", page)
                    parameter("size", size)
                }.body()

                if (res.success) Result.success(res)
                else Result.failure(Exception(res.message))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to list customer receipts: ${e.message}"))
            }
        }

    // 🔹 UPDATE
    suspend fun update(id: Long, req: UpdateReceiptRequest): Result<BaseResponse<ReceiptResponse>> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<ReceiptResponse> = client.put("receipts/$id") {
                    setBody(req)
                }.body()
                if (res.success) Result.success(res)
                else Result.failure(Exception(res.message))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to update receipt: ${e.message}"))
            }
        }

    // 🔹 DELETE
    suspend fun delete(id: Long): Result<BaseResponse<String>> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<String> = client.delete("receipts/$id").body()
                if (res.success) Result.success(res)
                else Result.failure(Exception(res.message))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to delete receipt: ${e.message}"))
            }
        }
    // dY"1 PARTIAL RETURN
    suspend fun partialReturn(id: Long, req: PartialReturnRequest): Result<BaseResponse<ReceiptResponse>> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<ReceiptResponse> = client.post("receipts/$id/partial-return") {
                    setBody(req)
                }.body()
                if (res.success) Result.success(res)
                else Result.failure(Exception(res.message))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to partial return receipt: ${e.message}"))
            }
        }

    suspend fun getProfitAnalysis(): Result<BaseResponse<ProfitAnalysisDTO>> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<List<ProfitAnalysisResponse>> = client.get("/api/v1/logistics/profit-analysis").body()
                if (res.success) {
                    val list = res.data ?: emptyList()
                    val aggregated = ProfitAnalysisDTO(
                        totalProfit = list.sumOf { it.totalProfit },
                        expectedRevenue = list.sumOf { it.totalExpectedRevenue },
                        totalQuantityLoaded = list.sumOf { it.totalQuantityLoaded }
                    )
                    Result.success(BaseResponse(success = true, message = res.message, code = "200", data = aggregated, timestamp = System.currentTimeMillis()))
                }
                else Result.failure(Exception(res.message))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to get profit analysis: ${e.message}"))
            }
        }

    suspend fun getDebtSummary(): Result<BaseResponse<DebtSummaryDTO>> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<DebtSummaryDTO> = client.get("/api/v1/logistics/debt-summary").body()
                if (res.success) Result.success(res)
                else Result.failure(Exception(res.message))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to get debt summary: ${e.message}"))
            }
        }
}

@kotlinx.serialization.Serializable
data class ProfitAnalysisDTO(
    val totalProfit: Double = 0.0,
    val expectedRevenue: Double = 0.0,
    val totalQuantityLoaded: Double = 0.0
)

@kotlinx.serialization.Serializable
data class DebtSummaryDTO(
    val supplierDebt: Double = 0.0,
    val customerDebt: Double = 0.0
)
