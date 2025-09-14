package ru.coolsoft.alphatrainer.screens

import alphatrainer.composeapp.generated.resources.Res
import alphatrainer.composeapp.generated.resources.im_learning_alphabet
import alphatrainer.composeapp.generated.resources.pair_count
import alphatrainer.composeapp.generated.resources.script_language
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.selectAll
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.coolsoft.alphatrainer.FALLBACK_LANGUAGE
import ru.coolsoft.alphatrainer.LocalAppLocalization
import ru.coolsoft.alphatrainer.baseLanguageFor
import ru.coolsoft.alphatrainer.data.LocalizedString


const val DEFAULT_PAIR_COUNT = 6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlphabetsScreen(
    alphabets: List<LocalizedString>,
    baseToTranscriptMap: Map<String, List<LocalizedString>>,
    pairCountState: TextFieldState,
    onAlphabetSelected: (alphabetId: String, scriptLanguageId: String, pairCount: Int) -> Unit
) {
    val options = baseToTranscriptMap.entries.flatMap { it.value }
    val scrollState = rememberScrollState()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

            val locale = LocalAppLocalization.current
            val selectedScriptLanguageOrdinal = remember {
                mutableStateOf(options.run {
                    listOf(locale, FALLBACK_LANGUAGE).firstNotNullOfOrNull {
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
                onAlphabetSelected,
                pairCountState,
                selectedScriptLanguage,
                baseToTranscriptMap
            )
            Spacer(Modifier.height(40.dp))
            TargetLanguageSelector(selectedScriptLanguage, options, selectedScriptLanguageOrdinal)
            Spacer(Modifier.height(20.dp))
            PairCountInput(pairCountState)
            Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun Alphabets(
    alphabets: List<LocalizedString>,
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
            a.run {
                selectedScriptLanguage.id == FALLBACK_LANGUAGE ||
                        baseToTranscriptMap[baseLanguageFor(id)]
                            ?.run {
                                map { it.id }.contains(selectedScriptLanguage.id)
                            } ?: false &&
                        id != selectedScriptLanguage.id
            }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = a.name, style = MaterialTheme.typography.titleLarge)
                a.transcription?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TargetLanguageSelector(
    selectedScriptLanguage: LocalizedString,
    options: List<LocalizedString>,
    selectedScriptLanguageOrdinal: MutableState<Int>
) {
    var expanded by remember { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState(selectedScriptLanguage.name)
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            state = textFieldState,
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            readOnly = true,
            lineLimits = TextFieldLineLimits.SingleLine,
            label = { Text(stringResource(Res.string.script_language)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEachIndexed { i, option ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(option.name, style = MaterialTheme.typography.titleSmall)
                            option.transcription?.let {
                                Text(text = it, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    },
                    onClick = {
                        selectedScriptLanguageOrdinal.value = i
                        textFieldState.setTextAndPlaceCursorAtEnd(option.name)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PairCountInput(pairCountState: TextFieldState) {
    var selectionPendingState by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val interactionState by interactionSource.interactions.collectAsState(null)
    if (interactionState is PressInteraction.Press) {
        pairCountState.edit { selectAll() }
        selectionPendingState = true
    }
    OutlinedTextField(
        state = pairCountState,
        label = { Text(stringResource(Res.string.pair_count)) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        inputTransformation = transformation@{
            if (selectionPendingState) {
                selectAll()
                selectionPendingState = false
                return@transformation
            }
            asCharSequence().apply {
                forEachIndexed { i, c ->
                    if (!c.isDigit()) replace(i, i + 1, "")
                }
            }
            if (length == 0) {
                replace(0, length, DEFAULT_PAIR_COUNT.toString())
                selectAll()
                return@transformation
            }
            if (selection.length == length) {
                selection = TextRange(length)
            }
        },
        lineLimits = TextFieldLineLimits.SingleLine,
        interactionSource = interactionSource
    )
}