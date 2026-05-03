package org.safieddine.ablogistics.data

import kotlinx.serialization.Serializable

@Serializable
data class LogoutRequest(
    val token: String? = null
)

