package ru.coolsoft.alphatrainer.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.coolsoft.alphatrainer.data.LocalizedString

typealias LanguagesDataProvider = suspend (uiLanguageId: String) -> List<LocalizedString>

class LanguagesViewModel(private val dataProvider: LanguagesDataProvider) : ViewModel() {
    companion object {
        val DATA_PROVIDER = CreationExtras.Key<LanguagesDataProvider>()
    }

    private val _locale = MutableStateFlow<String?>(null)
    private val _languages = MutableStateFlow<List<LocalizedString>?>(null)
    val languages = _languages.asStateFlow()

    fun onLaunched(locale: String) {
        (if (locale == _locale.value) _languages.value else null) ?: reload(locale)
    }

    fun reload(locale: String) {
        _locale.value = locale
        viewModelScope.launch {
            _languages.value = dataProvider(locale)
        }
    }
}

fun languagesViewModelFactory(): ViewModelProvider.Factory {
    return viewModelFactory {
        initializer {
            get(LanguagesViewModel.DATA_PROVIDER)?.let {
                LanguagesViewModel(it)
            } ?: throw NoSuchElementException("DATA_PROVIDER factory extra")
        }
    }
}