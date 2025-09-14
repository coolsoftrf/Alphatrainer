package ru.coolsoft.alphatrainer.nonwasm.data

import androidx.room.Room
import androidx.room.RoomDatabase
import ru.coolsoft.alphatrainer.shared.DATABASE_FILENAME
import java.io.File

fun getDatabaseBuilder(dbAssetUri: String): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("user.dir"), DATABASE_FILENAME)
    if (!dbFile.exists()) dbFile.outputStream().use { outStream ->
        Unit.javaClass.getResourceAsStream("/${dbAssetUri.split("!/")[1]}")
            .use { inStream ->
                inStream!!.copyTo(outStream)
            }
    }
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
    )
}

fun getDatabase(dbAssetUri: String): AppDatabase = getRoomDatabase(getDatabaseBuilder(dbAssetUri))
