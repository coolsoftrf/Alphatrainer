package ru.coolsoft.alphatrainer.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ru.coolsoft.alphatrainer.Section
import ru.coolsoft.alphatrainer.data.LocalizedString

private operator fun Pair<EnterTransition, ExitTransition>.plus(
    other: Pair<EnterTransition, ExitTransition>
): Pair<EnterTransition, ExitTransition> {
    return (first + other.first) to (second + other.second)
}

@Composable
fun TrainingModeScreen(
    forLanguage: String,
    alphabets: List<LocalizedString>,
    transcriptionLanguages: Map<String, List<LocalizedString>>,
    pairCountState: TextFieldState,
    selectedSection: Section?,

    dictionaries: Map<String, Pair<LocalizedString?, List<LocalizedString>>>,
    dictionaryToTranscriptsMap: Map<String, List<LocalizedString>>,

    onAlphabetSelected: OnAlphabetSelectedListener,
    onDictionarySelected: OnDictionarySelectedListener
) {
    val currentSelection = remember { (mutableStateOf(selectedSection)) }
    val (enter, exit) = (
            if ((selectedSection?.ordinal ?: 0) > (currentSelection.value?.ordinal ?: 0))
                slideInHorizontally { it / 2 } to slideOutHorizontally()
            else slideInHorizontally() to slideOutHorizontally { it / 2 }
            ) + (fadeIn() to fadeOut())
    Box {
        AnimatedVisibility(selectedSection == Section.Alphabet, enter = enter, exit = exit) {
            AlphabetsScreen(
                alphabets,
                transcriptionLanguages,
                pairCountState,
                onAlphabetSelected
            )
        }
        AnimatedVisibility(selectedSection == Section.Dictionary, enter = enter, exit = exit) {
            DictionariesScreen(
                forLanguage,
                dictionaries,
                dictionaryToTranscriptsMap,
                pairCountState,
                onDictionarySelected
            )
        }
    }
}
