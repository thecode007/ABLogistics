package org.safieddine.ablogistics.data

import kotlinx.serialization.Serializable

@Serializable
data class UserSummaryDTO(
    val username: String,
    val fullName: String,
    val phoneNumber: String
)


@Serializable
data class WarehouseDTO(
    val id: Long = 0L,
    val name: String,
    val location: String? = null,
    val isoCode: String = "",
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val totalFunds: java.math.BigDecimal? = null,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val realFunds: java.math.BigDecimal? = null,
    val createdAt: String? = null,
    val users: List<UserSummaryDTO> = emptyList(),
    val customersCount: Int,
    val receiptsCount: Int
)

@Serializable data class UserDTO(
    val username: String,
    val fullName: String,
    val phoneNumber: String,
    val createdAt: String? = null,
    val warehouses: List<WarehouseInfo> = emptyList(),
    val roles: List<String> = emptyList(),
    val isBlocked: Boolean
) {
    val isAdmin: Boolean
        get() {
            return roles.any { i ->
                i == "ROLE_ADMIN"
            }
        }


    val userWarehouse: WarehouseInfo?
        get() {
           return warehouses.firstOrNull()
        }

}
