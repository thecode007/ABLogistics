package org.safieddine.ablogistics.data.service

import io.ktor.client.call.body
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.safieddine.ablogistics.data.*
import org.safieddine.ablogistics.data.network.HttpClientFactory

object PriceService {

    private val client = HttpClientFactory.httpClient

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
