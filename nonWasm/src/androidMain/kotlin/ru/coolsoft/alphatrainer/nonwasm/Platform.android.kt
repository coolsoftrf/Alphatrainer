package ru.coolsoft.alphatrainer.nonwasm

import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.core.qualifier.qualifier
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import ru.coolsoft.alphatrainer.javashared.Koin
import ru.coolsoft.alphatrainer.nonwasm.data.AppDatabase
import ru.coolsoft.alphatrainer.nonwasm.data.getDatabase


actual fun platformModule() = module {
    single<AppDatabase> {
        getDatabase(get(), get(qualifier( DATABASE_ASSET_URI_QUALIFIER)))
    }
}

internal actual val doSetupKoin:(
    platformModule: Module,
    appDeclaration: KoinAppDeclaration,
    initKoin: (platformModule: Module, appDeclaration: KoinAppDeclaration) -> KoinApplication
)->Unit = Koin::setupKoin