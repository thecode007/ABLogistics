package org.safieddine.ablogistics.data.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.safieddine.ablogistics.data.BaseResponse
import org.safieddine.ablogistics.data.CreateWarehouseRequest
import org.safieddine.ablogistics.data.WarehouseDTO
import org.safieddine.ablogistics.data.WarehouseFundsDTO
import org.safieddine.ablogistics.data.WarehouseUpdateRequest
import org.safieddine.ablogistics.data.session.SessionStore
import org.safieddine.ablogistics.data.config.AppConfig
import org.safieddine.ablogistics.data.network.HttpClientFactory

object WarehouseService {

    private var tokenProvider: TokenProvider = SessionStore

    private val client = HttpClientFactory.httpClient
    suspend fun createWarehouse(request: CreateWarehouseRequest): Result<BaseResponse<WarehouseDTO>> {
        return try {
            val response: BaseResponse<WarehouseDTO> = client.post("warehouses") {
                setBody(request)
            }.body()
            if (response.success) Result.success(response)
            else Result.failure(Exception(response.message))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to create warehouse: ${e.message}"))
        }
    }

    suspend fun updateWarehouse(id: Long, request: WarehouseUpdateRequest): Result<BaseResponse<String>> {
        return try {
            val response: BaseResponse<String> = client.put ("warehouses/${id}") {
                setBody(request)
            }.body()
            if (response.success)
                Result.success(response)
            else Result.failure(Exception(response.message))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to create warehouse: ${e.message}"))
        }
    }

    suspend fun deleteWarehouse(id: Long): Result<BaseResponse<String>> {
        return try {
            val response: BaseResponse<String> = client.delete ("warehouses/${id}").body()
            if (response.success)
                Result.success(response)
            else
                Result.failure(Exception(response.message))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to create warehouse: ${e.message}"))
        }
    }
    suspend fun getWarehouse(id: Long): Result<BaseResponse<WarehouseDTO>> {
        return try {
            val response: BaseResponse<WarehouseDTO> = client.get("warehouses/$id").body()
            if (response.success) Result.success(response)
            else Result.failure(Exception(response.message))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get warehouse: ${e.message}"))
        }
    }

    suspend fun getAllWarehouses(): Result<BaseResponse<List<WarehouseDTO>>> {
        return try {
            val response: BaseResponse<List<WarehouseDTO>> = client.get("warehouses").body()
            if (response.success) Result.success(response)
            else Result.failure(Exception(response.message))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get warehouses: ${e.message}"))
        }
    }

    suspend fun getWarehouseFunds(id: Long): Result<BaseResponse<WarehouseFundsDTO>> {
        return try {
            val response: BaseResponse<WarehouseFundsDTO> = client.get("warehouses/$id/funds").body()
            if (response.success) Result.success(response)
            else Result.failure(Exception(response.message))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get warehouse funds: ${e.message}"))
        }
    }

    fun close() {
        client.close()
    }
}
