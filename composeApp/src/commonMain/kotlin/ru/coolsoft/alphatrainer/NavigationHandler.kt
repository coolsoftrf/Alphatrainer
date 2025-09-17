package ru.coolsoft.alphatrainer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.coolsoft.alphatrainer.data.LocalizedString
import ru.coolsoft.alphatrainer.data.availableAlphabets
import ru.coolsoft.alphatrainer.data.scriptAlphabetsForAlphabetList

typealias AlphabetsForLanguageProvider = suspend (language: String) -> List<LocalizedString>
typealias AlphabetsForAlphabetListProvider =
        suspend (languages: List<String>) -> Map<String, List<LocalizedString>>

class NavigationHandlerScope(
    val navController: NavHostController,
    val pairCount: Int, //ToDo: remaster UI flow to get rid of this implicity
    val scope: CoroutineScope,
    val alphabetListProvider: AlphabetsForLanguageProvider = ::availableAlphabets,
    val transcriptionLanguageListProvider: AlphabetsForAlphabetListProvider = ::scriptAlphabetsForAlphabetList
) {
    fun onLanguageSelected(languageId: String) {
        scope.launch {
            val alphabets = alphabetListProvider(languageId)
            val scriptLanguages =
                transcriptionLanguageListProvider(
                    alphabets.map { baseLanguageFor(it.id) }
                )
            withContext(Dispatchers.Main) {
                onAlphabetsReady(alphabets, scriptLanguages)
            }
        }
    }

    fun onAlphabetsReady(
        alphabets: List<LocalizedString>,
        baseToTranscriptMap: Map<String, List<LocalizedString>>,
    ) {
        if (alphabets.size == 1 &&
            baseToTranscriptMap[baseLanguageFor(alphabets.first().id)] == null
        ) {
            navController.navigate(
                flipcards(
                    alphabets.first().id,
                    FALLBACK_LANGUAGE,
                    pairCount
                )
            )
        } else {
            navController.navigate(
                AlphabetChooser(alphabets, baseToTranscriptMap)
            )
        }
    }

    fun onAlphabetSelected(alphabetId: String, scriptLanguageId: String, pairCount: Int) {
        navController.navigate(flipcards(alphabetId, scriptLanguageId, pairCount))
    }
}

@Composable
fun NavigationHandler(
    navController: NavHostController,
    pairCount: Int,
    alphabetListProvider: AlphabetsForLanguageProvider = ::availableAlphabets,
    transcriptionLanguageListProvider: AlphabetsForAlphabetListProvider = ::scriptAlphabetsForAlphabetList,
    content: @Composable NavigationHandlerScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val navigationHandlerScope =
        remember(
            navController,
            pairCount,
            alphabetListProvider,
            transcriptionLanguageListProvider
        ) {
            NavigationHandlerScope(
                navController,
                pairCount,
                scope,
                alphabetListProvider,
                transcriptionLanguageListProvider
            )
        }

    return navigationHandlerScope.content()
}