package org.safieddine.ablogistics.data

import kotlinx.serialization.json.Json
import java.util.prefs.Preferences

class AuthStorage {
    private val preferences: Preferences = Preferences.userRoot().node("warehouse_hub")
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun saveAuthData(loginData: LoginData) {
        preferences.put("auth_token", loginData.token)
        loginData.refreshToken?.let { preferences.put("refresh_token", it) }
        preferences.put("user_data", json.encodeToString(loginData.user))
        preferences.putBoolean("is_logged_in", true)
        preferences.putLong("login_timestamp", System.currentTimeMillis())
    }

    fun saveCurrentWarehouse(warehouseDTO: WarehouseInfo?) {
        if (warehouseDTO != null) {
            preferences.put("warehouse_data",
                json.encodeToString(warehouseDTO)
            )
        }
        else{
            preferences.remove("warehouse_data")
            preferences.flush()
        }
    }

    fun getCurrentWarehouse():WarehouseInfo? {
        return try {
            val userJson = preferences.get("warehouse_data", null)
            userJson?.let { json.decodeFromString<WarehouseInfo>(it) }
        } catch (e: Exception) {
            println("Error decoding warehouse data: ${e.message}")
            null
        }
    }

    fun saveUser(userResponse: UserResponse) {
        preferences.put("user_data", json.encodeToString(userResponse))
    }

    fun getToken(): String? {
        return preferences.get("auth_token", null)
    }

    fun getRefreshToken(): String? {
        return preferences.get("refresh_token", null)
    }

    fun getUser(): UserResponse? {
        return try {
            val userJson = preferences.get("user_data", null)
            userJson?.let { json.decodeFromString<UserResponse>(it) }
        } catch (e: Exception) {
            println("Error decoding user data: ${e.message}")
            null
        }
    }

    fun isLoggedIn(): Boolean {
        return preferences.getBoolean("is_logged_in", false)
    }

    fun clearAuthData() {
        preferences.remove("auth_token")
        preferences.remove("refresh_token")
        preferences.remove("user_data")
        preferences.remove("warehouse_data")
        preferences.remove("is_logged_in")
        preferences.remove("login_timestamp")
        preferences.flush() // Ensure changes are written
    }

    // Language persistence
    fun saveLanguage(langCode: String) {
        preferences.put("language", langCode)
    }

    fun getLanguage(): String {
        return preferences.get("language", "FR")
    }

    companion object {
        private var instance: AuthStorage? = null
        fun getInstance(): AuthStorage {
            return instance ?: synchronized(this) {
                instance ?: AuthStorage().also { instance = it }
            }
        }
    }
}
