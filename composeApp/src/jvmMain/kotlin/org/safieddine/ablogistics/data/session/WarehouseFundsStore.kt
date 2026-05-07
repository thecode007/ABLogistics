package org.safieddine.ablogistics.data.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.safieddine.ablogistics.data.service.WarehouseService

import java.math.BigDecimal

object WarehouseFundsStore {
    private val _totalFunds = MutableStateFlow(BigDecimal.ZERO)
    val totalFunds: StateFlow<BigDecimal> = _totalFunds.asStateFlow()

    private val _realFunds = MutableStateFlow(BigDecimal.ZERO)
    val realFunds: StateFlow<BigDecimal> = _realFunds.asStateFlow()

    suspend fun refresh(warehouseId: Long) {
        val res = WarehouseService.getWarehouseFunds(warehouseId)
        if (res.isSuccess) {
            val dto = res.getOrNull()?.data
            _totalFunds.value = dto?.totalFunds ?: BigDecimal.ZERO
            _realFunds.value = dto?.realFunds ?: BigDecimal.ZERO
        }
    }

    fun setReal(value: BigDecimal?) {
        if (value != null) _realFunds.value = value
    }

    fun setTotal(value: BigDecimal?) {
        if (value != null) _totalFunds.value = value
    }
}
