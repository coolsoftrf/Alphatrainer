package ru.coolsoft.alphatrainer.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

enum class Size {
    Large,
    Small,
    SmallEmphasized
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LocalizedText(
    main: String,
    transcription: String?,
    size: Size = Size.Large,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally
) {
    val (mainStyle, transcriptionStyle) = with(MaterialTheme.typography) {
        when (size) {
            Size.Large -> titleLarge to bodyMedium
            Size.Small -> titleSmall to bodySmall
            Size.SmallEmphasized -> titleMediumEmphasized to bodySmallEmphasized
        }
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        //placeholder
        Column(horizontalAlignment = horizontalAlignment) {
            Text(text = "", style = mainStyle)
            Text(text = "", style = transcriptionStyle)
        }
        //actual texts
        Column(horizontalAlignment = horizontalAlignment) {
            Text(text = main, style = mainStyle)
            transcription?.let {
                Text(text = it, style = transcriptionStyle)
            }
        }
    }
}