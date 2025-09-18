package ru.coolsoft.alphatrainer.screens

import alphatrainer.composeapp.generated.resources.Res
import alphatrainer.composeapp.generated.resources.alphabet
import alphatrainer.composeapp.generated.resources.im_learning_words
import alphatrainer.composeapp.generated.resources.script_alphabet
import alphatrainer.composeapp.generated.resources.word_pair_count
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.coolsoft.alphatrainer.FALLBACK_LANGUAGE
import ru.coolsoft.alphatrainer.LocalAppLocalization
import ru.coolsoft.alphatrainer.components.LanguageSelector
import ru.coolsoft.alphatrainer.components.PairCountInput
import ru.coolsoft.alphatrainer.data.LocalizedString


typealias OnDictionarySelectedListener = (dictId: String, alphabetId: String, scriptAlphabetId: String, pairCount: Int) -> Unit

@Composable
fun DictionariesScreen(
    forLanguage: String,
    dictionaries: List<LocalizedString>, //ToDo: migrate to categorized list
    dictionaryToTranscriptsMap: Map<String, List<LocalizedString>>,
    pairCountState: TextFieldState,
    onDictionarySelected: OnDictionarySelectedListener
) {
    val options = dictionaryToTranscriptsMap.values.flatten().distinct()
    //ToDo: order and indent options hierarchically
    val languageOptions = options.filter { it.id.startsWith(forLanguage) }
    val transcriptOptions = options.filter { it.isPrimary }

    val scrollState = rememberScrollState()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val locale = LocalAppLocalization.current

        //ToDo: validate Selected Script when Selected Alphabet changes
        val selectedAlphabetOrdinal = rememberSaveable {
            mutableStateOf(languageOptions.run {
                indexOfFirst { it.id.startsWith(forLanguage) }
            })
        }
        val selectedAlphabet = languageOptions[selectedAlphabetOrdinal.value]

        val selectedScriptLanguageOrdinal = rememberSaveable {
            mutableStateOf(transcriptOptions.run {
                listOf(locale, FALLBACK_LANGUAGE).firstNotNullOfOrNull {
                    indexOfFirst { opt -> it == opt.id }.let { i ->
                        if (i == -1) null else i
                    }
                } ?: 0
            })
        }
        val selectedScriptLanguage = transcriptOptions[selectedScriptLanguageOrdinal.value]

        Text(
            text = stringResource(Res.string.im_learning_words),
            modifier = Modifier.padding(20.dp),
            style = MaterialTheme.typography.titleMedium
        )

        LanguageSelector(
            stringResource(Res.string.alphabet),
            selectedAlphabet,
            languageOptions,
            locale,
            selectedAlphabetOrdinal
        )
        Spacer(Modifier.height(20.dp))
        LanguageSelector(
            stringResource(Res.string.script_alphabet),
            selectedScriptLanguage,
            transcriptOptions,
            locale,
            selectedScriptLanguageOrdinal

        ) {
            !it.id.startsWith(languageOptions[selectedAlphabetOrdinal.value].id)
        }
        Spacer(Modifier.height(20.dp))
        Dictionaries(
            forLanguage,
            dictionaries,
            locale,
            onDictionarySelected,
            selectedAlphabet,
            selectedScriptLanguage,
            pairCountState.text.toString().toInt(),
            dictionaryToTranscriptsMap
        )
        Spacer(Modifier.height(20.dp))
        PairCountInput(stringResource(Res.string.word_pair_count), pairCountState)
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun Dictionaries(
    forLanguage: String,
    dictionaries: List<LocalizedString>,
    locale: String,
    onDictionarySelected: OnDictionarySelectedListener,
    selectedAlphabet: LocalizedString,
    selectedScriptAlphabet: LocalizedString,
    pairCount: Int,
    dictionaryToTranscriptMap: Map<String, List<LocalizedString>>
) {
    dictionaries.map { d ->
        OutlinedButton(
            {
                onDictionarySelected(
                    d.id,
                    selectedAlphabet.id,
                    selectedScriptAlphabet.id,
                    pairCount
                )
            },
            Modifier.padding(10.dp),
            //ToDo: consider if any match found for the alphabet pair
            (dictionaryToTranscriptMap[d.id]?.map { it.id }?.run {
                contains(selectedAlphabet.id) &&
                        selectedScriptAlphabet.id
                            .let { it == FALLBACK_LANGUAGE || contains(it) }
            } ?: false) &&
                    d.id != selectedScriptAlphabet.id
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = d.name(forLanguage),
                    style = MaterialTheme.typography.titleLarge
                )
                (d.transcription(locale) ?: d.name).let {
                    if (it != d.name(forLanguage))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium
                        )
                }
            }
        }
    }
}