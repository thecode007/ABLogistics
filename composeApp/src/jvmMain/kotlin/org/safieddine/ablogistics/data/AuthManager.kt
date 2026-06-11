package org.safieddine.ablogistics.data

import org.safieddine.ablogistics.data.service.AuthService
import org.safieddine.ablogistics.data.service.UserService
import org.safieddine.ablogistics.data.session.SessionStore
import org.safieddine.ablogistics.data.network.HttpClientFactory

object AuthManager {
    private val authService = AuthService()

    private val authStorage = AuthStorage.getInstance()

    private val userService = UserService

    suspend fun login(username: String, password: String): Result<LoginData> {
        val deviceToken = try { DeviceIdProvider.getOrCreate() } catch (_: Exception) { null }
        val result = authService.login(username, password, deviceToken = deviceToken, platform = "DESKTOP")

        result.onSuccess { response ->
            response.data?.let { loginData ->
                authStorage.saveAuthData(loginData)
                // Sync session store
                SessionStore.setToken(loginData.token)
                SessionStore.setRefreshToken(loginData.refreshToken)
                SessionStore.setCurrentUser(loginData.user)
                HttpClientFactory.clearTokensCache()
            }
        }

        return result.map { it.data ?: throw Exception("No login data received") }
    }

    fun isLoggedIn(): Boolean {
        return authStorage.isLoggedIn()
    }


    fun setSelectedWarehouse(warehouseDTO: WarehouseInfo?) {
        authStorage.saveCurrentWarehouse(warehouseDTO)
        SessionStore.setSelectedWarehouse(warehouseDTO)
    }

    fun getSelectedWarehouse(): WarehouseInfo? {
        return authStorage.getCurrentWarehouse()
    }

    fun getCurrentUser() = authStorage.getUser()

    fun updateCurrentUSer(fullName: String, phone: String) {
        val currentUser = authStorage.getUser()
        if (currentUser != null) {
            authStorage.saveUser(currentUser.copy(
                fullName = fullName,
                phoneNumber = phone
            ))
            // Sync session store
            SessionStore.setCurrentUser(authStorage.getUser())
        }
    }

    suspend fun isBlocked(): Boolean? {
       return try {
           val isBlocked =  userService.checkUserBlocked(username =getCurrentUser()?.username?:"").getOrNull()?.data ?: false

           println("Calling is blocked ---> $isBlocked")
            isBlocked
        }catch (ex: Exception) {
            println(ex.message)
            null
        }

    }

    suspend fun validateCurrentToken(): Boolean {
        val token = authStorage.getToken()
        val refreshToken = authStorage.getRefreshToken()
        return if (!token.isNullOrEmpty()) {
            try {
                var isAuthenticated = authService.validateToken(token)
                if (!isAuthenticated && !refreshToken.isNullOrEmpty()) {
                    val refreshResult = authService.refreshToken(refreshToken)
                    refreshResult.onSuccess { response ->
                        response.data?.let { loginData ->
                            authStorage.saveAuthData(loginData)
                            // Sync session store
                            SessionStore.setToken(loginData.token)
                            SessionStore.setRefreshToken(loginData.refreshToken)
                            SessionStore.setCurrentUser(loginData.user)
                            HttpClientFactory.clearTokensCache()
                            isAuthenticated = true
                        }
                    }
                }
                if (!isAuthenticated) {
                    logout()
                }
                isAuthenticated
            } catch (e: Exception) {
                println(e)
                false
            }
        } else {
            false
        }
    }

    suspend fun logout() {
        val token = authStorage.getToken()
        if (!token.isNullOrEmpty()) {
            val deviceToken = try { DeviceIdProvider.getOrCreate() } catch (_: Exception) { null }
            try { authService.logout(token, deviceToken) } catch (_: Exception) {}
        }
        authStorage.clearAuthData()
        SessionStore.clear()
    }

    fun close() {
        authService.close()
    }
}
