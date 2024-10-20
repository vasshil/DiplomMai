package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.jme3.math.Vector3f
import model.City
import model.graph.Edge
import model.graph.Graph3D
import model.landscape.Building
import ui.compose.Scheme2D
import kotlin.random.Random

@Composable
@Preview
fun CityCreator() {

    val city by remember {
        mutableStateOf(City())
    }


    MaterialTheme {

        TopBar(

        )


        Scheme2D(
            modifier = Modifier.size(width = 600.dp, height = 500.dp),
            city = city
        )
    }

}

@Composable
fun TopBar(modifier: Modifier = Modifier) {

    Row {



    }

}

@Composable
fun ActionButton(modifier: Modifier = Modifier, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {

    Button(
        modifier = modifier
            .size(48.dp)
            .padding(8.dp),
        onClick = { onCheckedChange(!checked) },
        enabled = TODO(),
        interactionSource = TODO(),
        elevation = TODO(),
        shape = TODO(),
        border = TODO(),
        colors = TODO(),
        contentPadding = TODO(),
        content = TODO(),
    )

}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}

