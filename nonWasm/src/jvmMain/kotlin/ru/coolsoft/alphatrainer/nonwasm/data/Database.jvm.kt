package ru.coolsoft.alphatrainer.nonwasm.data

import androidx.room.Room
import androidx.room.RoomDatabase
import ru.coolsoft.alphatrainer.javashared.assertDatabase
import ru.coolsoft.alphatrainer.shared.DATABASE_FILENAME
import java.io.File

fun getDatabaseBuilder(dbAssetUri: String): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("user.dir"), DATABASE_FILENAME)
    dbFile.assertDatabase (
        Unit.javaClass.getResourceAsStream("/${dbAssetUri.split("!/")[1]}")
    )
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
    )
}

fun getDatabase(dbAssetUri: String): AppDatabase = getRoomDatabase(getDatabaseBuilder(dbAssetUri))
