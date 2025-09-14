package ru.coolsoft.alphatrainer.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.coolsoft.alphatrainer.shared.FlipcardRequest
import ru.coolsoft.alphatrainer.logger
import kotlin.time.Clock.System.now
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

enum class CardState {
    IDLE,
    SELECTED,
    ERROR,
    MATCHED,
    REMOVED
}

enum class GameState {
    INIT,
    STARTED,
    OVER
}

typealias FlipDataProvider = suspend (request: FlipcardRequest) -> List<Flipcard>

data class Flipcard(
    val key: String,
    val title: String,
    var state: CardState = CardState.IDLE
)

class FlipcardsViewModel(private val dataProducer: FlipDataProvider) : ViewModel() {
    companion object {
        val DATA_PROVIDER = CreationExtras.Key<FlipDataProvider>()
        val TAG = FlipcardsViewModel::class.simpleName
    }

    private val _flipcards = MutableStateFlow<List<Flipcard>?>(null)
    val flipcards = _flipcards.asStateFlow()

    @OptIn(ExperimentalTime::class)
    private var _timerStart: Instant? = null
    private val _timerValue = MutableStateFlow("")
    val timerValue = _timerValue.asStateFlow()

    private val _slipCount = MutableStateFlow(0)
    val slipCount = _slipCount.asStateFlow()

    private val _gameState = MutableStateFlow(GameState.INIT)
    val gameState = _gameState.asStateFlow()

    private var _timerJob: Job? = null

    fun onLaunched(flipcardRequest: FlipcardRequest) {
        _flipcards.value ?: reload(flipcardRequest)
    }

    @OptIn(ExperimentalTime::class)
    fun reload(flipcardRequest: FlipcardRequest) {
        logger().i(TAG, "reloading model for request $flipcardRequest")
        _timerJob?.cancel()
        _gameState.value = GameState.INIT
        _flipcards.value = null

        _timerJob = viewModelScope.launch {
            _flipcards.value = dataProducer(flipcardRequest)
            _gameState.value = GameState.STARTED

            _timerStart = now()
            while (true) {
                (now() - _timerStart!!).let {
                    val seconds = (it.inWholeSeconds % 60).toString().padStart(2, '0')
                    _timerValue.value = "${it.inWholeMinutes}:${seconds}"
                }
                delay(200)
            }
        }
    }

    fun onCardClicked(index: Int) {
        val card = _flipcards.value!![index]
        logger().i(TAG, "card $index clicked")
        if(card.state == CardState.REMOVED){
            logger().i(TAG, "card $index click cancelled")
            return
        }

        _flipcards.value!!.find { it.state == CardState.SELECTED }
            ?.also { selected ->
                _flipcards.update { cards ->
                    val targetState = when {
                        selected === card -> CardState.IDLE
                        selected.key == card.key -> {
                            if (cards!!.count { c -> c.state != CardState.REMOVED } == 2) {
                                logger().i(TAG, "stopping timer")
                                _timerJob!!.cancel()
                                _gameState.value = GameState.OVER
                            }
                            CardState.MATCHED
                        }

                        else -> {
                            _slipCount.value++
                            CardState.ERROR
                        }
                    }
                    logger().i(TAG, "moving to $targetState")
                    cards!!.map { c ->
                        when (c) {
                            card -> card.copy(state = targetState)
                            selected -> selected.copy(state = targetState)
                            else -> c
                        }
                    }
                }
            } ?: _flipcards.update { cards ->
            logger().i(TAG, "selecting card @${index}: ${card.title}")
            cards!!.map { c ->
                if (c === card) card.copy(state = CardState.SELECTED) else c
            }
        }
    }

    fun onColorApplied() {
        logger().i(TAG, "handling color applied")
        _flipcards.update { cards ->
            cards!!.map { c ->
                when (c.state) {
                    CardState.MATCHED -> CardState.REMOVED
                    CardState.ERROR -> CardState.IDLE
                    else -> null
                }?.let {
                    c.copy(state = it)
                } ?: c
            }
        }
    }
}

fun flipcardsViewModelFactory(): ViewModelProvider.Factory {
    return viewModelFactory {
        initializer {
            get(FlipcardsViewModel.DATA_PROVIDER)?.let {
                FlipcardsViewModel(it)
            } ?: throw NoSuchElementException("DATA_PROVIDER factory extra")
        }
    }
}