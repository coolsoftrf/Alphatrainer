package ru.coolsoft.alphatrainer.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ru.coolsoft.alphatrainer.data.LocalizedString


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelector(
    label: String,
    selectedScriptLanguage: LocalizedString,
    options: List<LocalizedString>,
    locale: String,
    selectedScriptLanguageOrdinal: MutableState<Int>,
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
                        Column {
                            Text(option.name, style = MaterialTheme.typography.titleSmall)
                            option.transcription(locale)?.let {
                                Text(text = it, style = MaterialTheme.typography.bodySmall)
                            }
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