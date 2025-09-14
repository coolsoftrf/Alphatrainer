package ru.coolsoft.alphatrainer.data

import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import ru.coolsoft.alphatrainer.models.Flipcard
import ru.coolsoft.alphatrainer.shared.FlipcardRequest
import ru.coolsoft.alphatrainer.shared.ICategorizedLocalizedEntity
import ru.coolsoft.alphatrainer.shared.ILocalizedEntity
import ru.coolsoft.alphatrainer.shared.ISymbolPair

@Serializable
data class LocalizedString(val id: String, val name: String, val transcription: String?)

fun localizedString(entity: ILocalizedEntity) = LocalizedString(
    entity.id,
    entity.name,
    entity.run { spell ?: fallbackSpell}
)

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
    LocalizedString("jakana_hi", "ひらがな", "Hiragana"),
    LocalizedString("jakana_ka", "カタカナ", "Katakana"),
)

suspend fun mockAlphabetsData(languageId: String, uiLanguageId: String): List<LocalizedString> {
    delay(1000)
    return mockAlphabets
}

expect suspend fun getAvailableAlphabets(
    language: String,
    uiLanguageId: String
): List<ILocalizedEntity>

suspend fun availableAlphabets(language: String, uiLanguageId: String): List<LocalizedString> {
    return getAvailableAlphabets(language, uiLanguageId).map(::localizedString)
}

// ------------------------------------------
// Languages
// ------------------------------------------
expect suspend fun getTrainableLanguages(uiLanguageId: String): List<ILocalizedEntity>

suspend fun trainableLanguages(uiLanguageId: String): List<LocalizedString> {
    return getTrainableLanguages(uiLanguageId).map(::localizedString)
}

expect suspend fun getScriptAlphabetsForAlphabets(
    alphabets: List<String>,
    uiLanguageId: String
): List<ICategorizedLocalizedEntity>

suspend fun scriptAlphabetsForAlphabetList(
    alphabets: List<String>,
    uiLanguageId: String
): Map<String, List<LocalizedString>> {
    return getScriptAlphabetsForAlphabets(alphabets, uiLanguageId)
        .groupBy({ it.category }, ::localizedString)
}
