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
    val totalInbound: Double,
    val totalOutbound: Double,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)

@Serializable
data class WarehouseReceiptsSummaryDetailed(
    val receipts: List<ReceiptResponse>,
    val totalInbound: Double,
    val totalOutbound: Double,
    val inboundByEntityType: Map<String, Double> = emptyMap(),
    val outboundByEntityType: Map<String, Double> = emptyMap(),
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)

@Serializable
data class WarehouseFundsDTO(
    val totalFunds: Double = 0.0,
    val realFunds: Double = 0.0
)
