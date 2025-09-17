package ru.coolsoft.alphatrainer.nonwasm.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.coolsoft.alphatrainer.shared.IEntity
import ru.coolsoft.alphatrainer.shared.ILocalizedEntity
import ru.coolsoft.alphatrainer.shared.ICategorizedLocalizedEntity

enum class BitField(val bitMask: Int) {
    LanguageBitMask(1),
    AlphabetBitMask(2),
    //TrainingLevelBitMask(4)
}

@androidx.room.Entity("Entities")
data class Entity(
    @PrimaryKey @ColumnInfo(name = "_id") override val id: String,
    @ColumnInfo(name = "Name") override val name: String,
    @ColumnInfo(name = "Flags") val flags: Int
) : IEntity {
    constructor(entity: IEntity, flags: BitField) : this(entity.id, entity.name, flags.bitMask)
}

@androidx.room.Entity("Spells", primaryKeys = ["_id", "SpellLangId", "LangId"])
data class SpellEntity(
    @ColumnInfo(name = "_id") val id: String,
    @ColumnInfo(name = "Spell") val spell: String,
    @ColumnInfo(name = "SpellLangId") val spellLangId: String,
    @ColumnInfo(name = "LangId") val langId: String
)

data class LocalizedEntity(
    @ColumnInfo(name = "_id") override val id: String,
    @ColumnInfo(name = "Name") override val name: String,
    @ColumnInfo(name = "Flags") val flags: Int,
    @ColumnInfo(name = "SpellLangId") override val spellLangId: String?,
    @ColumnInfo(name = "Spell") override val spell: String?
) : ILocalizedEntity

data class CategorizedLocalizedEntity(
    override val category: String,
    @ColumnInfo(name = "_id") override val id: String,
    @ColumnInfo(name = "Name") override val name: String,
    @ColumnInfo(name = "Flags") val flags: Int,
    @ColumnInfo(name = "SpellLangId") override val spellLangId: String?,
    @ColumnInfo(name = "Spell") override val spell: String?
) : ICategorizedLocalizedEntity

const val LOCALIZED_ENTITY_FIELDS = """
   e.*,
   s.SpellLangId,
   s.Spell
"""
const val JOIN_SPELL_TABLES = """
   LEFT JOIN
   Spells s ON e._id = s.LangId AND
          s._id = ""
"""
const val WHERE_BITMASK = " WHERE (flags & :bitMask) > 0"
const val QUERY_LOCALIZED_ENTITY =
    "SELECT $LOCALIZED_ENTITY_FIELDS FROM Entities e $JOIN_SPELL_TABLES$WHERE_BITMASK"

const val QUERY_SCRIPT_LANGUAGES = """
    SELECT DISTINCT ScriptLangId, LangId
      FROM Alphabets
     WHERE LangId IN (:languageIds) AND
           LangId <> ScriptLangId
    UNION
    SELECT "", ""
"""

@Dao
interface EntitiesDao {
    @Query(QUERY_LOCALIZED_ENTITY)
    fun getAllEntitiesByFlagMask(bitMask: Int): Flow<List<LocalizedEntity>>

    @Query("$QUERY_LOCALIZED_ENTITY AND e._id LIKE :idPattern")
    fun getMatchingEntitiesByFlagMask(
        idPattern: String,
        bitMask: Int
    ): Flow<List<LocalizedEntity>>

    @Query(
        """
        WITH als (id, category) AS ($QUERY_SCRIPT_LANGUAGES)
        SELECT als.category, $LOCALIZED_ENTITY_FIELDS
        FROM als
        INNER JOIN
        Entities e ON e._id = als.id
        $JOIN_SPELL_TABLES
        """
    )
    fun getEntitiesForAlphabetsOfLanguageId(
        languageIds: List<String>
    ): Flow<List<CategorizedLocalizedEntity>>

    @Insert
    suspend fun insert(entity: Entity)
}