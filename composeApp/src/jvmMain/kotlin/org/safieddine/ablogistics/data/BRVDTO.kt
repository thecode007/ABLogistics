package org.safieddine.ablogistics.data

import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class BRVDTO(
    val id: Long,
    val plateNumber: String,
    val driverName: String?,
    val driverPhone: String? = null,
    val vendor: String?,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val capacity: BigDecimal,
    val status: String
)

@Serializable
data class FleetStatusResponse(
    val brvs: List<BRVDTO>
)

@Serializable
data class ProfitAnalysisResponse(
    val brvPlateNumber: String,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val totalProfit: BigDecimal,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val totalShortagePenalty: BigDecimal,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val totalQuantityLoaded: BigDecimal,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val totalQuantityDispatched: BigDecimal
)

@Serializable
data class ProcessLoadRequest(
    val brvId: Long,
    val customerId: Long,
    val warehouseId: Long,
    val materialType: MaterialType,
    val material: String? = null,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val loadedQuantity: BigDecimal,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val costPrice: BigDecimal,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val sellingPrice: BigDecimal,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val brvCost: BigDecimal,
    val description: String? = null,
    val receiptId: String? = null,
    val createdAtMillis: Long? = null
)

@Serializable
data class FinalizeDeliveryRequest(
    val customerReceiptId: Long,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val dispatchedQuantity: BigDecimal
)
