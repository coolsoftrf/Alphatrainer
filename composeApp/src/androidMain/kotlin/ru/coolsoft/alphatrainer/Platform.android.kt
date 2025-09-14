package ru.coolsoft.alphatrainer

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import ru.coolsoft.alphatrainer.shared.MultiplatformLogger

actual fun logger() = object : MultiplatformLogger{
    override fun d(tag: String?, message: String) {
        Log.d(tag,message)
    }

    override fun i(tag: String?, message: String) {
        Log.i(tag,message)
    }

    override fun e(tag: String?, message: String) {
        Log.e(tag,message)
    }
}


@Composable
actual fun appLocale(): String {
    val context = LocalContext.current
    return AndroidAppLocaleManager(context).getLocale()
}