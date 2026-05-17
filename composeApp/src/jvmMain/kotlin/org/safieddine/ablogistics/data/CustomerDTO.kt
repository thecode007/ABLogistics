package org.safieddine.ablogistics.data

import kotlinx.serialization.Serializable

@Serializable
data class UpdateCustomerRequest(
    val name: String,
    val location: String,
    val phoneNumber: String,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val totalFunds: java.math.BigDecimal = java.math.BigDecimal.ZERO,
    val warehouseId: Long? = null
)

@Serializable
data class CreateCustomerRequest(
    val name: String,
    val phoneNumber: String,
    val location: String,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val totalFunds: java.math.BigDecimal = java.math.BigDecimal.ZERO,
    val warehouseId: Long
)

@Serializable
data class CustomerResponse(
    val id: Long,
    val phoneNumber: String,
    val name: String,
    val location: String,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val totalFunds: java.math.BigDecimal,
    val warehouseId: Long,
    val warehouseName: String,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val totalFuelLiters: java.math.BigDecimal = java.math.BigDecimal.ZERO,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val totalDieselLiters: java.math.BigDecimal = java.math.BigDecimal.ZERO
)

@Serializable
data class CustomersListResponse(
    val customers: List<CustomerResponse> = emptyList(),
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val totalFundsSum: java.math.BigDecimal = java.math.BigDecimal.ZERO,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val totalFuelLitersSum: java.math.BigDecimal = java.math.BigDecimal.ZERO,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val totalDieselLitersSum: java.math.BigDecimal = java.math.BigDecimal.ZERO
)
