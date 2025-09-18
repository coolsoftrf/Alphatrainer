package ru.coolsoft.alphatrainer.shared


enum class BitField(val bitMask: Int) {
    LanguageBitMask(1),
    AlphabetBitMask(2),
    DictionaryBitMask(4);

    operator fun invoke() = bitMask
}

interface IEntity {
    val id: String
    val name: String
}

interface ILocalizedEntity {
    val id: String
    val name: String
    val spellLangId: String?
    val spell: String?
    val isPrimary: Boolean?
}

data class EntityKey(
    val id: String,
    val name: String,
    val isPrimary: Boolean
) {
    constructor(localizedEntity: ILocalizedEntity) : this(
        localizedEntity.id,
        localizedEntity.name,
        localizedEntity.isPrimary != false
    )
}

interface ICategorizedLocalizedEntity : ILocalizedEntity {
    val category: String
}

interface ILanguageRepository {
    suspend fun getAllTrainableLanguages(): List<ILocalizedEntity>
    suspend fun getAlphabetsForLanguage(language: String): List<ILocalizedEntity>
    suspend fun getScriptAlphabetsForAlphabets(languages: List<String>): List<ICategorizedLocalizedEntity>

    suspend fun getDictionariesForLanguage(languageId: String): List<ILocalizedEntity>
    suspend fun getScriptAlphabetsForDictionaries(dictionaries: List<String>): List<ICategorizedLocalizedEntity>

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
    val category: String,
    val learntLanguage: String,
    val scriptLanguage: String,
    val limitPairs: Int
)

interface IAlphabetRepository {
    suspend fun getAllSymbolPairsForLanguage(request: FlipcardRequest): List<ISymbolPair>
    suspend fun insert(symbol: ISymbol, language: String, aux: Int)
}

interface IDictionaryRepository {
    suspend fun getAllWordPairsForDictionary(request: FlipcardRequest): List<ISymbolPair>
}