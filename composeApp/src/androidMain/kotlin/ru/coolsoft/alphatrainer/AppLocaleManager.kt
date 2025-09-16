package ru.coolsoft.alphatrainer

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.getSystemService
import kotlin.getValue

private const val LOCALE_SPLITTER = "-"

class AndroidAppLocaleManager(context: Context) : AppLocaleManager {
    private val localManager by lazy { context.getSystemService<LocaleManager>() }

    override fun getLocale(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val locales = localManager?.applicationLocales ?: return DEFAULT_LANGUAGE
            if (locales.isEmpty) DEFAULT_LANGUAGE else
                locales[0]?.toLanguageTag()
        } else {
            AppCompatDelegate.getApplicationLocales().toLanguageTags()
        }
            ?.split(LOCALE_SPLITTER)?.firstOrNull()
            ?: DEFAULT_LANGUAGE
    }
}