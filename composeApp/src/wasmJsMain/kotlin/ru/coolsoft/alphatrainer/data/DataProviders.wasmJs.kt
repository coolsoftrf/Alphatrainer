package ru.coolsoft.alphatrainer.data

import ru.coolsoft.alphatrainer.shared.FlipcardRequest
import ru.coolsoft.alphatrainer.shared.ICategorizedLocalizedEntity
import ru.coolsoft.alphatrainer.shared.ILocalizedEntity
import ru.coolsoft.alphatrainer.shared.ISymbolPair


actual suspend fun getTrainableLanguages(): List<ILocalizedEntity> {
    TODO("Not yet implemented")
}

actual suspend fun getAvailableAlphabets(
    language: String
): List<ILocalizedEntity> {
    TODO("Not yet implemented")
}

actual suspend fun getTrainableAlphabet(request: FlipcardRequest): List<ISymbolPair> {
    TODO("Not yet implemented")
}
actual suspend fun getTrainableDictionary(request: FlipcardRequest): List<ISymbolPair> {
    TODO("Not yet implemented")
}

actual suspend fun getScriptAlphabetsForAlphabets(
    alphabets: List<String>
): List<ICategorizedLocalizedEntity> {
    TODO("Not yet implemented")
}

actual suspend fun getDictionariesForLanguage(languageId: String): List<ICategorizedLocalizedEntity> {
    TODO("Not yet implemented")
}

actual suspend fun getScriptAlphabetsForDictionaries(dictionaries: List<String>): List<ICategorizedLocalizedEntity> {
    TODO("Not yet implemented")
}