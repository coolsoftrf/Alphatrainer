package ru.coolsoft.alphatrainer.data

import ru.coolsoft.alphatrainer.shared.FlipcardRequest
import ru.coolsoft.alphatrainer.shared.ICategorizedLocalizedEntity
import ru.coolsoft.alphatrainer.shared.ILocalizedEntity
import ru.coolsoft.alphatrainer.shared.ISymbolPair


actual suspend fun getTrainableLanguages(uiLanguageId: String): List<ILocalizedEntity> {
    TODO("Not yet implemented")
}

actual suspend fun getAvailableAlphabets(
    language: String,
    uiLanguageId: String
): List<ILocalizedEntity> {
    TODO("Not yet implemented")
}

actual suspend fun getTrainableAlphabet(request: FlipcardRequest): List<ISymbolPair> {
    TODO("Not yet implemented")
}

actual suspend fun getScriptAlphabetsForAlphabets(
    alphabets: List<String>,
    uiLanguageId: String
): List<ICategorizedLocalizedEntity> {
    TODO("Not yet implemented")
}