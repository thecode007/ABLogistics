package org.safieddine.ablogistics.data

import kotlinx.serialization.Serializable

@Serializable
data class CreateWarehouseRequest(
    val name: String,
    val location: String,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val totalFunds: java.math.BigDecimal,
    val isoCode: String
)

@Serializable
data class CreateUserRequest(
    val username: String,
    val fullName: String,
    val phoneNumber: String,
    val password: String,
    val admin: Boolean = false,
    val warehouseID: Long? = null
)
