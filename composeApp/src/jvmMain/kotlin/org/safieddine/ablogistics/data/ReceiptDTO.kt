package org.safieddine.ablogistics.data

import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
enum class ReceiptType { INWARD, OUTWARD, RETURNED }

@Serializable
enum class EntityType { WAREHOUSE, CUSTOMER, SUPPLIER }

@Serializable
data class CreateReceiptRequest(
    val receiptId: String,
    val receiptType: ReceiptType,
    val entityType: EntityType,
    val warehouseId: Long,
    val customerId: Long? = null,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val amount: BigDecimal,
    val description: String? = null,
    val createdAtMillis: Long? = null
)

@Serializable
data class ReceiptResponse(
    val id: Long,
    val receiptId: String? = null,
    val receiptType: ReceiptType,
    val entityType: EntityType,
    val warehouseId: Long,
    val customerId: Long? = null,
    val supplierId: Long? = null,
    val brvId: Long? = null,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val amount: BigDecimal,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val beforeImpactFunds: BigDecimal,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val afterImpactFunds: BigDecimal,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val warehouseRealFundsBefore: BigDecimal = BigDecimal.ZERO,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val warehouseRealFundsAfter: BigDecimal = BigDecimal.ZERO,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val costPrice: BigDecimal? = null,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val sellingPrice: BigDecimal? = null,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val loadedQuantity: BigDecimal? = null,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val dispatchedQuantity: BigDecimal? = null,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val brvCost: BigDecimal? = null,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val shortagePenalty: BigDecimal? = null,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val profit: BigDecimal? = null,
    val isReturned: Boolean,
    val isReturnAdjustment: Boolean,
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
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val amount: BigDecimal,
    val description: String? = null,
    val customerId: Long? = null,
    val returned: Boolean? = null,
    val createdAtMillis: Long? = null
)

@Serializable
data class PartialReturnRequest(
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val paidAmount: BigDecimal
)
