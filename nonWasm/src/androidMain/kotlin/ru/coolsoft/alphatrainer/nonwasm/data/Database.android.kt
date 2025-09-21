package ru.coolsoft.alphatrainer.nonwasm.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.coolsoft.alphatrainer.javashared.assertDatabase
import ru.coolsoft.alphatrainer.shared.DATABASE_FILENAME

fun getDatabaseBuilder(ctx: Context, dbAssetUri: String): RoomDatabase.Builder<AppDatabase> {
    val appContext = ctx.applicationContext

    val dbFile = appContext.getDatabasePath(DATABASE_FILENAME)
    dbFile.assertDatabase (
        ctx.assets.open(dbAssetUri.substringAfter("file:///android_asset/"))
    )

    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}

fun getDatabase(ctx: Context, dbAssetUri: String): AppDatabase {
    return getRoomDatabase(getDatabaseBuilder(ctx, dbAssetUri))
}