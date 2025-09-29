package ru.coolsoft.alphatrainer.nonwasm.data

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
