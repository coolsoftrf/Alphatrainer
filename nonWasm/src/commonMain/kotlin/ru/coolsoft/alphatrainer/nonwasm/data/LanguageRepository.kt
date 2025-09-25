package ru.coolsoft.alphatrainer.nonwasm.data

import ru.coolsoft.alphatrainer.nonwasm.Koin
import ru.coolsoft.alphatrainer.shared.BitField
import ru.coolsoft.alphatrainer.shared.ICategorizedLocalizedEntity
import ru.coolsoft.alphatrainer.shared.IEntity
import ru.coolsoft.alphatrainer.shared.ILanguageRepository
import ru.coolsoft.alphatrainer.shared.ILocalizedEntity

internal class LanguageRepository(db: AppDatabase) : ILanguageRepository {
    private val dao = db.getEntitiesDao()

    override suspend fun getAllTrainableLanguages(): List<ILocalizedEntity> =
        dao.getAllEntitiesByFlagMask(BitField.LanguageBitMask())

    override suspend fun getAlphabetsForLanguage(
        language: String
    ): List<ILocalizedEntity> =
        dao.getMatchingEntitiesByFlagMask(
            language,
            BitField.AlphabetBitMask()
        )

    override suspend fun getScriptAlphabetsForAlphabets(
        languages: List<String>
    ): List<ICategorizedLocalizedEntity> =
        dao.getEntitiesForAlphabetsOfLanguageId(languages)

    override suspend fun getDictionariesForLanguage(languageId: String): List<ICategorizedLocalizedEntity> =
        dao.getEntitiesWithMatchingDictionariesByFlagMask(
            languageId,
            BitField.DictionaryBitMask()
        )

    override suspend fun getScriptAlphabetsForDictionaries(dictionaries: List<String>): List<ICategorizedLocalizedEntity> =
        dao.getScriptAlphabetsForDictionaries(dictionaries)

    override suspend fun insert(entity: IEntity) =
        dao.insert(BaseEntity(entity, BitField.AlphabetBitMask))
}

private val repository = Koin.di!!.koin.get<LanguageRepository>()

suspend fun fetchAllTrainableLanguages(): List<ILocalizedEntity> =
    repository.getAllTrainableLanguages()

suspend fun fetchAlphabetsForLanguage(language: String): List<ILocalizedEntity> =
    repository.getAlphabetsForLanguage(language)

suspend fun fetchScriptAlphabetsForAlphabets(alphabets: List<String>): List<ICategorizedLocalizedEntity> =
    repository.getScriptAlphabetsForAlphabets(alphabets)

suspend fun fetchDictionariesForLanguage(languageId: String): List<ICategorizedLocalizedEntity> =
    repository.getDictionariesForLanguage(languageId)

suspend fun fetchScriptAlphabetsForDictionaries(dictionaries: List<String>): List<ICategorizedLocalizedEntity> =
    repository.getScriptAlphabetsForDictionaries(dictionaries)
