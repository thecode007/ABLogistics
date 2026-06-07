package org.safieddine.ablogistics.data

import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class DashboardSummaryResponse(
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val fuelDelivered: BigDecimal,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val dieselDelivered: BigDecimal,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val fuelPending: BigDecimal,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val dieselPending: BigDecimal,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val totalRevenue: BigDecimal,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val totalProfit: BigDecimal,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val totalShortagePenalty: BigDecimal,
    val customerStats: List<CustomerStatDTO>
)

@Serializable
data class CustomerStatDTO(
    val name: String,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val fuelLiters: BigDecimal,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val dieselLiters: BigDecimal
)
