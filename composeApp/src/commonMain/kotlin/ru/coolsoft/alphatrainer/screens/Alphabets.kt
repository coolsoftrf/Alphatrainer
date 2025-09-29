package ru.coolsoft.alphatrainer.screens

import alphatrainer.composeapp.generated.resources.Res
import alphatrainer.composeapp.generated.resources.char_pair_count
import alphatrainer.composeapp.generated.resources.im_learning_alphabet
import alphatrainer.composeapp.generated.resources.script_alphabet
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
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
import ru.coolsoft.alphatrainer.FALLBACK_ID
import ru.coolsoft.alphatrainer.LocalAppLocalization
import ru.coolsoft.alphatrainer.parentLanguageFor
import ru.coolsoft.alphatrainer.components.LanguageSelector
import ru.coolsoft.alphatrainer.components.LocalizedText
import ru.coolsoft.alphatrainer.components.PairCountInput
import ru.coolsoft.alphatrainer.data.LocalizedString


const val DEFAULT_PAIR_COUNT = 6

typealias OnAlphabetSelectedListener = (alphabetId: String, scriptLanguageId: String, pairCount: Int) -> Unit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlphabetsScreen(
    alphabets: List<LocalizedString>,
    baseToTranscriptsMap: Map<String, List<LocalizedString>>,
    pairCountState: TextFieldState,
    onAlphabetSelected: OnAlphabetSelectedListener
) {
    val options = baseToTranscriptsMap.values.flatten().distinct()
    val scrollState = rememberScrollState()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val locale = LocalAppLocalization.current
        val selectedScriptLanguageOrdinal = rememberSaveable {
            mutableStateOf(with(options) {
                listOf(locale, FALLBACK_ID).firstNotNullOfOrNull {
                    indexOfFirst { opt -> it == opt.id }.let { i ->
                        if (i == -1) null else i
                    }
                } ?: 0
            })
        }
        val selectedScriptLanguage = options[selectedScriptLanguageOrdinal.value]

        Text(
            text = stringResource(Res.string.im_learning_alphabet),
            modifier = Modifier.padding(20.dp),
            style = MaterialTheme.typography.titleMedium
        )

        Alphabets(
            alphabets,
            locale,
            onAlphabetSelected,
            pairCountState,
            selectedScriptLanguage,
            baseToTranscriptsMap
        )
        Spacer(Modifier.height(40.dp))
        LanguageSelector(
            stringResource(Res.string.script_alphabet),
            selectedScriptLanguage,
            options,
            locale,
            selectedScriptLanguageOrdinal
        )
        Spacer(Modifier.height(20.dp))
        PairCountInput(stringResource(Res.string.char_pair_count), pairCountState)
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun Alphabets(
    alphabets: List<LocalizedString>,
    locale: String,
    onAlphabetSelected: (alphabetId: String, scriptLanguageId: String, pairCount: Int) -> Unit,
    pairCountState: TextFieldState,
    selectedScriptLanguage: LocalizedString,
    baseToTranscriptMap: Map<String, List<LocalizedString>>
) {
    alphabets.map { a ->
        OutlinedButton(
            {
                onAlphabetSelected(
                    a.id,
                    selectedScriptLanguage.id,
                    pairCountState.text.toString().toInt()
                )
            },
            Modifier.padding(10.dp),
            with(a) {
                selectedScriptLanguage.id == FALLBACK_ID ||
                        baseToTranscriptMap[parentLanguageFor(id)]
                            ?.run {
                                map { it.id }.contains(selectedScriptLanguage.id)
                            } ?: false &&
                        id != selectedScriptLanguage.id
            }
        ) {
            LocalizedText(a.name, a.transcription(locale))
        }
    }
}