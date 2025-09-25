package ru.coolsoft.alphatrainer.data

import ru.coolsoft.alphatrainer.nonwasm.data.fetchAllTrainableLanguages
import ru.coolsoft.alphatrainer.nonwasm.data.fetchAlphabetsForLanguage
import ru.coolsoft.alphatrainer.nonwasm.data.fetchDictionariesForLanguage
import ru.coolsoft.alphatrainer.nonwasm.data.fetchScriptAlphabetsForAlphabets
import ru.coolsoft.alphatrainer.nonwasm.data.fetchScriptAlphabetsForDictionaries
import ru.coolsoft.alphatrainer.nonwasm.data.fetchSymbolsForDictionary
import ru.coolsoft.alphatrainer.nonwasm.data.fetchSymbolsForLanguage
import ru.coolsoft.alphatrainer.shared.FlipcardRequest
import ru.coolsoft.alphatrainer.shared.ICategorizedLocalizedEntity
import ru.coolsoft.alphatrainer.shared.ILocalizedEntity
import ru.coolsoft.alphatrainer.shared.ISymbolPair

actual suspend fun getTrainableLanguages(): List<ILocalizedEntity> =
    fetchAllTrainableLanguages()

actual suspend fun getAvailableAlphabets(
    language: String
): List<ILocalizedEntity> = fetchAlphabetsForLanguage(language)

actual suspend fun getTrainableAlphabet(request: FlipcardRequest): List<ISymbolPair> =
    fetchSymbolsForLanguage(request)
actual suspend fun getTrainableDictionary(request: FlipcardRequest): List<ISymbolPair> =
    fetchSymbolsForDictionary(request)

actual suspend fun getScriptAlphabetsForAlphabets(
    alphabets: List<String>
): List<ICategorizedLocalizedEntity> = fetchScriptAlphabetsForAlphabets(alphabets)

actual suspend fun getDictionariesForLanguage(languageId: String): List<ICategorizedLocalizedEntity> =
    fetchDictionariesForLanguage(languageId)

actual suspend fun getScriptAlphabetsForDictionaries(dictionaries: List<String>): List<ICategorizedLocalizedEntity> =
    fetchScriptAlphabetsForDictionaries(dictionaries)
