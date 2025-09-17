package ru.coolsoft.alphatrainer.shared

import kotlinx.coroutines.flow.Flow

interface IEntity {
    val id: String
    val name: String
}

interface ILocalizedEntity {
    val id: String
    val name: String
    val spellLangId: String?
    val spell: String?
}

interface ICategorizedLocalizedEntity: ILocalizedEntity {
    val category:String
}
/*
fun entity(id: String, name: String): IEntity {
    return object : IEntity {
        override val id: String
            get() = id
        override val name: String
            get() = name
    }
}
*/

interface ILanguageRepository {
    suspend fun getAllTrainableLanguages(): Flow<List<ILocalizedEntity>>
    suspend fun getAlphabetsForLanguage(language: String): Flow<List<ILocalizedEntity>>
    suspend fun getScriptAlphabetsForAlphabets(languages: List<String>): Flow<List<ICategorizedLocalizedEntity>>
    suspend fun insert(entity: IEntity)
}

interface ISymbol {
    val id: String
    val transcriptionLanguage: String
    val name: String
}
interface ISymbolPair {
    val id: String
    val name: String
    val spellName: String?
}

data class FlipcardRequest(
    val language: String,
    val learntLanguage: String,
    val scriptLanguage: String,
    val limitPairs: Int
)

interface IAlphabetRepository {
    suspend fun getAllSymbolPairsForLanguage(request: FlipcardRequest): Flow<List<ISymbolPair>>
    suspend fun insert(symbol: ISymbol, language: String, aux: Int)
}