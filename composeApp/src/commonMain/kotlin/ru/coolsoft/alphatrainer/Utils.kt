package ru.coolsoft.alphatrainer

const val FALLBACK_LANGUAGE = ""

private const val HIERARCHY_DELIMITER = "_"
fun baseLanguageFor(compoundLanguageKey: String) =
    compoundLanguageKey.substringBefore(HIERARCHY_DELIMITER)