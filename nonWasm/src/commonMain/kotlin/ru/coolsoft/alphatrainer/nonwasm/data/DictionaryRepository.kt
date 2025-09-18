package ru.coolsoft.alphatrainer.nonwasm.data

import ru.coolsoft.alphatrainer.nonwasm.Koin
import ru.coolsoft.alphatrainer.shared.FlipcardRequest
import ru.coolsoft.alphatrainer.shared.IDictionaryRepository
import ru.coolsoft.alphatrainer.shared.ISymbolPair

internal class DictionaryRepository(db: AppDatabase) : IDictionaryRepository {
    private val dao = db.getDictionaryDao()

    override suspend fun getAllWordPairsForDictionary(request: FlipcardRequest): List<ISymbolPair> {
        val (dictionary, learntLanguage, scriptLanguage) = request
        return dao.getAllWordPairs(dictionary, learntLanguage, scriptLanguage)
    }
}

private val repository = Koin.di!!.koin.get<DictionaryRepository>()

suspend fun fetchSymbolsForDictionary(request: FlipcardRequest): List<ISymbolPair> =
    repository.getAllWordPairsForDictionary(request)
        .shuffled()
        .take(request.limitPairs)