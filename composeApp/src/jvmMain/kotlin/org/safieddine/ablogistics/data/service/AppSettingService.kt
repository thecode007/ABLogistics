package org.safieddine.ablogistics.data.service

import io.ktor.client.call.body
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.safieddine.ablogistics.data.BaseResponse
import org.safieddine.ablogistics.data.network.HttpClientFactory

@Serializable
data class ReceiptCounterDTO(val nextReceiptNumber: Long)

@Serializable
data class SetReceiptCounterRequest(val startFrom: Long)

object AppSettingService {

    private val client = HttpClientFactory.httpClient

    /** Peek at the current next receipt number (no increment). */
    suspend fun getReceiptCounter(): Result<BaseResponse<ReceiptCounterDTO>> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<ReceiptCounterDTO> =
                    client.get("/api/v1/settings/receipt-counter").body()
                if (res.success) Result.success(res)
                else Result.failure(Exception(res.message))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to fetch receipt counter: ${e.message}"))
            }
        }

    /** Set the starting number (config dialog). */
    suspend fun setReceiptCounter(startFrom: Long): Result<BaseResponse<ReceiptCounterDTO>> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<ReceiptCounterDTO> =
                    client.patch("/api/v1/settings/receipt-counter") {
                        setBody(SetReceiptCounterRequest(startFrom))
                    }.body()
                if (res.success) Result.success(res)
                else Result.failure(Exception(res.message))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to set receipt counter: ${e.message}"))
            }
        }

    /**
     * Atomically consume the next receipt number (server increments its counter).
     * Call this ONCE per receipt creation.
     */
    suspend fun nextReceiptNumber(): Result<Long> =
        withContext(Dispatchers.IO) {
            try {
                val res: BaseResponse<ReceiptCounterDTO> =
                    client.post("/api/v1/settings/receipt-counter/next").body()
                if (res.success) Result.success(res.data!!.nextReceiptNumber)
                else Result.failure(Exception(res.message))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to generate receipt number: ${e.message}"))
            }
        }
}
