package ru.coolsoft.alphatrainer

const val FALLBACK_LANGUAGE = ""

private const val HIERARCHY_DELIMITER = "_"
fun parentLanguageFor(compoundLanguageKey: String) =
    compoundLanguageKey.substringBeforeLast(HIERARCHY_DELIMITER)