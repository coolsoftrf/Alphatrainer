package ru.coolsoft.alphatrainer.nonwasm.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import ru.coolsoft.alphatrainer.nonwasm.Koin
import ru.coolsoft.alphatrainer.shared.ICategorizedLocalizedEntity
import ru.coolsoft.alphatrainer.shared.IEntity
import ru.coolsoft.alphatrainer.shared.ILanguageRepository
import ru.coolsoft.alphatrainer.shared.ILocalizedEntity

internal class LanguageRepository(db: AppDatabase) : ILanguageRepository {
    private val dao = db.getEntitiesDao()

    override suspend fun getAllTrainableLanguages(spellLanguageId: String): Flow<List<ILocalizedEntity>> =
        dao.getAllEntitiesByFlagMask(spellLanguageId, BitField.ApplicableForTrainingBitMask.bitMask)

    override suspend fun getAlphabetsForLanguage(
        language: String,
        spellLanguageId: String
    ): Flow<List<ILocalizedEntity>> =
        dao.getMatchingEntitiesByFlagMask(
            "$language%",
            spellLanguageId,
            BitField.ApplicableForAlphabetsBitMask.bitMask
        )

    override suspend fun getScriptAlphabetsForAlphabets(
        languages: List<String>,
        spellLanguageId: String
    ): Flow<List<ICategorizedLocalizedEntity>> =
        dao.getEntitiesForAlphabetsOfLanguageId(languages, spellLanguageId)

    override suspend fun insert(entity: IEntity) =
        dao.insert(Entity(entity, BitField.ApplicableForAlphabetsBitMask))
}

private val repository = Koin.di!!.koin.get<LanguageRepository>()

suspend fun fetchAllTrainableLanguages(spellLanguageId: String): List<ILocalizedEntity> =
    repository.getAllTrainableLanguages(spellLanguageId).first()

suspend fun fetchAlphabetsForLanguage(
    language: String,
    spellLanguageId: String
): List<ILocalizedEntity> =
    repository.getAlphabetsForLanguage(language, spellLanguageId).first()

suspend fun fetchScriptAlphabetsForAlphabets(
    alphabets: List<String>,
    spellLanguageId: String
): List<ICategorizedLocalizedEntity> =
    repository.getScriptAlphabetsForAlphabets(alphabets, spellLanguageId).first()