package ru.coolsoft.alphatrainer

import androidx.compose.runtime.Composable
import ru.coolsoft.alphatrainer.shared.MultiplatformLogger
import ru.coolsoft.alphatrainer.nonandroid.systemLogger
import java.util.Locale

actual fun logger(): MultiplatformLogger = systemLogger()

@Composable
actual fun appLocale(): String {
    return Locale.getDefault().language
}