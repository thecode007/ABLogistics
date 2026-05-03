package org.safieddine.ablogistics.data

import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.delete
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import org.safieddine.ablogistics.data.network.HttpClientFactory

object NotificationsApi {
    private fun endpoint(baseUrl: String, path: String): String {
        val root = baseUrl.trimEnd('/')
        val p = if (path.startsWith('/')) path else "/$path"
        return "$root$p"
    }

    suspend fun registerToken(baseUrl: String, jwt: String, token: String) {
        val url = endpoint(baseUrl, "/api/notifications/register-token")
        HttpClientFactory.httpClient.post(url) {
            contentType(ContentType.Application.Json)
            headers { append(HttpHeaders.Authorization, "Bearer $jwt") }
            setBody(mapOf("token" to token, "platform" to "DESKTOP"))
        }.body<Unit>()
    }

    suspend fun unregisterToken(baseUrl: String, jwt: String, token: String) {
        val url = endpoint(baseUrl, "/api/notifications/register-token")
        HttpClientFactory.httpClient.delete(url) {
            contentType(ContentType.Application.Json)
            headers { append(HttpHeaders.Authorization, "Bearer $jwt") }
            setBody(mapOf("token" to token))
        }.body<Unit>()
    }
}

