package ru.coolsoft.alphatrainer.data

import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import ru.coolsoft.alphatrainer.FALLBACK_LANGUAGE
import ru.coolsoft.alphatrainer.models.Flipcard
import ru.coolsoft.alphatrainer.shared.FlipcardRequest
import ru.coolsoft.alphatrainer.shared.ICategorizedLocalizedEntity
import ru.coolsoft.alphatrainer.shared.ILocalizedEntity
import ru.coolsoft.alphatrainer.shared.ISymbolPair


@Serializable
data class LocalizedString(
    val id: String,
    val name: String,
    val transcriptions: Map<String, String>
) {
    fun transcription(uiLanguageId: String) =
        transcriptions[uiLanguageId] ?: transcriptions[FALLBACK_LANGUAGE]
}

fun localizedString(entityLocalizations: Map.Entry<Pair<String, String>, List<ILocalizedEntity>>) =
    entityLocalizations.run {
        LocalizedString(
            key.first,
            key.second,
            value
                .filter { e -> e.spellLangId != null && e.spell != null }
                .associate { e -> e.spellLangId!! to e.spell!! }
        )
    }

// ------------------------------------------
// Flipcards
// ------------------------------------------
//ToDo: Move these mocks to tests
val mockFlipCards = listOf(
    Flipcard("yu", "yu"), Flipcard("yu", "ю"),
    Flipcard("ya", "ya"), Flipcard("ya", "я")
)

suspend fun mockFlipcardData(request: FlipcardRequest): List<Flipcard> {
    delay(1000)
    return mockFlipCards.shuffled()
}

expect suspend fun getTrainableAlphabet(request: FlipcardRequest): List<ISymbolPair>

suspend fun trainableFlipcardData(request: FlipcardRequest): List<Flipcard> {
    return getTrainableAlphabet(request).shuffled().map { pair ->
        listOf(
            Flipcard(pair.id, pair.name),
            Flipcard(pair.id, pair.run { spellName ?: id })
        )
    }.flatten().shuffled()
}

// ------------------------------------------
// Alphabets
// ------------------------------------------
//ToDo: Move these mocks to tests
val mockAlphabets = listOf(
    LocalizedString("jakana_hi", "ひらがな", mapOf(FALLBACK_LANGUAGE to "Hiragana")),
    LocalizedString("jakana_ka", "カタカナ", mapOf(FALLBACK_LANGUAGE to "Katakana")),
)

suspend fun mockAlphabetsData(languageId: String): List<LocalizedString> {
    delay(1000)
    return mockAlphabets
}

expect suspend fun getAvailableAlphabets(
    language: String
): List<ILocalizedEntity>

suspend fun availableAlphabets(language: String): List<LocalizedString> {
    return getAvailableAlphabets(language)
        .groupBy { it.id to it.name }
        .map { localizedString(it) }
}

// ------------------------------------------
// Languages
// ------------------------------------------
expect suspend fun getTrainableLanguages(): List<ILocalizedEntity>

suspend fun trainableLanguages(): List<LocalizedString> {
    return getTrainableLanguages()
        .groupBy { it.id to it.name }
        .map(::localizedString)
}

expect suspend fun getScriptAlphabetsForAlphabets(
    alphabets: List<String>
): List<ICategorizedLocalizedEntity>

suspend fun scriptAlphabetsForAlphabetList(
    alphabets: List<String>
): Map<String, List<LocalizedString>> {
    return getScriptAlphabetsForAlphabets(alphabets)
        .groupBy { it.category }
        .mapValues {
            it.value.groupBy { e -> e.id to e.name }
                .map(::localizedString)
        }
}
