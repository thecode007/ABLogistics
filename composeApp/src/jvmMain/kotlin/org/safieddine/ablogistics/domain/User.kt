package org.safieddine.ablogistics.domain

@JvmInline
value class UserId(val value: String)

@JvmInline
value class WarehouseId(val value: Long)

data class User(
    val id: UserId,
    val fullName: String,
    val phoneNumber: String,
    val roles: Set<String>,
    val warehouses: List<WarehouseId>,
    val isBlocked: Boolean
)

data class Warehouse(
    val id: WarehouseId,
    val name: String,
    val location: String?,
    val totalFunds: Double?,
    val createdAt: String?,
    val usersCount: Int,
    val customersCount: Int,
    val receiptsCount: Int
)

