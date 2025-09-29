package ru.coolsoft.alphatrainer.nonwasm.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.coolsoft.alphatrainer.javashared.assertDatabase
import ru.coolsoft.alphatrainer.javashared.md5
import ru.coolsoft.alphatrainer.shared.DATABASE_FILENAME
import java.io.File

fun getDatabaseBuilder(ctx: Context, dbAssetUri: String): RoomDatabase.Builder<AppDatabase> {
    val appContext = ctx.applicationContext

    val dbFile = appContext.getDatabasePath(DATABASE_FILENAME)
    val dbObbFile = File(appContext.obbDir, "Databases/$DATABASE_FILENAME")
    if (dbObbFile.exists()) {
        // restore from a user copy
        if (!dbFile.exists() || dbObbFile.md5 != dbFile.md5) {
            dbFile.copyTo(File("${dbObbFile.absolutePath}.original"), true)
            dbObbFile.copyTo(dbFile, true)
            dbObbFile.renameTo(File("${dbObbFile.absolutePath}.copied"))
        }
    } else {
        // create a working copy from assets
        dbFile.assertDatabase(
            ctx.assets.open(dbAssetUri.substringAfter("file:///android_asset/"))
        )
        // and prepare a user accessible copy
        dbFile.copyTo(dbObbFile)
    }

    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}

fun getDatabase(ctx: Context, dbAssetUri: String): AppDatabase {
    return getRoomDatabase(getDatabaseBuilder(ctx, dbAssetUri))
}