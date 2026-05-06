package org.safieddine.ablogistics.data

import kotlinx.serialization.Serializable

@Serializable
data class BRVDTO(
    val id: Long,
    val plateNumber: String,
    val driverName: String?,
    val driverPhone: String? = null,
    val vendor: String?,
    val capacity: Double,
    val status: String
)

@Serializable
data class FleetStatusResponse(
    val brvs: List<BRVDTO>
)

@Serializable
data class ProfitAnalysisResponse(
    val brvPlateNumber: String,
    val totalProfit: Double,
    val totalExpectedRevenue: Double,
    val totalSupplierDebt: Double,
    val totalQuantityLoaded: Double
)

@Serializable
data class ProcessLoadRequest(
    val brvId: Long,
    val supplierId: Long,
    val customerId: Long,
    val warehouseId: Long,
    val quantity: Double,
    val costPrice: Double,
    val sellingPrice: Double,
    val description: String? = null
)
