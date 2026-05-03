package org.safieddine.ablogistics.ui.screen.adminScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.safieddine.ablogistics.data.AuthManager
import org.safieddine.ablogistics.data.CreateUserRequest
import org.safieddine.ablogistics.data.UpdateUserRequest
import org.safieddine.ablogistics.data.UserDTO
import org.safieddine.ablogistics.data.service.UserService

class AdminUsersViewModel(
    private val userService: UserService = UserService,
    private val authManager: AuthManager = AuthManager
) : ViewModel() {

    private val _users = MutableStateFlow<List<UserDTO>>(emptyList())
    val users: StateFlow<List<UserDTO>> = _users

    private val _usersLoading = MutableStateFlow(false)
    val usersLoading: StateFlow<Boolean> = _usersLoading

    private val _usersError = MutableStateFlow<String?>(null)
    val usersError: StateFlow<String?> = _usersError

    fun changeUserStatus(userDTO: UserDTO, status: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _usersLoading.value = true
                if (status) {
                    userService.blockUser(userDTO.username)
                } else {
                    userService.unblockUser(userDTO.username)
                }
                delay(1000)
                loadUsers()
            } catch (e: Exception) {
                _usersError.value = e.message
            } finally {
                _usersLoading.value = false
            }
        }
    }

    fun loadUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            _usersLoading.value = true
            _usersError.value = null
            try {
                val response = userService.getAllUsers()
                if (response.isSuccess) {
                    val data = response.getOrNull()?.data
                        ?.filter { it.username.isNotBlank() && it.username != authManager.getCurrentUser()?.username }
                        ?: emptyList()
                    _users.value = data
                } else {
                    _usersError.value = response.exceptionOrNull()?.message ?: "Failed to load users"
                }
            } catch (e: Exception) {
                _usersError.value = e.message
            } finally {
                _usersLoading.value = false
            }
        }
    }

    fun deleteAdminUser(username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _usersLoading.value = true
            val result = userService.deleteUser(username)
            if (result.isSuccess) {
                loadUsers()
            } else {
                _usersLoading.value = false
                _usersError.value = "Failed to delete user: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun updateAdminUser(
        username: String,
        fullName: String,
        phone: String,
        password: String,
        isAdmin: Boolean,
        warehouseID: Long?,
        onUpdateSuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = userService.updateAdminUser(
                UpdateUserRequest(
                    username = username,
                    fullName = fullName,
                    phoneNumber = phone,
                    password = password.ifBlank { null },
                    isAdmin = isAdmin,
                    warehouseId = warehouseID
                )
            )
            if (result.isSuccess) {
                loadUsers()
                delay(500)
                onUpdateSuccess()
            } else {
                _usersError.value = "Failed to update user"
                println("Failed to update user: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun createAdminUser(
        username: String,
        fullName: String,
        phone: String,
        password: String,
        isAdmin: Boolean,
        warehouseID: Long? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _usersLoading.value = true
            val result = userService.createAdminUser(
                CreateUserRequest(
                    username = username,
                    fullName = fullName,
                    phoneNumber = phone,
                    password = password,
                    admin = isAdmin,
                    warehouseID = warehouseID
                )
            )
            result.onSuccess { loadUsers() }
            result.onFailure {
                _usersLoading.value = false
                _usersError.value = it.message
            }
        }
    }
}

