package ru.coolsoft.alphatrainer

import kotlin.text.lastIndexOf

const val FALLBACK_ID = ""

private const val HIERARCHY_DELIMITER = '_'
fun parentLanguageFor(compoundLanguageKey: String) =
    compoundLanguageKey.substringBeforeLast(HIERARCHY_DELIMITER)

fun parentOrFallback(compoundKey: String) = with(compoundKey) {
    when (val index = lastIndexOf(HIERARCHY_DELIMITER)) {
        -1 -> FALLBACK_ID
        else -> substring(0, index)
    }
}

fun String.hierarchyLevel() = count { it == HIERARCHY_DELIMITER }