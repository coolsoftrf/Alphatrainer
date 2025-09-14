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
    ApplicableForTrainingBitMask(1),
    ApplicableForAlphabetsBitMask(2),
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
    @ColumnInfo(name = "Spell") override val spell: String?,
    @ColumnInfo(name = "FallbackSpell") override val fallbackSpell: String?
) : ILocalizedEntity

data class CategorizedLocalizedEntity(
    override val category: String,
    @ColumnInfo(name = "_id") override val id: String,
    @ColumnInfo(name = "Name") override val name: String,
    @ColumnInfo(name = "Flags") val flags: Int,
    @ColumnInfo(name = "Spell") override val spell: String?,
    @ColumnInfo(name = "FallbackSpell") override val fallbackSpell: String?
) : ICategorizedLocalizedEntity

const val LOCALIZED_ENTITY_FIELDS = """
   e.*,
   s1.Spell,
   s2.Spell AS FallbackSpell
"""
const val JOIN_SPELL_TABLES = """
   LEFT JOIN
   spells s1 ON e._id = s1.LangId AND
                s1.SpellLangId = :spellLanguageId AND
                s1._id = ""
   LEFT JOIN
   spells s2 ON e._id = s2.LangId AND
                s2.SpellLangId = "" AND
                s2._id = ""
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
    fun getAllEntitiesByFlagMask(spellLanguageId: String, bitMask: Int): Flow<List<LocalizedEntity>>

    @Query("$QUERY_LOCALIZED_ENTITY AND e._id LIKE :idPattern")
    fun getMatchingEntitiesByFlagMask(
        idPattern: String,
        spellLanguageId: String,
        bitMask: Int
    ): Flow<List<LocalizedEntity>>

    @Query(
        """
        WITH als (id, category) AS ($QUERY_SCRIPT_LANGUAGES)
        SELECT a.category, $LOCALIZED_ENTITY_FIELDS
        FROM als a 
        INNER JOIN
        Entities e ON e._id = a.id
        $JOIN_SPELL_TABLES
        """
    )
    fun getEntitiesForAlphabetsOfLanguageId(
        languageIds: List<String>,
        spellLanguageId: String
    ): Flow<List<CategorizedLocalizedEntity>>

    @Insert
    suspend fun insert(entity: Entity)
}