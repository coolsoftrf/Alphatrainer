package ru.coolsoft.alphatrainer.data

import ru.coolsoft.alphatrainer.shared.FlipcardRequest
import ru.coolsoft.alphatrainer.shared.IRawDataProvider


actual val RawDataProvider = object : IRawDataProvider {
    override suspend fun getTrainableAlphabet(request: FlipcardRequest) = TODO("Not yet implemented")
    override suspend fun getTrainableDictionary(request: FlipcardRequest) = TODO("Not yet implemented")
    override val getAvailableAlphabets = TODO("Not yet implemented")
    override val getTrainableLanguages = TODO("Not yet implemented")
    override val getScriptAlphabetsForAlphabets = TODO("Not yet implemented")
    override val getDictionariesForLanguage = TODO("Not yet implemented")
    override val getScriptAlphabetsForDictionaries = TODO("Not yet implemented")
}
