package ru.coolsoft.alphatrainer

import alphatrainer.composeapp.generated.resources.Res
import alphatrainer.composeapp.generated.resources.icon_ico
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.compose.resources.painterResource
import ru.coolsoft.alphatrainer.nonwasm.Koin
import ru.coolsoft.alphatrainer.nonwasm.databasePath

fun main() = application {
    Koin.setupKoin{
        databasePath(dbAssetUri)
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "AlphaTrainer",
        icon = painterResource(Res.drawable.icon_ico)
    ) {
        App()
    }
}