package ru.coolsoft.alphatrainer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider


private data class Modifiers(
    val fillMax: Modifier.() -> Modifier,
    val thickness: Modifier.(value: Dp) -> Modifier,
    val gradient: (colors: List<Color>) -> Brush,
    val alignment2: Alignment
)

private class OrientationProvider : PreviewParameterProvider<Orientation> {
    override val values = sequenceOf(Orientation.Horizontal/*, Orientation.Vertical*/)
}

private class ComposableContentProvider :
    PreviewParameterProvider<@Composable BoxScope.() -> Unit> {
    override val values: Sequence<@Composable (BoxScope.() -> Unit)> =
        sequenceOf(@Composable { Text("test") })
}

@Composable
@Preview
fun ShadedBox(
    modifier: Modifier = Modifier,
    @PreviewParameter(OrientationProvider::class)
    orientation: Orientation = Orientation.Vertical,
    span: Dp = 20.dp,
    @PreviewParameter(ComposableContentProvider::class)
    content: @Composable BoxScope.() -> Unit
) {
    val (fillMax, thickness, gradient, alignment2) = remember(orientation) {
        when (orientation) {
            Orientation.Vertical -> Modifiers(
                Modifier::fillMaxWidth,
                Modifier::height,
                Brush::verticalGradient,
                Alignment.BottomCenter
            )

            Orientation.Horizontal -> Modifiers(
                Modifier::fillMaxHeight,
                Modifier::width,
                Brush::horizontalGradient,
                Alignment.CenterEnd
            )
        }
    }

    Box(
        modifier
            .systemBarsPadding()
    ) {
        content()
        Box(Modifier.matchParentSize()) {
            Box(
                Modifier
                    .fillMax()
                    .thickness(span)
                    .background(
                        gradient(
                            listOf(
                                MaterialTheme.colorScheme.background, Color.Transparent
                            )
                        )
                    )
            )
            Box(
                Modifier
                    .fillMax()
                    .thickness(span)
                    .align(alignment2)
                    .background(
                        gradient(
                            listOf(
                                Color.Transparent, MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )
        }
    }
}