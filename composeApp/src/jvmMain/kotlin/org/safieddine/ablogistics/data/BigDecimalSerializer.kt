package org.safieddine.ablogistics.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonPrimitive
import java.math.BigDecimal
import java.math.RoundingMode

object BigDecimalAsStringSerializer : KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BigDecimalAsString", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.setScale(4, RoundingMode.HALF_UP).toPlainString())
    }

    override fun deserialize(decoder: Decoder): BigDecimal {
        return when (decoder) {
            is JsonDecoder -> {
                val element = decoder.decodeJsonElement()
                val primitive = element.jsonPrimitive
                val content = primitive.content.trim().replace(",", "")
                try {
                    BigDecimal(content).setScale(4, RoundingMode.HALF_UP)
                } catch (e: Exception) {
                    throw SerializationException("Expected a numeric string for BigDecimal, got: $content")
                }
            }
            else -> {
                BigDecimal(decoder.decodeString()).setScale(4, RoundingMode.HALF_UP)
            }
        }
    }
}
