package org.safieddine.ablogistics.data.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.safieddine.ablogistics.data.service.WarehouseService

object WarehouseFundsStore {
    private val _totalFunds = MutableStateFlow(0.0)
    val totalFunds: StateFlow<Double> = _totalFunds.asStateFlow()

    private val _realFunds = MutableStateFlow(0.0)
    val realFunds: StateFlow<Double> = _realFunds.asStateFlow()

    suspend fun refresh(warehouseId: Long) {
        val res = WarehouseService.getWarehouseFunds(warehouseId)
        if (res.isSuccess) {
            val dto = res.getOrNull()?.data
            _totalFunds.value = dto?.totalFunds ?: 0.0
            _realFunds.value = dto?.realFunds ?: 0.0
        }
    }

    fun setReal(value: Double?) {
        if (value != null) _realFunds.value = value
    }

    fun setTotal(value: Double?) {
        if (value != null) _totalFunds.value = value
    }
}
