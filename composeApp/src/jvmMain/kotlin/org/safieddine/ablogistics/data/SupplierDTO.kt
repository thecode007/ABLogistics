package org.safieddine.ablogistics.data

import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class SupplierDTO(
    val id: Long,
    val name: String,
    val phoneNumber: String,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val totalDebt: BigDecimal
)

@Serializable
data class CustomerBalance(
    val id: Long,
    val name: String,
    val phoneNumber: String,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val totalFunds: BigDecimal
)
