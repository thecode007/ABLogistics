import kotlinx.serialization.json.JsonElement

@kotlinx.serialization.Serializable
data class LogResponse(
    val id: Long,
    val description: String,
    val executedQuery: String? = null,
    val undoQuery: String? = null,
    val executedBy: String,
    val executedAt: String? = null,
    val action: String? = null,
    val undone: Boolean = false,
    val undoneAt: String? = null,
    val payloadJson: String? = null
)

@kotlinx.serialization.Serializable
data class LogPayload(
    val action: String? = null,
    val entity: String? = null,
    val attributeChanges: List<AttributeChange>? = null
)

@kotlinx.serialization.Serializable
data class AttributeChange(
    val field: String,
    val before: JsonElement? = null,
    val after: JsonElement? = null
)
