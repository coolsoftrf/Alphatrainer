package ru.coolsoft.alphatrainer.screens

import alphatrainer.composeapp.generated.resources.Res
import alphatrainer.composeapp.generated.resources.alphabet
import alphatrainer.composeapp.generated.resources.dictionaries_assorted
import alphatrainer.composeapp.generated.resources.im_learning_words
import alphatrainer.composeapp.generated.resources.script_alphabet
import alphatrainer.composeapp.generated.resources.word_pair_count
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.coolsoft.alphatrainer.FALLBACK_ID
import ru.coolsoft.alphatrainer.LocalAppLocalization
import ru.coolsoft.alphatrainer.components.LanguageSelector
import ru.coolsoft.alphatrainer.components.PairCountInput
import ru.coolsoft.alphatrainer.components.ShadedBox
import ru.coolsoft.alphatrainer.data.LocalizedString


typealias OnDictionarySelectedListener = (dictId: String, alphabetId: String, scriptAlphabetId: String, pairCount: Int) -> Unit

@Composable
fun DictionariesScreen(
    forLanguage: String,
    dictionaries: Map<String, Pair<LocalizedString?, List<LocalizedString>>>,
    dictionaryToTranscriptsMap: Map<String, List<LocalizedString>>,
    pairCountState: TextFieldState,
    onDictionarySelected: OnDictionarySelectedListener
) {
    val options = dictionaryToTranscriptsMap.values.flatten().distinct()
    val languageOptions = options.filter { it.id.startsWith(forLanguage) }.sortedBy { it.id }
    val transcriptOptions = options.filter { it.isPrimary }.sortedBy { it.id }

    val scrollState = rememberScrollState()
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .align(Alignment.Center)
                .verticalScroll(scrollState)
                .width(IntrinsicSize.Min),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val locale = LocalAppLocalization.current

            //ToDo: validate Selected Script when Selected Alphabet changes
            val selectedAlphabetOrdinal = rememberSaveable {
                mutableStateOf(with(languageOptions) {
                    indexOfFirst { it.id.startsWith(forLanguage) }
                })
            }
            val selectedAlphabet = languageOptions[selectedAlphabetOrdinal.value]

            val selectedScriptLanguageOrdinal = rememberSaveable {
                mutableStateOf(with(transcriptOptions) {
                    listOf(locale, FALLBACK_ID).firstNotNullOfOrNull {
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
                selectedAlphabetOrdinal,
                true
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
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun Dictionaries(
    forLanguage: String,
    dictionaries: Map<String, Pair<LocalizedString?, List<LocalizedString>>>,
    locale: String,
    onDictionarySelected: OnDictionarySelectedListener,
    selectedAlphabet: LocalizedString,
    selectedScriptAlphabet: LocalizedString,
    pairCount: Int,
    dictionaryToTranscriptMap: Map<String, List<LocalizedString>>
) {
    dictionaries.forEach { section ->
        var expanded by rememberSaveable(section.key) { mutableStateOf(false) }
        section.value.first?.run {
            ShadedBox(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .clickable {
                        expanded = !expanded
                    },
                Orientation.Horizontal,
                40.dp
            ) {
                Text("\n") //placeholder
                Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (id == FALLBACK_ID) stringResource(Res.string.dictionaries_assorted)
                        else name(forLanguage),
                        style = MaterialTheme.typography.titleMediumEmphasized
                    )
                    if (id != FALLBACK_ID) (transcription(locale) ?: name).let {
                        if (it != name(forLanguage))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmallEmphasized
                            )
                    }
                }
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded,
                    Modifier
                        .minimumInteractiveComponentSize()
                        .align(Alignment.CenterEnd)
                )
            }
        }

        AnimatedVisibility(
            expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                section.value.second
                    .filter { d -> d.isPrimary }
                    .map { d ->
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
                                            .let { it == FALLBACK_ID || contains(it) }
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
        }
    }
}