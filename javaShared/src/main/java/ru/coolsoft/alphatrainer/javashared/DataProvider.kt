package ru.coolsoft.alphatrainer.javashared

import ru.coolsoft.alphatrainer.shared.FlipcardRequest
import ru.coolsoft.alphatrainer.shared.IAlphabetRepository
import ru.coolsoft.alphatrainer.shared.IRawDataProvider
import ru.coolsoft.alphatrainer.shared.IDictionaryRepository
import ru.coolsoft.alphatrainer.shared.ILanguageRepository


val DataProviderImpl = object : IRawDataProvider {
    private val languageRepository = Koin.di!!.koin.get<ILanguageRepository>()
    private val alphabetRepository = Koin.di!!.koin.get<IAlphabetRepository>()
    private val dictionaryRepository = Koin.di!!.koin.get<IDictionaryRepository>()

    override suspend fun getTrainableAlphabet(request: FlipcardRequest) =
        alphabetRepository.getAllSymbolPairsForLanguage(request)
            .shuffled()
            .take(request.limitPairs)

    override suspend fun getTrainableDictionary(request: FlipcardRequest) =
        dictionaryRepository.getAllWordPairsForDictionary(request)
            .shuffled()
            .take(request.limitPairs)

    override val getAvailableAlphabets = languageRepository::getAlphabetsForLanguage
    override val getTrainableLanguages = languageRepository::getAllTrainableLanguages
    override val getScriptAlphabetsForAlphabets = languageRepository::getScriptAlphabetsForAlphabets
    override val getDictionariesForLanguage = languageRepository::getDictionariesForLanguage
    override val getScriptAlphabetsForDictionaries =
        languageRepository::getScriptAlphabetsForDictionaries
}
