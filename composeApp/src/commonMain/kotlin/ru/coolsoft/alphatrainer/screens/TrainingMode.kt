package ru.coolsoft.alphatrainer.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import ru.coolsoft.alphatrainer.Section
import ru.coolsoft.alphatrainer.data.LocalizedString


@Composable
fun TrainingModeScreen(
    forLanguage: String,
    alphabets: List<LocalizedString>,
    transcriptionLanguages: Map<String, List<LocalizedString>>,
    pairCountState: TextFieldState,
    selectedSection: MutableState<Section>,

    dictionaries: List<LocalizedString>,
    dictionaryToTranscriptsMap: Map<String, List<LocalizedString>>,

    onAlphabetSelected: OnAlphabetSelectedListener,
    onDictionarySelected: OnDictionarySelectedListener
) {
    Box {
        when (selectedSection.value) {
            Section.Alphabet -> AlphabetsScreen(
                alphabets,
                transcriptionLanguages,
                pairCountState,
                onAlphabetSelected
            )

            Section.Dictionary -> DictionariesScreen(
                forLanguage,
                dictionaries,
                dictionaryToTranscriptsMap,
                pairCountState,
                onDictionarySelected
            )
        }
    }
}
