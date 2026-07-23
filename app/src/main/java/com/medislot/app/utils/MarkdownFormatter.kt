package com.medislot.app.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

object MarkdownFormatter {
    
    /**
     * Converts simple markdown formats like double asterisks (**text**) into Compose AnnotatedStrings
     */
    fun formatMarkdown(text: String): AnnotatedString {
        return buildAnnotatedString {
            var index = 0
            while (index < text.length) {
                val nextStart = text.indexOf("**", index)
                if (nextStart == -1) {
                    append(text.substring(index))
                    break
                }
                append(text.substring(index, nextStart))
                val nextEnd = text.indexOf("**", nextStart + 2)
                if (nextEnd == -1) {
                    append(text.substring(nextStart))
                    break
                }
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(text.substring(nextStart + 2, nextEnd))
                }
                index = nextEnd + 2
            }
        }
    }
}
