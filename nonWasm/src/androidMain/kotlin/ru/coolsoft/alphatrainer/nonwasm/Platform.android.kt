package ru.coolsoft.alphatrainer.nonwasm

import org.koin.core.qualifier.qualifier
import org.koin.dsl.module
import ru.coolsoft.alphatrainer.nonwasm.data.AppDatabase
import ru.coolsoft.alphatrainer.nonwasm.data.getDatabase

actual fun platformModule() = module {
    single<AppDatabase> {
        getDatabase(get(), get(qualifier( DATABASE_ASSET_URI_QUALIFIER)))
    }
}
