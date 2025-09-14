package ru.coolsoft.alphatrainer.nonwasm.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(entities = [Entity::class, SpellEntity::class, SymbolEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getEntitiesDao(): EntitiesDao
    abstract fun getAlphabetDao(): AlphabetDao
}

fun getRoomDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}