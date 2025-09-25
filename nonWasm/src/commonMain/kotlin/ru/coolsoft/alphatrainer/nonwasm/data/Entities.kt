package ru.coolsoft.alphatrainer.nonwasm.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import ru.coolsoft.alphatrainer.shared.BitField
import ru.coolsoft.alphatrainer.shared.ICategorizedLocalizedEntity
import ru.coolsoft.alphatrainer.shared.IEntity
import ru.coolsoft.alphatrainer.shared.ILocalizedEntity

@Entity("Entities")
data class BaseEntity(
    @PrimaryKey @ColumnInfo(name = "_id") override val id: String,
    @ColumnInfo(name = "Name") override val name: String,
    @ColumnInfo(name = "Flags") val flags: Int
) : IEntity {
    constructor(entity: IEntity, flags: BitField) : this(entity.id, entity.name, flags())
}

@Entity(
    "Spells",
    primaryKeys = ["_id", "SpellLangId", "LangId"],
    foreignKeys = [
        ForeignKey(BaseEntity::class, ["_id"], ["SpellLangId"], onUpdate = CASCADE),
        ForeignKey(BaseEntity::class, ["_id"], ["LangId"], onUpdate = CASCADE)
    ]
)
data class SpellEntity(
    @ColumnInfo(name = "_id") val id: String,
    @ColumnInfo(name = "Spell") val spell: String,
    @ColumnInfo(name = "SpellLangId") val spellLangId: String,
    @ColumnInfo(name = "LangId") val langId: String
)

data class LocalizedEntity(
    @ColumnInfo(name = "_id") override val id: String,
    @ColumnInfo(name = "Name") override val name: String,
    @ColumnInfo(name = "SpellLangId") override val spellLangId: String?,
    @ColumnInfo(name = "Spell") override val spell: String?,
    @ColumnInfo(name = "Primary") override val isPrimary: Boolean?
) : ILocalizedEntity

data class CategorizedLocalizedEntity(
    override val category: String,
    @ColumnInfo(name = "_id") override val id: String,
    @ColumnInfo(name = "Name") override val name: String,
    @ColumnInfo(name = "SpellLangId") override val spellLangId: String?,
    @ColumnInfo(name = "Spell") override val spell: String?,
    @ColumnInfo(name = "Primary") override val isPrimary: Boolean?
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
const val QUERY_LOCALIZED_ENTITY =
    "SELECT $LOCALIZED_ENTITY_FIELDS FROM Entities e $JOIN_SPELL_TABLES"
const val WHERE_BITMASK = " WHERE (e.Flags & :bitMask) > 0"
const val QUERY_LOCALIZED_ENTITY_BY_BITMASK = "$QUERY_LOCALIZED_ENTITY$WHERE_BITMASK"

const val QUERY_SCRIPT_LANGUAGES = """
    SELECT DISTINCT ScriptLangId, LangId
      FROM Alphabets
     WHERE LangId IN (:languageIds) AND
           LangId <> ScriptLangId
    UNION
    SELECT "", ""
"""

const val QUERY_DICT_SECTIONS = """
    SELECT DISTINCT LevelId,
                    RTRIM(RTRIM(LevelId, replace(LevelId, '_', '') ), '_')
      FROM Dict
     WHERE LangId LIKE :langIdBase || '%' 
"""
const val QUERY_ENABLED_DS_ENTITIES = """
    SELECT DISTINCT ds._id, e.Name, ds.parentId, pe.Name
      FROM ds
           JOIN Entities e USING (_id)
           JOIN Entities pe ON pe._id = parentId
     $WHERE_BITMASK
"""
const val QUERY_DS_ENTITIES_UNION = """
    SELECT _id, Name, true
      FROM enabled
    UNION
    SELECT parentId, parentName, false
      FROM enabled
"""

const val QUERY_DICT_LEVELS = """
    SELECT DISTINCT LangId, LevelId
      FROM Dict
     WHERE LevelId IN (:dictIds)
"""

@Dao
interface EntitiesDao {
    @Query(QUERY_LOCALIZED_ENTITY_BY_BITMASK)
    suspend fun getAllEntitiesByFlagMask(bitMask: Int): List<LocalizedEntity>

    @Query("$QUERY_LOCALIZED_ENTITY_BY_BITMASK AND e._id LIKE :langIdBase || '%'")
    suspend fun getMatchingEntitiesByFlagMask(
        langIdBase: String,
        bitMask: Int
    ): List<LocalizedEntity>

    @Query("""
        WITH                        ds(_id, parentId) AS ($QUERY_DICT_SECTIONS),
             enabled(_id, name, parentId, parentName) AS ($QUERY_ENABLED_DS_ENTITIES),
                united (_id, Name, "Primary") AS ($QUERY_DS_ENTITIES_UNION)

        SELECT e._id as category, $LOCALIZED_ENTITY_FIELDS
          FROM united e
               $JOIN_SPELL_TABLES
        """)
    suspend fun getEntitiesWithMatchingDictionariesByFlagMask(
        langIdBase: String,
        bitMask: Int
    ): List<CategorizedLocalizedEntity>

    @Query(
        """
        WITH als (id, category) AS ($QUERY_SCRIPT_LANGUAGES)
        SELECT als.category, $LOCALIZED_ENTITY_FIELDS
          FROM als
               JOIN
               Entities e ON e._id = als.id
               $JOIN_SPELL_TABLES
        """
    )
    suspend fun getEntitiesForAlphabetsOfLanguageId(
        languageIds: List<String>
    ): List<CategorizedLocalizedEntity>

    @Query(
        """
        WITH ds (lang, category) AS ($QUERY_DICT_LEVELS)
        SELECT ds.category, $LOCALIZED_ENTITY_FIELDS, (e._id IN (ds.lang, "")) AS "Primary"
          FROM ds
               JOIN
               Entities e ON ds.lang like e._id || '%'
               $JOIN_SPELL_TABLES
        """
    )
    suspend fun getScriptAlphabetsForDictionaries(
        dictIds: List<String>
    ): List<CategorizedLocalizedEntity>

    @Insert
    suspend fun insert(entity: BaseEntity)
}