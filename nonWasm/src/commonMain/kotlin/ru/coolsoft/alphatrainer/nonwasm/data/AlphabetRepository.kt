package ru.coolsoft.alphatrainer.nonwasm.data

import ru.coolsoft.alphatrainer.shared.FlipcardRequest
import ru.coolsoft.alphatrainer.shared.IAlphabetRepository
import ru.coolsoft.alphatrainer.shared.ISymbol
import ru.coolsoft.alphatrainer.shared.ISymbolPair


internal class AlphabetRepository(db: AppDatabase) : IAlphabetRepository {
    private val dao = db.getAlphabetDao()

    override suspend fun getAllSymbolPairsForLanguage(request: FlipcardRequest): List<ISymbolPair> {
        val (language, learntLanguage, scriptLanguage) = request
        return dao.getAllSymbolPairs(language, learntLanguage, scriptLanguage)
    }

    override suspend fun insert(
        symbol: ISymbol,
        language: String,
        aux: Int
    ) = dao.insert(SymbolEntity(symbol, language, aux))
}
