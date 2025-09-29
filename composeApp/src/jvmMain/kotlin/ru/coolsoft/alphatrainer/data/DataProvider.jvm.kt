package ru.coolsoft.alphatrainer.data

import ru.coolsoft.alphatrainer.javashared.DataProviderImpl
import ru.coolsoft.alphatrainer.shared.IRawDataProvider

actual val RawDataProvider: IRawDataProvider = DataProviderImpl
