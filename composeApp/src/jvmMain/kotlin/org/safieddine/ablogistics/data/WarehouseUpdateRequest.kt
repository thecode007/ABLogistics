package org.safieddine.ablogistics.data

import kotlinx.serialization.Serializable

@Serializable
data class WarehouseUpdateRequest(
    val name: String,
    val location: String,
    val isoCode: String
)

@Serializable
data class WarehouseReceiptsSummary(
    val receipts: List<ReceiptResponse>,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val totalInbound: java.math.BigDecimal,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val totalOutbound: java.math.BigDecimal,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)

@Serializable
data class WarehouseReceiptsSummaryDetailed(
    val receipts: List<ReceiptResponse>,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val totalInbound: java.math.BigDecimal,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val totalOutbound: java.math.BigDecimal,
    val inboundByEntityType: Map<String, @Serializable(with = BigDecimalAsStringSerializer::class) java.math.BigDecimal> = emptyMap(),
    val outboundByEntityType: Map<String, @Serializable(with = BigDecimalAsStringSerializer::class) java.math.BigDecimal> = emptyMap(),
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)

@Serializable
data class WarehouseFundsDTO(
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val totalFunds: java.math.BigDecimal = java.math.BigDecimal.ZERO,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val realFunds: java.math.BigDecimal = java.math.BigDecimal.ZERO
)
