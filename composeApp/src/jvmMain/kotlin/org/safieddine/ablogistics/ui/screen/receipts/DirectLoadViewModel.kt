package org.safieddine.ablogistics.ui.screen.receipts

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.safieddine.ablogistics.data.*
import org.safieddine.ablogistics.data.service.*
import java.math.BigDecimal
import java.math.RoundingMode
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

class DirectLoadViewModel : ViewModel() {
    private val _customers = MutableStateFlow<List<CustomerResponse>>(emptyList())
    val customers: StateFlow<List<CustomerResponse>> = _customers

    private val _suppliers = MutableStateFlow<List<SupplierDTO>>(emptyList())
    val suppliers: StateFlow<List<SupplierDTO>> = _suppliers

    private val _brvs = MutableStateFlow<List<BRVDTO>>(emptyList())
    val brvs: StateFlow<List<BRVDTO>> = _brvs

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _success = MutableStateFlow<String?>(null)
    val success: StateFlow<String?> = _success

    // Form State
    var selectedCustomer by mutableStateOf<CustomerResponse?>(null)
    var selectedSupplier by mutableStateOf<SupplierDTO?>(null)
    var selectedBrv by mutableStateOf<BRVDTO?>(null)
    var loadedQuantity by mutableStateOf("")
    var costPrice by mutableStateOf("")
    var sellingPrice by mutableStateOf("")
    var brvCost by mutableStateOf("")
    var description by mutableStateOf("")

    // Preview state
    var projectedRevenue by mutableStateOf(BigDecimal.ZERO)
    var projectedProfit by mutableStateOf(BigDecimal.ZERO)

    fun loadInitialData(warehouseId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val custRes = CustomerService.list(warehouseId)
                _customers.value = custRes.getOrNull()?.data?.customers?.filter { it.warehouseId == warehouseId } ?: emptyList()

                val supRes = SupplierService.getSuppliers()
                _suppliers.value = supRes.getOrNull()?.data ?: emptyList()

                val fleetRes = BRVService.getFleetStatus()
                _brvs.value = fleetRes.getOrNull()?.data?.brvs ?: emptyList()
            } catch (e: Exception) {
                _error.value = "Failed to load data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCalculations() {
        val qty = loadedQuantity.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val cp = costPrice.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val sp = sellingPrice.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val bc = brvCost.toBigDecimalOrNull() ?: BigDecimal.ZERO

        projectedRevenue = sp.multiply(qty).add(bc).setScale(4, RoundingMode.HALF_UP)
        projectedProfit = sp.subtract(cp).multiply(qty).setScale(4, RoundingMode.HALF_UP)
    }

    fun processLoad(warehouseId: Long) {
        val qty = loadedQuantity.toBigDecimalOrNull() ?: return
        val cp = costPrice.toBigDecimalOrNull() ?: return
        val sp = sellingPrice.toBigDecimalOrNull() ?: return
        val bc = brvCost.toBigDecimalOrNull() ?: return
        val cust = selectedCustomer ?: return
        val sup = selectedSupplier ?: return
        val brv = selectedBrv ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _success.value = null
            
            val req = ProcessLoadRequest(
                brvId = brv.id,
                supplierId = sup.id,
                customerId = cust.id,
                warehouseId = warehouseId,
                loadedQuantity = qty,
                costPrice = cp,
                sellingPrice = sp,
                brvCost = bc,
                description = description.ifBlank { "Direct Load: ${brv.plateNumber}" }
            )

            val res = BRVService.processLoad(req)
            if (res.isSuccess) {
                _success.value = "Load processed successfully! Linked receipts created."
                clearForm()
            } else {
                _error.value = res.exceptionOrNull()?.message ?: "Unknown error"
            }
            _isLoading.value = false
        }
    }

    private fun clearForm() {
        selectedCustomer = null
        selectedSupplier = null
        selectedBrv = null
        loadedQuantity = ""
        costPrice = ""
        sellingPrice = ""
        brvCost = ""
        description = ""
        projectedRevenue = BigDecimal.ZERO
        projectedProfit = BigDecimal.ZERO
    }
}

fun String.toBigDecimalOrNull(): BigDecimal? {
    return try {
        BigDecimal(this.replace(",", ""))
    } catch (e: Exception) {
        null
    }
}
