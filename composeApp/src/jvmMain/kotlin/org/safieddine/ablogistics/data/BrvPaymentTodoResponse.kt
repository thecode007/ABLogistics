package org.safieddine.ablogistics.data

import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class BrvPaymentTodoResponse(
    @Serializable(with = org.safieddine.ablogistics.data.BigDecimalAsStringSerializer::class)
    val amount: BigDecimal,
    val brvId: Long,
    val createdAt: String? = null,
    val customerName: String,
    val customerReceiptId: Long,
    val id: Long,
    val isTicked: Boolean,
    val plateNumber: String
)

@Serializable
data class ConfirmPaymentsResponse(
    @Serializable(with = org.safieddine.ablogistics.data.BigDecimalAsStringSerializer::class)
    val totalConfirmedAmount: BigDecimal,
    val confirmedCount: Int
)
