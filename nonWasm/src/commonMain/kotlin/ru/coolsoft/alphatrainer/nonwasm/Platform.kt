package ru.coolsoft.alphatrainer.nonwasm

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.qualifier.qualifier
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import ru.coolsoft.alphatrainer.nonwasm.data.AlphabetRepository
import ru.coolsoft.alphatrainer.nonwasm.data.DictionaryRepository
import ru.coolsoft.alphatrainer.nonwasm.data.LanguageRepository
import ru.coolsoft.alphatrainer.shared.IAlphabetRepository
import ru.coolsoft.alphatrainer.shared.IDictionaryRepository
import ru.coolsoft.alphatrainer.shared.ILanguageRepository

expect fun platformModule(): Module

private fun commonModule(): Module {
    return module {
        single<ILanguageRepository> { LanguageRepository(get())}
        single<IAlphabetRepository> { AlphabetRepository(get()) }
        single<IDictionaryRepository> { DictionaryRepository(get()) }
    }
}

private fun initKoin(platformModule: Module, appDeclaration: KoinAppDeclaration) =
    startKoin {
        appDeclaration()
        modules(platformModule, commonModule())
    }

internal expect val doSetupKoin: (
    platformModule: Module,
    appDeclaration: KoinAppDeclaration,
    initKoin: (platformModule: Module, appDeclaration: KoinAppDeclaration) -> KoinApplication
) -> Unit

fun setupKoin(appDeclaration: KoinAppDeclaration = {}) {
    doSetupKoin(platformModule(), appDeclaration, ::initKoin)
}

const val DATABASE_ASSET_URI_QUALIFIER = "dbUri"
fun KoinApplication.databasePath(dbPath: String) {
    koin.loadModules(
        listOf(
            module {
                single(qualifier(DATABASE_ASSET_URI_QUALIFIER)) { dbPath }
            }
        ))
}