package org.safieddine.ablogistics.data

import kotlinx.serialization.Serializable

@Serializable
data class NotificationDTO(
    val id: String,
    val title: String,
    val message: String,
    val type: String? = null,
    val timestamp: Long
)

@Serializable
data class RegisterTokenRequest(
    val token: String,
    val platform: String = "desktop"
)

