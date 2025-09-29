package ru.coolsoft.alphatrainer.data

import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import ru.coolsoft.alphatrainer.FALLBACK_ID
import ru.coolsoft.alphatrainer.models.Flipcard
import ru.coolsoft.alphatrainer.shared.EntityKey
import ru.coolsoft.alphatrainer.shared.FlipcardRequest
import ru.coolsoft.alphatrainer.shared.ICategorizedLocalizedEntity
import ru.coolsoft.alphatrainer.shared.IRawDataProvider
import ru.coolsoft.alphatrainer.shared.ILocalizedEntity
import ru.coolsoft.alphatrainer.shared.ISymbolPair

@Serializable
data class LocalizedString(
    val id: String,
    val name: String,
    val isPrimary: Boolean,
    val transcriptions: Map<String, String>
) {
    fun name(languageId: String) =
        transcriptions[languageId] ?: name

    fun transcription(languageId: String) =
        transcriptions[languageId] ?: transcriptions[FALLBACK_ID]
}

private fun localizedString(entityLocalizations: Map.Entry<EntityKey, List<ILocalizedEntity>>) =
    with(entityLocalizations) {
        LocalizedString(
            key.id, key.name, key.isPrimary,
            value
                .filter { e -> e.spellLangId != null && e.spell != null }
                .associate { e -> e.spellLangId!! to e.spell!! }
        )
    }

private fun List<ILocalizedEntity>.toLocalizations() =
    groupBy { EntityKey(it) }
        .map(::localizedString)

private fun List<ICategorizedLocalizedEntity>.toCategorizedLocalizations() =
    groupBy { it.category }
        .mapValues { it.value.toLocalizations() }

expect val RawDataProvider: IRawDataProvider

object DataProvider {
    private fun List<ISymbolPair>.toPairs(fallback: Boolean) =
        (if (fallback) this else filter { it.spellName != null })
            .map {
                with(it) {
                    Flipcard(id, name) to Flipcard(id, spellName ?: id)
                }
            }

    private fun List<Pair<Flipcard, Flipcard>>.flatten() =
        flatMap { listOf(it.first, it.second) }

    /** ------------------------------------------
     * Flipcards
     * ------------------------------------------ */
    suspend fun alphabetFlipcardData(request: FlipcardRequest): List<Flipcard> =
        RawDataProvider.getTrainableAlphabet(request).toPairs(request.scriptLanguage == FALLBACK_ID)
            .flatten()
            .shuffled()

    suspend fun dictionaryFlipcardData(request: FlipcardRequest): List<Flipcard> =
        RawDataProvider.getTrainableDictionary(request).toPairs(request.scriptLanguage == FALLBACK_ID)
            .unzip()
            .run { first.shuffled() zip second.shuffled() }
            .flatten()

    /** ------------------------------------------
     * Alphabets
     * ------------------------------------------*/
    suspend fun availableAlphabets(language: String): List<LocalizedString> =
        RawDataProvider.getAvailableAlphabets(language).toLocalizations()

    /** ------------------------------------------
     * Languages
     * ------------------------------------------*/
    suspend fun trainableLanguages(): List<LocalizedString> {
        return RawDataProvider.getTrainableLanguages().toLocalizations()
    }

    suspend fun scriptAlphabetsForAlphabetList(
        alphabets: List<String>
    ): Map<String, List<LocalizedString>> =
        RawDataProvider.getScriptAlphabetsForAlphabets(alphabets).toCategorizedLocalizations()

    /** ------------------------------------------
     * Dictionaries
     * ------------------------------------------*/
    suspend fun dictionariesForLanguage(
        languageId: String
    ): Map<String, List<LocalizedString>> =
        RawDataProvider.getDictionariesForLanguage(languageId).toCategorizedLocalizations()

    suspend fun scriptAlphabetsForDictionaries(
        dictionaries: List<String>
    ): Map<String, List<LocalizedString>> =
        RawDataProvider.getScriptAlphabetsForDictionaries(dictionaries).toCategorizedLocalizations()
}

//ToDo: Move these mocks to tests

// ------------------------------------------
// Flipcards
// ------------------------------------------
val mockFlipCards = listOf(
    Flipcard("yu", "yu"), Flipcard("yu", "ю"),
    Flipcard("ya", "ya"), Flipcard("ya", "я")
)

suspend fun mockFlipcardData(request: FlipcardRequest): List<Flipcard> {
    delay(1000)
    return mockFlipCards.shuffled()
}

// ------------------------------------------
// Alphabets
// ------------------------------------------
val mockAlphabets = listOf(
    LocalizedString("jakana_hi", "ひらがな", true, mapOf(FALLBACK_ID to "Hiragana")),
    LocalizedString("jakana_ka", "カタカナ", true, mapOf(FALLBACK_ID to "Katakana")),
)

suspend fun mockAlphabetsData(languageId: String): List<LocalizedString> {
    delay(1000)
    return mockAlphabets
}