package ru.coolsoft.alphatrainer.data

import ru.coolsoft.alphatrainer.nonwasm.data.fetchAllTrainableLanguages
import ru.coolsoft.alphatrainer.nonwasm.data.fetchAlphabetsForLanguage
import ru.coolsoft.alphatrainer.nonwasm.data.fetchScriptAlphabetsForAlphabets
import ru.coolsoft.alphatrainer.nonwasm.data.fetchSymbolsForLanguage
import ru.coolsoft.alphatrainer.shared.FlipcardRequest
import ru.coolsoft.alphatrainer.shared.ICategorizedLocalizedEntity
import ru.coolsoft.alphatrainer.shared.ILocalizedEntity
import ru.coolsoft.alphatrainer.shared.ISymbolPair

actual suspend fun getTrainableLanguages(uiLanguageId: String): List<ILocalizedEntity> =
    fetchAllTrainableLanguages(uiLanguageId)

actual suspend fun getAvailableAlphabets(
    language: String,
    uiLanguageId: String
): List<ILocalizedEntity> = fetchAlphabetsForLanguage(language, uiLanguageId)

actual suspend fun getTrainableAlphabet(request: FlipcardRequest): List<ISymbolPair> =
    fetchSymbolsForLanguage(request)

actual suspend fun getScriptAlphabetsForAlphabets(
    alphabets: List<String>,
    uiLanguageId: String
): List<ICategorizedLocalizedEntity> = fetchScriptAlphabetsForAlphabets(alphabets, uiLanguageId)