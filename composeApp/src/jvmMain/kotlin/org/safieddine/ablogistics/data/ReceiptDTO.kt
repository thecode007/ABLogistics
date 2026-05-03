package org.safieddine.ablogistics.data

import kotlinx.serialization.Serializable

@Serializable
enum class ReceiptType { INWARD, OUTWARD, RETURNED }

@Serializable
enum class EntityType { WAREHOUSE, CUSTOMER }

@Serializable
data class CreateReceiptRequest(
    val receiptId: String,
    val receiptType: ReceiptType,
    val entityType: EntityType,
    val warehouseId: Long,
    val customerId: Long? = null,
    val amount: Double,
    val description: String? = null,
    val createdAtMillis: Long? = null
)

@Serializable
data class ReceiptResponse(
    val id: Long,
    val receiptId: String,
    val receiptType: ReceiptType,
    val entityType: EntityType,
    val warehouseId: Long,
    val customerId: Long?,
    val amount: Double,
    val beforeImpactFunds: Double,
    val afterImpactFunds: Double,
    val isReturned: Boolean,
    val isReturnAdjustment: Boolean,
    val warehouseRealFundsBefore: Double,
    val warehouseRealFundsAfter: Double,
    val description: String?,
    val createdBy: String,
    val createdAt: String?
)

@Serializable
data class PageResponse<T>(
    val content: List<T> = emptyList(),
    val totalPages: Int = 0,
    val totalElements: Long = 0,
    val number: Int = 0,
    val size: Int = 0
)

@Serializable
data class UpdateReceiptRequest(
    val receiptId: String? = null,
    val receiptType: ReceiptType,
    val entityType: EntityType,
    val amount: Double,
    val description: String? = null,
    val customerId: Long? = null,
    val returned: Boolean? = null,
    val createdAtMillis: Long? = null
)

@Serializable
data class PartialReturnRequest(
    val paidAmount: Double
)
