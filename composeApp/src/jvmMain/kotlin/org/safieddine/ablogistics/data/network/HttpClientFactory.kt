package org.safieddine.ablogistics.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.safieddine.ablogistics.data.AuthStorage
import org.safieddine.ablogistics.data.BaseResponse
import org.safieddine.ablogistics.data.LoginData
import org.safieddine.ablogistics.data.RefreshTokenRequest
import org.safieddine.ablogistics.data.config.AppConfig
import org.safieddine.ablogistics.data.session.SessionStore

object HttpClientFactory {
    
    // Dedicated client for refresh to avoid circular interceptors
    private val authClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
        defaultRequest {
            url(AppConfig.baseUrl)
            contentType(ContentType.Application.Json)
        }
    }

    val httpClient: HttpClient by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }
            install(Logging) { 
                level = LogLevel.INFO
                logger = Logger.DEFAULT 
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 30_000
            }
            install(SSE)
            
            install(Auth) {
                bearer {
                    loadTokens {
                        val token = SessionStore.currentToken()
                        val refreshToken = SessionStore.currentRefreshToken()
                        if (token != null && refreshToken != null) {
                            BearerTokens(token, refreshToken)
                        } else null
                    }
                    
                    refreshTokens {
                        val refreshToken = SessionStore.currentRefreshToken() ?: return@refreshTokens null
                        
                        try {
                            val response: BaseResponse<LoginData> = authClient.post("auth/refresh") {
                                setBody(RefreshTokenRequest(refreshToken))
                                markAsRefreshTokenRequest()
                            }.body()
                            
                            if (response.success && response.data != null) {
                                val loginData = response.data
                                SessionStore.setToken(loginData.token)
                                SessionStore.setRefreshToken(loginData.refreshToken)
                                AuthStorage.getInstance().saveAuthData(loginData)
                                
                                BearerTokens(loginData.token, loginData.refreshToken!!)
                            } else {
                                // Refresh failed, clear session
                                SessionStore.clear()
                                AuthStorage.getInstance().clearAuthData()
                                null
                            }
                        } catch (e: Exception) {
                            println("Automatic token refresh failed: ${e.message}")
                            SessionStore.clear()
                            AuthStorage.getInstance().clearAuthData()
                            null
                        }
                    }
                    
                    sendWithoutRequest { request ->
                        // Don't send token for auth endpoints
                        request.url.encodedPath.contains("/auth/") || request.url.encodedPath.endsWith("/auth")
                    }
                }
            }

            defaultRequest {
                url(AppConfig.baseUrl)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }
        }
    }

    fun clearTokensCache() {
        try {
            val authPlugin = httpClient.plugin(Auth)
            val field = authPlugin::class.java.getDeclaredField("providers")
            field.isAccessible = true
            val providers = field.get(authPlugin) as? List<*> ?: return
            for (provider in providers) {
                if (provider != null) {
                    try {
                        val method = provider::class.java.getMethod("clearToken")
                        method.isAccessible = true
                        method.invoke(provider)
                    } catch (_: Exception) {}
                }
            }
        } catch (e: Exception) {
            println("Failed to clear Ktor token cache via reflection: ${e.message}")
        }
    }
}
