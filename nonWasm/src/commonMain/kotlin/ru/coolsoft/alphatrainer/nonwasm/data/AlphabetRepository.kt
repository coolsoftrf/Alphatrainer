package ru.coolsoft.alphatrainer.nonwasm.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import ru.coolsoft.alphatrainer.nonwasm.Koin
import ru.coolsoft.alphatrainer.shared.FlipcardRequest
import ru.coolsoft.alphatrainer.shared.IAlphabetRepository
import ru.coolsoft.alphatrainer.shared.ISymbol
import ru.coolsoft.alphatrainer.shared.ISymbolPair

internal class AlphabetRepository(db: AppDatabase) : IAlphabetRepository {
    private val dao = db.getAlphabetDao()

    override suspend fun getAllSymbolPairsForLanguage(request: FlipcardRequest): Flow<List<ISymbolPair>> {
        val (language, learntLanguage, scriptLanguage) = request
        return dao.getAllSymbolPairs(language, learntLanguage, scriptLanguage)
    }

    override suspend fun insert(
        symbol: ISymbol,
        language: String,
        aux: Int
    ) = dao.insert(SymbolEntity(symbol, language, aux))
}

private val repository = Koin.di!!.koin.get<AlphabetRepository>()

suspend fun fetchSymbolsForLanguage(request: FlipcardRequest): List<ISymbolPair> =
    repository.getAllSymbolPairsForLanguage(request).first()
        .shuffled()
        .take(request.limitPairs)
