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
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import org.safieddine.ablogistics.data.BaseResponse
import org.safieddine.ablogistics.data.BaseResponseRaw
import org.safieddine.ablogistics.data.CreateCustomerRequest
import org.safieddine.ablogistics.data.CustomerResponse
import org.safieddine.ablogistics.data.CustomersListResponse
import org.safieddine.ablogistics.data.UpdateCustomerRequest
import org.safieddine.ablogistics.data.ApproveCustomerUpdateRequest
import org.safieddine.ablogistics.data.RejectCustomerUpdateRequest
import org.safieddine.ablogistics.data.config.AppConfig
import org.safieddine.ablogistics.data.session.SessionStore

object CustomerService {

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
            level = LogLevel.ALL // Log ALL: headers, body, info
            logger = Logger.DEFAULT // prints to standard output
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

    suspend fun list(warehouseId: Long): Result<BaseResponse<CustomersListResponse>> = withContext(Dispatchers.IO) {
        try {
            val raw: BaseResponseRaw = client.get("customers/warehouse/$warehouseId").body()
            if (!raw.success) return@withContext Result.failure(Exception(raw.message))

            val json = Json { ignoreUnknownKeys = true }
            val summary: CustomersListResponse = when (val d: JsonElement? = raw.data) {
                is JsonObject -> {
                    // New shape: { customers: [...], totalFundsSum: 0.0 }
                    json.decodeFromJsonElement(CustomersListResponse.serializer(), d)
                }
                is JsonArray -> {
                    // Old shape: data is just an array of customers
                    val list = json.decodeFromJsonElement(ListSerializer(CustomerResponse.serializer()), d)
                    CustomersListResponse(
                        customers = list,
                        totalFundsSum = list.sumOf { it.totalFunds }
                    )
                }
                else -> CustomersListResponse()
            }

            val normalized = BaseResponse(
                success = raw.success,
                message = raw.message,
                code = raw.code,
                data = summary,
                errors = raw.errors,
                timestamp = raw.timestamp
            )

            Result.success(normalized)
        } catch (e: Exception) {
            println("Failed to list customers: ${e.message}")
            Result.failure(Exception("Failed to list customers: ${e.message}"))
        }
    }

    suspend fun create(req: CreateCustomerRequest): Result<BaseResponse<CustomerResponse>> = withContext(Dispatchers.IO) {
        try {
            val res: BaseResponse<CustomerResponse> = client.post("customers") {
                setBody(req)
            }.body()
            if (res.success) Result.success(res) else Result.failure(Exception(res.message))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to create customer: ${e.message}"))
        }
    }

    suspend fun update(id: Long, req: UpdateCustomerRequest): Result<BaseResponse<CustomerResponse>> = withContext(Dispatchers.IO) {
        try {
            val res: BaseResponse<CustomerResponse> = client.put("customers/$id") {
                setBody(req)
            }.body()
            if (res.success) Result.success(res) else Result.failure(Exception(res.message))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update customer: ${e.message}"))
        }
    }

    suspend fun delete(id: Long): Result<BaseResponse<String>> = withContext(Dispatchers.IO) {
        try {
            val res: BaseResponse<String> = client.delete("customers/$id").body()
            if (res.success) Result.success(res)
            else
                Result.failure(Exception(res.message))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Failed to delete customer"))
        }
    }

    suspend fun approveUpdate(id: Long, req: ApproveCustomerUpdateRequest): Result<BaseResponse<CustomerResponse>> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<CustomerResponse> = client.post("customers/$id/approve-update") {
                    setBody(req)
                }.body()
                if (res.success) Result.success(res) else Result.failure(Exception(res.message))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to approve update: ${e.message}"))
            }
        }

    suspend fun rejectUpdate(id: Long, req: RejectCustomerUpdateRequest): Result<BaseResponse<String>> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<String> = client.post("customers/$id/reject-update") {
                    setBody(req)
                }.body()
                if (res.success) Result.success(res) else Result.failure(Exception(res.message))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to reject update: ${e.message}"))
            }
        }
}
