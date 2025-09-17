package ru.coolsoft.alphatrainer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.write
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ru.coolsoft.alphatrainer.data.LocalizedString
import ru.coolsoft.alphatrainer.screens.AlphabetsScreen
import ru.coolsoft.alphatrainer.screens.DEFAULT_PAIR_COUNT
import ru.coolsoft.alphatrainer.screens.FlipcardsScreen
import ru.coolsoft.alphatrainer.screens.LanguagesScreen
import ru.coolsoft.alphatrainer.shared.FlipcardRequest
import ru.coolsoft.alphatrainer.theme.AlphaTrainerTheme
import kotlin.reflect.typeOf


@Serializable
object LanguageChooser

inline fun <reified T> typeMapItem() = typeOf<T>() to object :
    NavType<T>(false) {
    override fun put(
        bundle: SavedState,
        key: String,
        value: T
    ) {
        bundle.write { putString(key, serializeAsValue(value)) }
    }

    override fun get(
        bundle: SavedState,
        key: String
    ): T? {
        return bundle.read { parseValue(getString(key)) }
    }

    override fun parseValue(value: String): T {
        return Json.decodeFromString(value)
    }

    override fun serializeAsValue(value: T): String {
        return Json.encodeToString(value)
    }
}

@Serializable
data class AlphabetChooser(
    val alphabets: List<LocalizedString>,
    val transcriptionLanguages: Map<String, List<LocalizedString>>
) {
    companion object {
        val typeMap = mapOf(
            typeMapItem<List<LocalizedString>>(),
            typeMapItem<Map<String, List<LocalizedString>>>()
        )
    }
}

@Serializable
data class Flipcards(
    val language: String,
    val learntLanguage: String,
    val scriptLanguage: String,
    val limitPairs: Int
)

fun flipcards(alphabetId: String, scriptLanguageId: String, pairCount: Int) = Flipcards(
    baseLanguageFor(alphabetId),
    alphabetId,
    scriptLanguageId,
    pairCount
)

fun flipcardRequest(flipcards: Flipcards) = FlipcardRequest(
    flipcards.language,
    flipcards.learntLanguage,
    flipcards.scriptLanguage,
    flipcards.limitPairs,
)

val LocalAppLocalization = compositionLocalOf { DEFAULT_LANGUAGE }

private const val TAG = "App"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    AlphaTrainerTheme {
        CompositionLocalProvider(
            LocalAppLocalization provides appLocale()
        ) {
            val navController = rememberNavController()
            val backStackEntry by navController.currentBackStackEntryAsState()
            logger().i(TAG, "BS Entry: ${backStackEntry?.destination}")
            Scaffold(
                Modifier.imePadding(),
                topBar = {
                    TopAppBar(
                        title = { },
                        navigationIcon = {
                            navController.previousBackStackEntry?.let {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Localized description"
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            ) {
                val pairCountState = rememberTextFieldState(DEFAULT_PAIR_COUNT.toString())
                NavigationHandler(navController, pairCountState.text.toString().toInt()) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .systemBarsPadding()
                    ) {
                        NavHost(
                            navController,
                            LanguageChooser,
                            Modifier.fillMaxSize()
                        ) {
                            composable<LanguageChooser> {
                                LanguagesScreen(::onLanguageSelected)
                            }
                            composable<AlphabetChooser>(typeMap = AlphabetChooser.typeMap) { entry ->
                                val (alphabets, transcriptionLanguages) = entry.toRoute<AlphabetChooser>()
                                AlphabetsScreen(
                                    alphabets,
                                    transcriptionLanguages,
                                    pairCountState,
                                    ::onAlphabetSelected
                                )
                            }
                            composable<Flipcards> { entry ->
                                FlipcardsScreen(flipcardRequest(entry.toRoute<Flipcards>())) {
                                    navController.popBackStack(LanguageChooser::class, false)
                                }
                            }
                        }

                        //Shades
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.background, Color.Transparent
                                        )
                                    )
                                )
                        )
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent, MaterialTheme.colorScheme.background
                                        )
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}
