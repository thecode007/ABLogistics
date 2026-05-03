package org.safieddine.ablogistics.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Decodes a number that may arrive as a JSON number or a quoted string (e.g., "10.00").
 */
object DoubleAsStringOrNumberSerializer : KSerializer<Double> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("DoubleAsStringOrNumber", PrimitiveKind.DOUBLE)

    override fun serialize(encoder: Encoder, value: Double) {
        encoder.encodeDouble(value)
    }

    override fun deserialize(decoder: Decoder): Double {
        val jsonDecoder = decoder as? JsonDecoder ?: return decoder.decodeDouble()
        val element = jsonDecoder.decodeJsonElement()
        val primitive = element.jsonPrimitive
        // Try direct numeric first
        primitive.doubleOrNull?.let { return it }
        // Fallback to parsing from string content (remove grouping commas if any)
        val raw = primitive.content.trim().replace(",", "")
        return raw.toDoubleOrNull() ?: throw SerializationException("Expected a number or numeric string, got: $raw")
    }
}

