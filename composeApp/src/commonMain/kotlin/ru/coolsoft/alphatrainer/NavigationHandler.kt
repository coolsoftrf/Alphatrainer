package ru.coolsoft.alphatrainer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.coolsoft.alphatrainer.data.LocalizedString
import ru.coolsoft.alphatrainer.data.availableAlphabets
import ru.coolsoft.alphatrainer.data.dictionariesForLanguage
import ru.coolsoft.alphatrainer.data.scriptAlphabetsForAlphabetList
import ru.coolsoft.alphatrainer.data.scriptAlphabetsForDictionaries

typealias AlphabetsForLanguageProvider = suspend (language: String) -> List<LocalizedString>
typealias AlphabetsForAlphabetListProvider =
        suspend (languages: List<String>) -> Map<String, List<LocalizedString>>

class NavigationHandlerScope(
    val navController: NavHostController,
    val scope: CoroutineScope,
    val alphabetListProvider: AlphabetsForLanguageProvider = ::availableAlphabets,
    val transcriptionLanguageListProvider: AlphabetsForAlphabetListProvider = ::scriptAlphabetsForAlphabetList
) {
    fun onLanguageSelected(languageId: String) {
        scope.launch {
            lateinit var alphabets: List<LocalizedString>
            lateinit var scriptLanguages: Map<String, List<LocalizedString>>
            lateinit var dictionaries: List<LocalizedString>
            lateinit var dictionaryToTranscriptsMap: Map<String, List<LocalizedString>>
            listOf(
                launch {
                    alphabets = alphabetListProvider(languageId)
                    scriptLanguages = transcriptionLanguageListProvider(
                        alphabets.map { parentLanguageFor(it.id) }.distinct()
                    )
                }, launch {
                    dictionaries = dictionariesForLanguage(languageId)
                    dictionaryToTranscriptsMap =
                        scriptAlphabetsForDictionaries(dictionaries.map { it.id })
                }
            ).joinAll()
            withContext(Dispatchers.Main) {
                onDataReady(
                    languageId,
                    alphabets,
                    scriptLanguages,
                    dictionaries,
                    dictionaryToTranscriptsMap
                )
            }
        }
    }

    private fun onDataReady(
        forLanguage: String,
        alphabets: List<LocalizedString>,
        scriptLanguages: Map<String, List<LocalizedString>>,
        dictionaries: List<LocalizedString>,
        dictionaryToTranscriptsMap: Map<String, List<LocalizedString>>
    ) {
        navController.navigate(
            ModeChooser(
                forLanguage,
                alphabets, scriptLanguages,
                dictionaries, dictionaryToTranscriptsMap
            )
        )
    }

    fun onAlphabetSelected(alphabetId: String, scriptLanguageId: String, pairCount: Int) {
        navController.navigate(
            flipcards(
                Section.Alphabet,
                alphabetId,
                scriptLanguageId,
                pairCount
            )
        )
    }

    fun onDictionarySelected(
        dictId: String,
        alphabetId: String,
        scriptAlphabetId: String,
        pairCount: Int
    ) {
        navController.navigate(
            Flipcards(
                Section.Dictionary,
                dictId,
                alphabetId,
                scriptAlphabetId,
                pairCount
            )
        )
    }
}

@Composable
fun NavigationHandler(
    navController: NavHostController,
    alphabetListProvider: AlphabetsForLanguageProvider = ::availableAlphabets,
    transcriptionLanguageListProvider: AlphabetsForAlphabetListProvider = ::scriptAlphabetsForAlphabetList,
    content: @Composable NavigationHandlerScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val navigationHandlerScope =
        remember(
            navController,
            alphabetListProvider,
            transcriptionLanguageListProvider
        ) {
            NavigationHandlerScope(
                navController,
                scope,
                alphabetListProvider,
                transcriptionLanguageListProvider
            )
        }

    return navigationHandlerScope.content()
}