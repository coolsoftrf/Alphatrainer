package ru.coolsoft.alphatrainer

import alphatrainer.composeapp.generated.resources.Res
import androidx.compose.runtime.Composable
import ru.coolsoft.alphatrainer.shared.DATABASE_FILENAME
import ru.coolsoft.alphatrainer.shared.MultiplatformLogger

expect fun logger(): MultiplatformLogger

val dbAssetUri = Res.getUri("files/${DATABASE_FILENAME}")

interface AppLocaleManager {
    fun getLocale(): String
}

const val DEFAULT_LANGUAGE = "en"

@Composable
expect fun appLocale(): String