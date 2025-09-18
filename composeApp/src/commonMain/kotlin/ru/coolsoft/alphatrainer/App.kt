package ru.coolsoft.alphatrainer

import alphatrainer.composeapp.generated.resources.Res
import alphatrainer.composeapp.generated.resources.alphabet
import alphatrainer.composeapp.generated.resources.back
import alphatrainer.composeapp.generated.resources.dictionary
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.TypeSpecimen
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
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
import org.jetbrains.compose.resources.stringResource
import ru.coolsoft.alphatrainer.data.LocalizedString
import ru.coolsoft.alphatrainer.screens.DEFAULT_PAIR_COUNT
import ru.coolsoft.alphatrainer.screens.FlipcardsScreen
import ru.coolsoft.alphatrainer.screens.LanguagesScreen
import ru.coolsoft.alphatrainer.screens.TrainingModeScreen
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
data class ModeChooser(
    val forLanguage: String,
    val alphabets: List<LocalizedString>,
    val scriptLanguages: Map<String, List<LocalizedString>>,
    val dictionaries: List<LocalizedString>,
    val dictionaryToTranscriptsMap: Map<String, List<LocalizedString>>
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
    val source: Section,
    val category: String,
    val learntAlphabet: String,
    val scriptAlphabet: String,
    val limitPairs: Int
) {
    companion object {
        val typeMap = mapOf(
            typeMapItem<Section>()
        )
    }
}

fun flipcards(
    source: Section,
    alphabetId: String,
    scriptLanguageId: String,
    pairCount: Int
) = Flipcards(
    source,
    parentLanguageFor(alphabetId),
    alphabetId,
    scriptLanguageId,
    pairCount
)

fun flipcardRequest(flipcards: Flipcards) = FlipcardRequest(
    flipcards.category,
    flipcards.learntAlphabet,
    flipcards.scriptAlphabet,
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
                                        contentDescription = stringResource(Res.string.back)
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
                    NavHost(
                        navController,
                        LanguageChooser,
                        Modifier.fillMaxSize()
                    ) {
                        composable<LanguageChooser> {
                            ShadedBox(Modifier.fillMaxSize()) {
                                LanguagesScreen(Modifier.matchParentSize(), ::onLanguageSelected)
                            }
                        }
                        composable<ModeChooser>(typeMap = ModeChooser.typeMap) { entry ->
                            val (forLanguage, alphabets, transcriptionLanguages, dictionaries, dictionaryToTranscriptsMap) = entry.toRoute<ModeChooser>()
                            val selectedSection =
                                rememberSaveable { mutableStateOf(Section.Alphabet) }

                            NavContainer({ BarContent(selectedSection) }) {
                                logger().i(TAG, "recomposing navContainer")
                                TrainingModeScreen(
                                    forLanguage,
                                    alphabets,
                                    transcriptionLanguages,
                                    pairCountState,
                                    selectedSection,
                                    dictionaries,
                                    dictionaryToTranscriptsMap,
                                    ::onAlphabetSelected,
                                    ::onDictionarySelected
                                )
                            }
                        }
                        composable<Flipcards>(typeMap = Flipcards.typeMap) { entry ->
                            val route = entry.toRoute<Flipcards>()
                            ShadedBox {
                                FlipcardsScreen(
                                    flipcardRequest(route),
                                    route.source
                                ) {
                                    navController.popBackStack(LanguageChooser::class, false)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShadedBox(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.systemBarsPadding()) {
        content()
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

@Composable
private fun NavContainer(buttons: @Composable () -> Unit, content: @Composable () -> Unit) =
    if (LocalWindowInfo.current.containerSize.run { width > height })
        NavigationRailContainer(buttons, content)
    else
        BottomBarContainer(buttons, content)


@Composable
private fun NavigationRailContainer(
    buttons: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    ShadedBox {
        content()
        NavigationRail {
            Spacer(Modifier.weight(1f))
            buttons()
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun BottomBarContainer(buttons: @Composable () -> Unit, content: @Composable () -> Unit) {
    Column {
        ShadedBox(Modifier.weight(1f)) {
            content()
        }
        BottomAppBar(
            Modifier.align(Alignment.CenterHorizontally).wrapContentHeight(),
            MaterialTheme.colorScheme.surface,
        ) {
            Row(
                Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                buttons()
            }
        }
    }
}

@Composable
private fun BarContent(selectedSection: MutableState<Section>) {
    NavigationRailItem(
        selectedSection.value == Section.Alphabet,
        { selectedSection.value = Section.Alphabet },
        {
            Icon(
                Icons.Outlined.TypeSpecimen,
                contentDescription = stringResource(Res.string.alphabet)
            )
        },
        label = {
            Text(stringResource(Res.string.alphabet))
        }
    )
    NavigationRailItem(
        selectedSection.value == Section.Dictionary,
        { selectedSection.value = Section.Dictionary },
        {
            Icon(
                Icons.Outlined.Translate,
                contentDescription = stringResource(Res.string.dictionary)
            )
        },
        label = {
            Text(stringResource(Res.string.dictionary))
        }
    )
}