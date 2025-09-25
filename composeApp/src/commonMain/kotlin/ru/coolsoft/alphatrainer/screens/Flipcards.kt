package ru.coolsoft.alphatrainer.screens

import alphatrainer.composeapp.generated.resources.Res
import alphatrainer.composeapp.generated.resources.home_icon
import alphatrainer.composeapp.generated.resources.reload_icon
import alphatrainer.composeapp.generated.resources.slip_count
import alphatrainer.composeapp.generated.resources.timer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonDefaults.TextButtonContentPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider
import ru.coolsoft.alphatrainer.Section
import ru.coolsoft.alphatrainer.data.alphabetFlipcardData
import ru.coolsoft.alphatrainer.data.dictionaryFlipcardData
import ru.coolsoft.alphatrainer.data.mockFlipCards
import ru.coolsoft.alphatrainer.models.CardState
import ru.coolsoft.alphatrainer.models.Flipcard
import ru.coolsoft.alphatrainer.models.FlipcardsViewModel
import ru.coolsoft.alphatrainer.models.GameState
import ru.coolsoft.alphatrainer.models.flipcardsViewModelFactory
import ru.coolsoft.alphatrainer.shared.FlipcardRequest


@Composable
fun FlipcardsScreen(
    flipcardRequest: FlipcardRequest,
    source: Section,
    flipcardsModel: FlipcardsViewModel = viewModel(
        factory = flipcardsViewModelFactory(),
        extras = MutableCreationExtras().apply {
            set(
                FlipcardsViewModel.DATA_PROVIDER, when (source) {
                    Section.Alphabet -> ::alphabetFlipcardData
                    Section.Dictionary -> ::dictionaryFlipcardData
                }
            )
        }
    ),
    onGoHome: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LaunchedEffect(Unit) {
            flipcardsModel.onLaunched(flipcardRequest)
        }

        val timerValue by flipcardsModel.timerValue.collectAsState()
        val flipcards by flipcardsModel.flipcards.collectAsState()
        val slipCount by flipcardsModel.slipCount.collectAsState()
        val gameState by flipcardsModel.gameState.collectAsState()

        flipcards
            ?.also { cards ->
                val timerFontScale by animateFloatAsState(
                    if (gameState == GameState.OVER) 3f else 1f
                )
                val slipFontScale by animateFloatAsState(
                    if (gameState == GameState.OVER) 2f else 1f
                )

                Text(
                    stringResource(Res.string.timer, timerValue),
                    Modifier
                        .padding(20.dp)
                        .graphicsLayer(
                            scaleX = timerFontScale,
                            scaleY = timerFontScale,
                            transformOrigin = TransformOrigin(0.5f, -1f)
                        ),
                    MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    stringResource(Res.string.slip_count, slipCount),
                    Modifier
                        .padding(20.dp)
                        .graphicsLayer(
                            scaleX = slipFontScale,
                            scaleY = slipFontScale,
                            transformOrigin = TransformOrigin(0.5f, -2.7f)
                        ),
                    MaterialTheme.colorScheme.onBackground,
                )
                when (gameState) {
                    GameState.OVER -> FinalActions(flipcardRequest, flipcardsModel, onGoHome)
                    else -> GameField(cards, flipcardsModel, source)
                }
            }
            ?: CircularProgressIndicator(Modifier.padding(20.dp))
    }
}

@Composable
@Preview
fun GameField(
    @PreviewParameter(CardsPreviewProvider::class)
    cards: List<Flipcard>,
    @PreviewParameter(FlipcardsViewModelProvider::class)
    flipcardsModel: FlipcardsViewModel,
    source: Section
) {
    LazyVerticalGrid(
        columns = when (source) {
            Section.Alphabet -> GridCells.FixedSize(70.dp)
            Section.Dictionary -> GridCells.Fixed(2)
        },
        modifier = Modifier
            .wrapContentWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(
            10.dp,
            Alignment.CenterHorizontally
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(cards.size) { index ->
            val showContent = remember(index) {
                MutableTransitionState(false).apply { targetState = true }
            }

            val state = cards[index].state

            val cardColor by animateColorAsState(
                targetValue = when (state) {
                    CardState.IDLE, CardState.REMOVED -> MaterialTheme.colorScheme.background
                    CardState.ERROR -> MaterialTheme.colorScheme.errorContainer
                    CardState.SELECTED -> MaterialTheme.colorScheme.inversePrimary
                    CardState.MATCHED -> MaterialTheme.colorScheme.surfaceContainerHigh
                },
                animationSpec = when (state) {
                    CardState.ERROR, CardState.MATCHED -> snap()
                    else -> tween()
                },
                finishedListener = { flipcardsModel.onColorApplied() }
            )

            AnimatedVisibility(
                showContent,
                enter = scaleIn(tween(delayMillis = 300)),
            ) {
                val scaleX by animateFloatAsState(
                    targetValue = if (state == CardState.REMOVED) 0f else 1f,
                    animationSpec = tween()
                )
                OutlinedButton(
                    onClick = { flipcardsModel.onCardClicked(index) },
                    shape = RoundedCornerShape(20),
                    modifier = Modifier
                        .scale(scaleX, 1f)
                        .run {
                            if (source == Section.Alphabet)
                                aspectRatio(1f)
                            else
                                this
                        },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cardColor,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    contentPadding = TextButtonContentPadding
                ) {
                    Text(cards[index].title)
                }
            }
        }
    }
}

@Composable
@Preview
fun FinalActions(
    flipcardRequest: FlipcardRequest,
    @PreviewParameter(FlipcardsViewModelProvider::class)
    model: FlipcardsViewModel,
    onGoHome: () -> Unit
) {
    val visibility = MutableTransitionState(false).apply { targetState = true }
    AnimatedVisibility(visibility, enter = slideInVertically(initialOffsetY = { it })) {
        Row(
            Modifier
                .padding(vertical = 80.dp)
                .fillMaxHeight(),
            Arrangement.spacedBy(10.dp),
            Alignment.CenterVertically
        ) {
            OutlinedButton({ model.reload(flipcardRequest) }) {
                Text(stringResource(Res.string.reload_icon))
            }
            OutlinedButton(onGoHome) {
                Text(stringResource(Res.string.home_icon))
            }
        }
    }
}


private class CardsPreviewProvider : PreviewParameterProvider<List<Flipcard>> {
    override val values = sequenceOf(mockFlipCards)
}

private class FlipcardsViewModelProvider : PreviewParameterProvider<FlipcardsViewModel> {
    override val values = sequenceOf(
        flipcardsViewModelFactory().create(
            FlipcardsViewModel::class,
            MutableCreationExtras().apply {
                set(FlipcardsViewModel.DATA_PROVIDER, ::alphabetFlipcardData)
            })
    )
}