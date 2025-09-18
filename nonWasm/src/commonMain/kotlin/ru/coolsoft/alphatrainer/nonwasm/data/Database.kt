package ru.coolsoft.alphatrainer.nonwasm.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [BaseEntity::class, SpellEntity::class, SymbolEntity::class, DictEntity::class],
    version = 2,
    autoMigrations = [AutoMigration(1, 2)]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getEntitiesDao(): EntitiesDao
    abstract fun getAlphabetDao(): AlphabetDao
    abstract fun getDictionaryDao(): DictionaryDao
}

fun getRoomDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}