package ru.coolsoft.alphatrainer.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.selectAll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import ru.coolsoft.alphatrainer.screens.DEFAULT_PAIR_COUNT


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PairCountInput(labelText: String, pairCountState: TextFieldState) {
    var selectionPendingState by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val interactionState by interactionSource.interactions.collectAsState(null)
    if (interactionState is PressInteraction.Press) {
        pairCountState.edit { selectAll() }
        selectionPendingState = true
    }
    OutlinedTextField(
        state = pairCountState,
        label = { Text(labelText) },
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