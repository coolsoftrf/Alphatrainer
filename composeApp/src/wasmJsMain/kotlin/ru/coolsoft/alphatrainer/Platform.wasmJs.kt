package ru.coolsoft.alphatrainer

import androidx.compose.runtime.Composable
import ru.coolsoft.alphatrainer.shared.MultiplatformLogger
import ru.coolsoft.alphatrainer.nonandroid.systemLogger

actual fun logger(): MultiplatformLogger = systemLogger()

@Composable
actual fun appLocale(): String {
    TODO("Not yet implemented")
}