package org.safieddine.ablogistics.data.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.safieddine.ablogistics.data.AuthStorage
import org.safieddine.ablogistics.data.UserResponse
import org.safieddine.ablogistics.data.WarehouseInfo
import org.safieddine.ablogistics.data.service.TokenProvider
import java.util.Locale

/**
 * Centralized session state. Exposes the current auth token, user, and selected warehouse
 * via StateFlow, and implements TokenProvider for network clients.
 */
object SessionStore : TokenProvider {
    private val storage = AuthStorage.getInstance()

    private val _token = MutableStateFlow(storage.getToken())
    val token: StateFlow<String?> = _token.asStateFlow()

    private val _refreshToken = MutableStateFlow(storage.getRefreshToken())
    val refreshToken: StateFlow<String?> = _refreshToken.asStateFlow()

    private val _currentUser = MutableStateFlow(storage.getUser())
    val currentUser: StateFlow<UserResponse?> = _currentUser.asStateFlow()

    private val _selectedWarehouse = MutableStateFlow(storage.getCurrentWarehouse())
    val selectedWarehouse: StateFlow<WarehouseInfo?> = _selectedWarehouse.asStateFlow()

    // App language
    val englishLocale = Locale.ENGLISH
    val frenchLocale = Locale.FRENCH

    private val _language = MutableStateFlow(
        when (storage.getLanguage().uppercase()) {
            "EN" -> englishLocale
            else -> frenchLocale
        }
    )

    init {
        print(storage.getLanguage())
        Locale.setDefault(_language.value)
    }
    val language: StateFlow<Locale> = _language.asStateFlow()

    override fun currentToken(): String? = _token.value
    override fun currentRefreshToken(): String? = _refreshToken.value

    fun setToken(value: String?) {
        _token.value = value
    }

    fun setRefreshToken(value: String?) {
        _refreshToken.value = value
    }

    fun setCurrentUser(value: UserResponse?) {
        _currentUser.value = value
    }

    fun setSelectedWarehouse(value: WarehouseInfo?) {
        _selectedWarehouse.value = value
    }

    fun setLanguage(value: Locale) {
        _language.value = value
        storage.saveLanguage(value.language)
        try {
            Locale.setDefault(value)
        } catch (_: Exception) { }
    }

    fun clear() {
        _token.value = null
        _refreshToken.value = null
        _currentUser.value = null
        _selectedWarehouse.value = null
    }
}
