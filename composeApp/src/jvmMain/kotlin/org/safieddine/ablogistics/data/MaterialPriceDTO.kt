package org.safieddine.ablogistics.data

import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
enum class MaterialType {
    FUEL, DIESEL
}

@Serializable
data class MaterialPriceDTO(
    val materialType: MaterialType,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val costPrice: BigDecimal,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val sellingPrice: BigDecimal
)
