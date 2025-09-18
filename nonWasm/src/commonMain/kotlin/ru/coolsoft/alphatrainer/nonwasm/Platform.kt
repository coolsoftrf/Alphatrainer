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

expect fun platformModule(): Module

fun commonModule(): Module {
    return module {
        single { LanguageRepository(get()) }
        single { AlphabetRepository(get()) }
        single { DictionaryRepository(get()) }
    }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(platformModule(), commonModule())
    }

object Koin {
    var di: KoinApplication? = null

    fun setupKoin(appDeclaration: KoinAppDeclaration = {}) {
        if (di == null) {
            di = initKoin(appDeclaration)
        }
    }
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