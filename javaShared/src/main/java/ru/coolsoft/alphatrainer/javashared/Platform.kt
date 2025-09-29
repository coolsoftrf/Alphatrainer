package ru.coolsoft.alphatrainer.javashared

import org.koin.core.KoinApplication
import org.koin.dsl.KoinAppDeclaration
import org.koin.core.module.Module


object Koin {
    var di: KoinApplication? = null

    fun setupKoin(
        platformModule: Module,
        appDeclaration: KoinAppDeclaration,
        initKoin: (platformModule: Module, appDeclaration: KoinAppDeclaration) -> KoinApplication
    ) {
        if (di == null) {
            di = initKoin(platformModule, appDeclaration)
        }
    }
}