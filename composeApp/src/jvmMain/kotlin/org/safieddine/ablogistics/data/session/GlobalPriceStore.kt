package org.safieddine.ablogistics.data.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.safieddine.ablogistics.data.MaterialPriceDTO

object GlobalPriceStore {
    private val _prices = MutableStateFlow<List<MaterialPriceDTO>>(emptyList())
    val prices: StateFlow<List<MaterialPriceDTO>> = _prices

    fun updatePrices(newPrices: List<MaterialPriceDTO>) {
        _prices.value = newPrices
    }
}
