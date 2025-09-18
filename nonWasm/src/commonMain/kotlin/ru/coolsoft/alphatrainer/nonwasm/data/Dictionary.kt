package ru.coolsoft.alphatrainer.nonwasm.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.ForeignKey.Companion.RESTRICT
import androidx.room.Query
import ru.coolsoft.alphatrainer.shared.ISymbol

@Entity(
    "Dict",
    primaryKeys = ["_id", "LangId"],
    foreignKeys = [
        ForeignKey(BaseEntity::class, ["_id"], ["LangId"], RESTRICT, CASCADE),
        ForeignKey(BaseEntity::class, ["_id"], ["LevelId"], RESTRICT, CASCADE)
    ]
)
data class DictEntity(
    @ColumnInfo(name = "_id") override val id: String,
    @ColumnInfo(name = "LangId") override val transcriptionLanguage: String,
    @ColumnInfo(name = "LevelId") val dictionaryLevel: String,
    @ColumnInfo(name = "Word") override val name: String,
) : ISymbol


@Dao
interface DictionaryDao {
    @Query(
        """
        SELECT d._id,
               d.Word AS Symbol,
               s.Word AS Spell
          FROM Dict d
               LEFT JOIN
               Dict s ON d._id = s._id AND
                         d.LevelId = s.LevelId AND
                         s.LangId = :scriptLanguage
         WHERE d.LevelId = :dictionary AND
               d.LangId LIKE :learntLanguage || '%'
                """
    )
    suspend fun getAllWordPairs(
        dictionary: String,
        learntLanguage: String,
        scriptLanguage: String,
    ): List<SymbolPairEntity>
}
