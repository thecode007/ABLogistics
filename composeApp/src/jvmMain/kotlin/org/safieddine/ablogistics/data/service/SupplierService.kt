package org.safieddine.ablogistics.data.service

import io.ktor.client.call.body
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.safieddine.ablogistics.data.*
import org.safieddine.ablogistics.data.network.HttpClientFactory

object SupplierService {

    private val client = HttpClientFactory.httpClient

    suspend fun getSuppliers(): Result<BaseResponse<List<SupplierDTO>>> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<List<SupplierDTO>> = client.get("/api/v1/logistics/suppliers").body()
                if (res.success) Result.success(res)
                else Result.failure(Exception(res.message))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
