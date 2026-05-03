package org.safieddine.ablogistics.data

import kotlinx.serialization.Serializable

@Serializable
data class NotificationListItem(
    val id: Long,
    val type: String,
    val title: String,
    val body: String,
    val data: Map<String, String> = emptyMap(),
    val status: String,
    val isRead: Boolean,
    val createdAt: String? = null,
    val sentAt: String? = null,
    val readAt: String? = null
)

