package org.safieddine.ablogistics.data

import kotlinx.serialization.Serializable
@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class BaseResponse<T>(
    val success: Boolean,
    val message: String,
    val code: String,
    val data: T? = null,
    val errors: List<ErrorDetail>? = null,
    val timestamp: Long
)

@Serializable
data class ErrorDetail(
    val field: String? = null,
    val message: String,
    val code: String? = null
)

@Serializable
data class LoginData(
    val token: String,
    val refreshToken: String? = null,
    val user: UserResponse
)

@Serializable
data class UserResponse(
    val username: String,
    val phoneNumber: String,
    val fullName: String,
    val roles: Set<String>,
    val warehouses: Set<WarehouseInfo>,
    val isBlocked: Boolean
) {
    fun isAdmin(): Boolean{
        return roles.firstOrNull()  == "ROLE_ADMIN"
    }
}

@Serializable
data class WarehouseInfo(
    val id: Long,
    val name: String
)
