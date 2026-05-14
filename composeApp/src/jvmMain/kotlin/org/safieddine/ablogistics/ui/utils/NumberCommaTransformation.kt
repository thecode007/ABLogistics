package org.safieddine.ablogistics.ui.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class NumberCommaTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val parts = originalText.split(".")
        val integerPart = parts[0]
        val fractionalPart = if (parts.size > 1) "." + parts[1] else ""

        val formattedInt = buildString {
            var count = 0
            for (i in integerPart.lastIndex downTo 0) {
                if (count > 0 && count % 3 == 0 && integerPart[i] != '-') {
                    append(',')
                }
                append(integerPart[i])
                if (integerPart[i].isDigit()) {
                    count++
                }
            }
        }.reversed()

        val formattedText = formattedInt + fractionalPart

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var originalCount = 0
                var transformedCount = 0
                for (char in formattedText) {
                    if (originalCount == offset) break
                    if (char != ',') {
                        originalCount++
                    }
                    transformedCount++
                }
                return transformedCount
            }

            override fun transformedToOriginal(offset: Int): Int {
                var originalCount = 0
                var transformedCount = 0
                for (char in formattedText) {
                    if (transformedCount == offset) break
                    if (char != ',') {
                        originalCount++
                    }
                    transformedCount++
                }
                return originalCount
            }
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}
