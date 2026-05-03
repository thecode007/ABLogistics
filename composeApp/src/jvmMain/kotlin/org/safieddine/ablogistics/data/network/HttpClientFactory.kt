package org.safieddine.ablogistics.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.sse.SSE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {
    val httpClient: HttpClient by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }
            install(Logging) { level = LogLevel.INFO; logger = Logger.DEFAULT }
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 30_000
            }
            install(SSE)
        }
    }
}
