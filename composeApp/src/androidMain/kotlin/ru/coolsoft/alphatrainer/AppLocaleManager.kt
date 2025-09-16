package ru.coolsoft.alphatrainer

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService

private const val LOCALE_SPLITTER = "-"

class AndroidAppLocaleManager(val context: Context) : AppLocaleManager {
    private val localManager by lazy { context.getSystemService<LocaleManager>() }

    override fun getLocale(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            localManager?.applicationLocales ?: return DEFAULT_LANGUAGE
        } else {
            context.resources.configuration.locales
        }.run {
            if (isEmpty) return DEFAULT_LANGUAGE else get(0)?.toLanguageTag()
        }
            ?.split(LOCALE_SPLITTER)?.firstOrNull()
            ?: DEFAULT_LANGUAGE
    }
}