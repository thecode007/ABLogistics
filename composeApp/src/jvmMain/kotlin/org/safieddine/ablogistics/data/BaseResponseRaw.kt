package org.safieddine.ablogistics.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class BaseResponseRaw(
    val success: Boolean,
    val message: String,
    val code: String,
    val data: JsonElement? = null,
    val errors: List<ErrorDetail>? = null,
    val timestamp: Long
)

