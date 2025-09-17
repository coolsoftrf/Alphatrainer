package ru.coolsoft.alphatrainer.screens

import alphatrainer.composeapp.generated.resources.Res
import alphatrainer.composeapp.generated.resources.im_learning
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.resources.stringResource
import ru.coolsoft.alphatrainer.LocalAppLocalization
import ru.coolsoft.alphatrainer.data.trainableLanguages
import ru.coolsoft.alphatrainer.models.LanguagesViewModel
import ru.coolsoft.alphatrainer.models.languagesViewModelFactory


typealias OnLanguageSelectedListener = (languageId: String) -> Unit

@Composable
fun LanguagesScreen(
    onLanguageSelected: OnLanguageSelectedListener,
    languagesModel: LanguagesViewModel = viewModel(
        factory = languagesViewModelFactory(),
        extras = MutableCreationExtras().apply {
            set(LanguagesViewModel.DATA_PROVIDER, ::trainableLanguages)
        }
    )
) {
    val locale = LocalAppLocalization.current
    LaunchedEffect(Unit) {
        languagesModel.onLaunched()
    }

    val languages by languagesModel.languages.collectAsState()
    val scrollState = rememberScrollState()
    Column(
        Modifier
            .verticalScroll(scrollState)
            .fillMaxHeight(),
        Arrangement.Center,
        Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.im_learning),
            modifier = Modifier.padding(20.dp),
            style = MaterialTheme.typography.titleMedium
        )

        languages?.run {
            map { l ->
                OutlinedButton(
                    { onLanguageSelected(l.id) },
                    Modifier.padding(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = l.name, style = MaterialTheme.typography.titleLarge)
                        l.transcription(locale)?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        } ?: CircularProgressIndicator()
    }
}