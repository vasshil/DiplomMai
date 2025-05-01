package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import model.City
import ui.compose.common.SchemeView

@Composable
@Preview
fun App() {

    var city by remember { mutableStateOf<City?>(null) }

    LaunchedEffect(Unit) {
        city = City.loadFromFile("city1234.txt")
    }

    MaterialTheme {
        city?.let {
            SchemeView(
                modifier = Modifier.size(width = 600.dp, height = 500.dp),
                city = it
            )
        }

    }

}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}

