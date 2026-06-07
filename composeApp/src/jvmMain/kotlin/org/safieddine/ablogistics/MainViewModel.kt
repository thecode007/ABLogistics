package org.safieddine.ablogistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.safieddine.ablogistics.data.AuthManager
import org.safieddine.ablogistics.data.UpdateUserRequest
import org.safieddine.ablogistics.data.WarehouseInfo
import org.safieddine.ablogistics.data.MaterialPriceDTO
import org.safieddine.ablogistics.data.NotificationManager
import org.safieddine.ablogistics.data.session.GlobalPriceStore
import org.safieddine.ablogistics.data.service.UserService
import org.safieddine.ablogistics.data.service.PriceService
import org.safieddine.ablogistics.data.service.AppSettingService

class MainViewModel(val userService: UserService = UserService,
    val authManager: AuthManager): ViewModel() {
    private val _usersLoading = MutableStateFlow(false)
    val usersLoading: StateFlow<Boolean> = _usersLoading

    private val _usersError = MutableStateFlow<String?>(null)
    val usersError: StateFlow<String?> = _usersError

    private val _selectedWarehouse = MutableStateFlow<WarehouseInfo?>(null)
    val selectedWarehouse: StateFlow<WarehouseInfo?> = _selectedWarehouse
    fun setSelectedWarehouse(warehouseDTO: WarehouseInfo?) {
        _selectedWarehouse.value = warehouseDTO
        authManager.setSelectedWarehouse(warehouseDTO)
    }

    private val _globalPrices = MutableStateFlow<List<MaterialPriceDTO>>(emptyList())
    val globalPrices: StateFlow<List<MaterialPriceDTO>> = _globalPrices

    private val _receiptCounter = MutableStateFlow<Long?>(null)
    val receiptCounter: StateFlow<Long?> = _receiptCounter

    init {
        fetchPrices()
        fetchReceiptCounter()
        observePriceUpdates()
    }

    fun fetchReceiptCounter() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = AppSettingService.getReceiptCounter()
            result.getOrNull()?.data?.nextReceiptNumber?.let {
                _receiptCounter.value = it
            }
        }
    }

    fun setReceiptCounter(startFrom: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = AppSettingService.setReceiptCounter(startFrom)
            if (result.isSuccess) {
                _receiptCounter.value = startFrom
            } else {
                println("Set counter failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun fetchPrices() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = PriceService.getGlobalPrices()
            if (result.isSuccess) {
                val data = result.getOrNull()?.data ?: emptyList()
                _globalPrices.value = data
                GlobalPriceStore.updatePrices(data)
            } else {
                println("Price Fetch Failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun updatePrices(prices: List<MaterialPriceDTO>) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = PriceService.updateGlobalPrices(prices)
            if (result.isSuccess) {
                fetchPrices()
            } else {
                println("Price Update Failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    private fun observePriceUpdates() {
        viewModelScope.launch {
            NotificationManager.lastEvent.collect { event ->
                if (event?.type == "PRICE_UPDATE") {
                    fetchPrices()
                }
            }
        }
    }



    fun clearError() {
        _usersError.value = null
    }
    fun updateAdminUser(username: String, fullName: String, phone: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = userService.updateAdminUser(
                UpdateUserRequest(
                    username = username,
                    fullName = fullName,
                    phoneNumber = phone,
                    password = password.ifBlank { null },
                    authManager.getCurrentUser()?.isAdmin() ?: false,
                    null
                )
            )
            if (result.isSuccess) {
                authManager.updateCurrentUSer(fullName, phone)
            } else {
                _usersError.value = "Failed to update user: ${result.exceptionOrNull()?.message}"
            }
        }
    }

}