package org.safieddine.ablogistics.data

import kotlinx.serialization.Serializable

@Serializable
data class UpdateCustomerRequest(
    val name: String,
    val location: String,
    val phoneNumber: String,
    val totalFunds: Double = 0.0,
    val warehouseId: Long? = null
)

@Serializable
data class CreateCustomerRequest(
    val name: String,
    val phoneNumber: String,
    val location: String,
    val totalFunds: Double = 0.0,
    val warehouseId: Long
)

@Serializable
data class CustomerResponse(
    val id: Long,
    val phoneNumber: String,
    val name: String,
    val location: String,
    val totalFunds: Double,
    val warehouseId: Long,
    val warehouseName: String
)

@Serializable
data class CustomersListResponse(
    val customers: List<CustomerResponse> = emptyList(),
    val totalFundsSum: Double = 0.0
)
