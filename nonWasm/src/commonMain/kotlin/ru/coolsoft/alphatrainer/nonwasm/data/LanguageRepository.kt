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

    override suspend fun getAllTrainableLanguages(): Flow<List<ILocalizedEntity>> =
        dao.getAllEntitiesByFlagMask(BitField.LanguageBitMask.bitMask)

    override suspend fun getAlphabetsForLanguage(
        language: String
    ): Flow<List<ILocalizedEntity>> =
        dao.getMatchingEntitiesByFlagMask(
            "$language%",
            BitField.AlphabetBitMask.bitMask
        )

    override suspend fun getScriptAlphabetsForAlphabets(
        languages: List<String>
    ): Flow<List<ICategorizedLocalizedEntity>> =
        dao.getEntitiesForAlphabetsOfLanguageId(languages)

    override suspend fun insert(entity: IEntity) =
        dao.insert(Entity(entity, BitField.AlphabetBitMask))
}

private val repository = Koin.di!!.koin.get<LanguageRepository>()

suspend fun fetchAllTrainableLanguages(): List<ILocalizedEntity> =
    repository.getAllTrainableLanguages().first()

suspend fun fetchAlphabetsForLanguage(
    language: String
): List<ILocalizedEntity> =
    repository.getAlphabetsForLanguage(language).first()

suspend fun fetchScriptAlphabetsForAlphabets(
    alphabets: List<String>
): List<ICategorizedLocalizedEntity> =
    repository.getScriptAlphabetsForAlphabets(alphabets).first()