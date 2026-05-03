package org.safieddine.ablogistics.data

import kotlinx.serialization.Serializable
import kotlin.Boolean

@Serializable
data class UpdateUserRequest(
    val username: String,
    val fullName: String,
    val phoneNumber: String,
    val password: String? = null,
    val isAdmin: Boolean ,
    val warehouseId: Long?
)