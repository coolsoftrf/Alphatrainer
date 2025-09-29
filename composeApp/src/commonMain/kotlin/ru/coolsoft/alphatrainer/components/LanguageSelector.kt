package ru.coolsoft.alphatrainer.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.coolsoft.alphatrainer.data.LocalizedString
import ru.coolsoft.alphatrainer.hierarchyLevel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelector(
    label: String,
    selectedScriptLanguage: LocalizedString,
    options: List<LocalizedString>,
    locale: String,
    selectedScriptLanguageOrdinal: MutableState<Int>,
    hierarchical: Boolean = false,
    isEnabled: (option: LocalizedString) -> Boolean = { true }
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
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEachIndexed { i, option ->
                DropdownMenuItem(
                    text = {
                        Row {
                            if (hierarchical) {
                                Spacer(Modifier.width((option.id.hierarchyLevel() * 20).dp))
                            }
                            LocalizedText(option.name, option.transcription(locale), Size.Small,
                                horizontalAlignment = Alignment.Start)
                        }
                    },
                    onClick = {
                        selectedScriptLanguageOrdinal.value = i
                        textFieldState.setTextAndPlaceCursorAtEnd(option.name)
                        expanded = false
                    },
                    enabled = isEnabled(option),
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}