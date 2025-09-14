package ru.coolsoft.alphatrainer

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.koin.android.ext.koin.androidContext
import ru.coolsoft.alphatrainer.nonwasm.Koin
import ru.coolsoft.alphatrainer.nonwasm.databasePath

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Koin.setupKoin{
            androidContext(applicationContext)
            databasePath(dbAssetUri)
        }

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}

@Preview
@Preview(name = "night", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AppAndroidPreview() {
    App()
}