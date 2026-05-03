package org.safieddine.ablogistics.data

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.sse.ClientSSESessionWithDeserialization
import io.ktor.client.plugins.sse.deserialize
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.url
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.sse.TypedServerSentEvent
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.safieddine.ablogistics.data.network.HttpClientFactory

@Serializable
data class NotificationEvent(
    val type: String,
    val title: String,
    val message: String? = null,
    // Some events (e.g., DECISION) use 'body' instead of 'message'
    val body: String? = null,
    val data: Map<String, String> = emptyMap()
)

object NotificationSseClient {
    @Volatile
    var onUnauthorizedRefresh: (suspend () -> String?)? = null

    private fun endpoint(baseUrl: String): String {
        val root = baseUrl.trimEnd('/')
        return "$root/api/notifications/stream"
    }

    suspend fun listen(
        baseUrl: String,
        jwt: String,
        onEvent: (name: String, evt: NotificationEvent) -> Unit
    ) {
        var attempt = 0
        var currentJwt = jwt
        while (true) {
            val delayMs = listOf(1_000L, 2_000L, 5_000L, 10_000L, 20_000L, 30_000L)[minOf(attempt, 5)]
            try {
                println("[SSE] Connecting to ${endpoint(baseUrl)} (attempt=$attempt)")

                HttpClientFactory.httpClient.sse(
                    request = {
                        url(endpoint(baseUrl))
                        headers.append(HttpHeaders.Authorization, "Bearer $currentJwt")
                    },
                    deserialize = { typeInfo, jsonString ->
                        val kType = typeInfo.kotlinType!!
                        val serializer = Json.serializersModule.serializer(kType)
                        // Lenient, ignore unknown keys; coerce to defaults
                        val relaxed = Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                            coerceInputValues = true
                        }
                        relaxed.decodeFromString(serializer, jsonString)!!
                    }
                ) {
                    // Signal connection established even if server doesn't send a 'connected' event
                    try {
                        onEvent(
                            "connected",
                            NotificationEvent(
                                type = "connected",
                                title = "Connected",
                                message = "SSE connected"
                            )
                        )
                    } catch (_: Exception) {}
                    consumeEvents(onEvent)
                }

                // normal completion; reset attempts
                attempt = 0
            } catch (e: Exception) {
                val unauthorized = (e as? ClientRequestException)?.response?.status == HttpStatusCode.Unauthorized
                if (unauthorized) {
                    println("[SSE] Unauthorized (401). Attempting token refresh...")
                    val refreshed = onUnauthorizedRefresh?.invoke()
                    if (refreshed != null) {
                        currentJwt = refreshed
                        attempt = 0
                        continue
                    } else {
                        println("[SSE] No refreshed token available; stopping.")
                        try {
                            onEvent(
                                "disconnected",
                                NotificationEvent(
                                    type = "disconnected",
                                    title = "Disconnected",
                                    message = "Unauthorized; session ended"
                                )
                            )
                        } catch (_: Exception) {}
                        return
                    }
                }
                attempt++
                println("[SSE] Error: ${e.message}. Reconnecting in ${delayMs} ms (attempt=${attempt})")
                try {
                    onEvent(
                        "disconnected",
                        NotificationEvent(
                            type = "disconnected",
                            title = "Disconnected",
                            message = e.message ?: "Connection lost"
                        )
                    )
                } catch (_: Exception) {}
                delay(delayMs)
                continue
            }
        }
    }

    private suspend fun ClientSSESessionWithDeserialization.consumeEvents(
        onEvent: (name: String, evt: NotificationEvent) -> Unit
    ) {
        incoming.collect { event: TypedServerSentEvent<String> ->
            val eventName = event.event ?: "message"
            val dataText = event.data ?: ""
            val trimmed = dataText.trim()

            // Handle non-JSON keep-alive/handshake events like 'connected: ok'
            if (eventName.equals("connected", ignoreCase = true)) {
                onEvent(
                    "connected",
                    NotificationEvent(
                        type = "connected",
                        title = "Connected",
                        message = trimmed.ifBlank { "Connected" }
                    )
                )
                return@collect
            }

            if (trimmed.isBlank()) return@collect
            try {
                val evt = if (trimmed.startsWith("{")) {
                    try {
                        // Try to parse to NotificationEvent (relaxed). If it comes with 'body' only,
                        // normalize 'message' to use body when message is null.
                        val raw = deserialize<NotificationEvent>(trimmed)!!
                        raw.copy(message = raw.message ?: raw.body)
                    } catch (_: Exception) {
                        val dto = deserialize<NotificationDTO>(trimmed)!!
                        NotificationEvent(
                            type = dto.type ?: eventName,
                            title = dto.title,
                            message = dto.message,
                            data = mapOf("id" to dto.id, "timestamp" to dto.timestamp.toString())
                        )
                    }
                } else {
                    // Plain-text payload; wrap into a NotificationEvent
                    NotificationEvent(
                        type = eventName,
                        title = eventName,
                        message = trimmed
                    )
                }
                onEvent(eventName, evt)
            } catch (ex: Exception) {
                println("[SSE] Failed to parse event '$eventName': ${ex.message}")
                // Last-resort fallback: surface raw payload
                onEvent(
                    eventName,
                    NotificationEvent(
                        type = eventName,
                        title = eventName,
                        message = trimmed
                    )
                )
            }
        }
    }
}
