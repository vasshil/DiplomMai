package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.jme3.math.Vector3f
import model.City
import model.graph.Edge
import model.graph.Graph3D
import model.landscape.Building
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Polygon
import ui.compose.city_creator.CityCreatorViewModel
import ui.compose.common.Scheme2D

@Composable
@Preview
fun App() {

    var city by remember { mutableStateOf<City?>(null) }

    LaunchedEffect(Unit) {
        city = City.loadFromFile("city1234.txt")
    }

    MaterialTheme {
        city?.let {
            Scheme2D(
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

