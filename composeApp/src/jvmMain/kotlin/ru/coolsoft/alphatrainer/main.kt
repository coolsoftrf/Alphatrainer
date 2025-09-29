package ru.coolsoft.alphatrainer

import alphatrainer.composeapp.generated.resources.Res
import alphatrainer.composeapp.generated.resources.icon_ico
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.compose.resources.painterResource
import ru.coolsoft.alphatrainer.nonwasm.databasePath
import ru.coolsoft.alphatrainer.nonwasm.setupKoin

fun main() = application {
    setupKoin{
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