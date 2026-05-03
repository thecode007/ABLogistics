package org.safieddine.ablogistics.ui.screen.adminScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.safieddine.ablogistics.data.CreateWarehouseRequest
import org.safieddine.ablogistics.data.WarehouseDTO
import org.safieddine.ablogistics.data.WarehouseUpdateRequest
import org.safieddine.ablogistics.data.service.UserService
import org.safieddine.ablogistics.data.service.WarehouseService

class AdminWarehousesViewModel(
    private val warehouseService: WarehouseService = WarehouseService,
    private val userService: UserService = UserService

) : ViewModel() {

    private val _warehouses = MutableStateFlow<List<WarehouseDTO>>(emptyList())
    val warehouses: StateFlow<List<WarehouseDTO>> = _warehouses

    private val _warehousesLoading = MutableStateFlow(false)
    val warehousesLoading: StateFlow<Boolean> = _warehousesLoading

    private val _warehousesError = MutableStateFlow<String?>(null)
    val warehousesError: StateFlow<String?> = _warehousesError

    init {
        viewModelScope.launch(Dispatchers.IO){
            loadWarehouses()
        }
    }

    fun loadWarehouses() {
        viewModelScope.launch(Dispatchers.IO) {
            _warehousesLoading.value = true
            _warehousesError.value = null
            try {
                val response = warehouseService.getAllWarehouses()
                if (response.isSuccess) {
                    val houses = response.getOrNull()?.data?.filter { it.id != 1L }
                    _warehouses.value = houses ?: emptyList()
                } else {
                    _warehousesError.value = response.exceptionOrNull()?.message ?: "Failed to load warehouses"
                }
            } catch (e: Exception) {
                _warehousesError.value = e.message
            } finally {
                _warehousesLoading.value = false
            }
        }
    }

    fun createWarehouse(name: String, location: String, totalFunds: Double, isoCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _warehousesLoading.value = true
            val result = warehouseService.createWarehouse(
                CreateWarehouseRequest(name, location, totalFunds, isoCode)
            )
            result.onSuccess { loadWarehouses() }
            result.onFailure { _warehousesError.value = it.message }
        }
    }

    fun unAssign(name: String,onSucces:() -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _warehousesLoading.value = true
            val result = userService.unassignUserFromWarehouses(name)
            result.onSuccess {
                loadWarehouses()
                delay(500)
                onSucces()
            }
            result.onFailure { _warehousesError.value = it.message }
        }
    }

    fun updateWarehouse(id: Long, warehouseUpdateRequest: WarehouseUpdateRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            _warehousesLoading.value = true
            val result = warehouseService.updateWarehouse(id, warehouseUpdateRequest)
            if (result.isSuccess) {
                loadWarehouses()
            } else {
                _warehousesLoading.value = false
                _warehousesError.value = "Failed to update warehouse: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun deleteWarehouse(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _warehousesLoading.value = true
            val result = warehouseService.deleteWarehouse(id)
            if (result.isSuccess) {
                loadWarehouses()
            } else {
                _warehousesLoading.value = false
                _warehousesError.value = "Failed to delete warehouse: ${result.exceptionOrNull()?.message}"
            }
        }
    }
}
