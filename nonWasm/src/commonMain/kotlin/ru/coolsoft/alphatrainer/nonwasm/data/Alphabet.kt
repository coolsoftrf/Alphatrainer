package ru.coolsoft.alphatrainer.nonwasm.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.coolsoft.alphatrainer.shared.ISymbol
import ru.coolsoft.alphatrainer.shared.ISymbolPair

@Entity("Alphabets", primaryKeys = ["_id", "LangId", "ScriptLangId"])
data class SymbolEntity(
    @ColumnInfo(name = "_id") override val id: String,
    @ColumnInfo(name = "Symbol") override val name: String,
    @ColumnInfo(name = "Auxiliary") val aux: Int,
    @ColumnInfo(name = "LangId") val language: String,
    @ColumnInfo(name = "ScriptLangId") override val transcriptionLanguage: String,
) : ISymbol {
    constructor(symbol: ISymbol, language: String, aux: Int) : this(
        symbol.id,
        symbol.name,
        aux,
        language,
        symbol.transcriptionLanguage,
    )
}

data class SymbolPairEntity(
    @ColumnInfo(name = "_id") override val id: String,
    @ColumnInfo(name = "Symbol") override val name: String,
    @ColumnInfo(name = "Spell") override val spellName: String?
) : ISymbolPair

@Dao
interface AlphabetDao {
    @Query(
        """
        SELECT a._id,
               a.Symbol,
               s.Symbol AS Spell
          FROM Alphabets a
               LEFT JOIN
               Alphabets s ON a._id = s._id AND
                              a.LangId = s.LangId AND
                              s.ScriptLangId = :scriptLanguage
         WHERE a.LangId = :language AND
               a.ScriptLangId = :learntLanguage AND
               a.auxiliary = 0
                """
    )
    fun getAllSymbolPairs(
        language: String,
        learntLanguage: String,
        scriptLanguage: String,
    ): Flow<List<SymbolPairEntity>>

    @Insert
    suspend fun insert(symbol: SymbolEntity)
}