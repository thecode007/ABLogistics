package org.safieddine.ablogistics.data

import kotlinx.serialization.Serializable

@Serializable
data class ApproveCustomerUpdateRequest(
    val requestedBy: String,
    val name: String? = null,
    val phoneNumber: String? = null,
    val location: String? = null,
    val warehouseId: Long? = null
)

@Serializable
data class RejectCustomerUpdateRequest(
    val requestedBy: String,
    val reason: String? = null
)

