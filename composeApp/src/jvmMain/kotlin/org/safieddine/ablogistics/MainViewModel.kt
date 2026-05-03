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
import org.safieddine.ablogistics.data.service.UserService

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