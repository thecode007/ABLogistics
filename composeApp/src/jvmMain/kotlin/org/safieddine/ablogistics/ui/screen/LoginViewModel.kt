package org.safieddine.ablogistics.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.safieddine.ablogistics.data.AuthManager
import org.safieddine.ablogistics.data.LoginData
import org.safieddine.ablogistics.data.session.SessionStore

class LoginViewModel(
    private val authManager: AuthManager = AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableStateFlow<LoginNavigationEvent?>(null)
    val navigationEvent: StateFlow<LoginNavigationEvent?> = _navigationEvent.asStateFlow()

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(
            username = username,
            errorMessage = null
        )
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            errorMessage = null
        )
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            passwordVisible = !_uiState.value.passwordVisible
        )
    }

    fun login() {
        val currentState = _uiState.value

        if (currentState.username.isBlank() || currentState.password.isBlank()) {
            _uiState.value = currentState.copy(
                errorMessage = "Please enter both username and password"
            )
            return
        }

        _uiState.value = currentState.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = authManager.login(currentState.username.trim(), currentState.password)

                result.onSuccess { loginData ->
                    _uiState.value = currentState.copy(
                        isLoading = false
                    )
                    if (!loginData.user.isAdmin()) {
                        SessionStore.setSelectedWarehouse(loginData.user.warehouses.firstOrNull())
                    }
                    _navigationEvent.value = LoginNavigationEvent.LoginSuccess(loginData)
                }

                result.onFailure { exception ->
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Login failed. Please check your credentials and try again."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "Network error: ${e.message}"
                )
            }
        }
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null
        )
    }

}

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class LoginNavigationEvent {
    data class LoginSuccess(val loginData: LoginData) : LoginNavigationEvent()
}