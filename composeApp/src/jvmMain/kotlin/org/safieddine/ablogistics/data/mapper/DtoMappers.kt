package org.safieddine.ablogistics.data.mapper

import org.safieddine.ablogistics.data.UserDTO
import org.safieddine.ablogistics.data.UserResponse
import org.safieddine.ablogistics.data.WarehouseDTO
import org.safieddine.ablogistics.data.WarehouseInfo
import org.safieddine.ablogistics.domain.User
import org.safieddine.ablogistics.domain.UserId
import org.safieddine.ablogistics.domain.Warehouse
import org.safieddine.ablogistics.domain.WarehouseId

fun UserResponse.toDomain(): User = User(
    id = UserId(username),
    fullName = fullName,
    phoneNumber = phoneNumber,
    roles = roles,
    warehouses = warehouses.map { WarehouseId(it.id) },
    isBlocked = isBlocked
)

fun UserDTO.toDomain(): User = User(
    id = UserId(username),
    fullName = fullName,
    phoneNumber = phoneNumber,
    roles = roles.toSet(),
    warehouses = warehouses.map { WarehouseId(it.id) },
    isBlocked = isBlocked
)

fun WarehouseDTO.toDomain(): Warehouse = Warehouse(
    id = WarehouseId(id),
    name = name,
    location = location,
    totalFunds = totalFunds,
    createdAt = createdAt,
    usersCount = users.size,
    customersCount = customersCount,
    receiptsCount = receiptsCount
)

fun WarehouseInfo.toDomain(): Warehouse = Warehouse(
    id = WarehouseId(id),
    name = name,
    location = null,
    totalFunds = null,
    createdAt = null,
    usersCount = 0,
    customersCount = 0,
    receiptsCount = 0
)

