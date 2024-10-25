package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import model.City
import model.landscape.Building
import ui.compose.city_creator.BuildingList
import ui.compose.city_creator.CityCreatorMode
import ui.compose.city_creator.TopBar
import ui.compose.common.BASE_STATION_COLOR
import ui.compose.common.BUILDING_COLOR
import ui.compose.common.DESTINATION_COLOR
import ui.compose.common.Scheme2D

@Composable
@Preview
fun CityCreator() {

    var editorMode by remember { mutableStateOf(CityCreatorMode.NONE) }

    println(editorMode)

    val city by remember {
        mutableStateOf(City())
    }

    var newBuilding: Building? by remember { mutableStateOf(null) }

    var mousePosition by remember { mutableStateOf(Offset.Zero) }

    MaterialTheme {

        Column(
            modifier = Modifier
        ) {

            TopBar(
                modifier = Modifier,
                mousePosition = mousePosition,
                editorMode = editorMode,
            ) { mode ->
                editorMode = mode
            }

            Row(
                modifier = Modifier,
            ) {

                Scheme2D(
                    modifier = Modifier.width(width = 600.dp).weight(1f).fillMaxHeight(),
                    city = city,
                    editorMode = true,
                    drawBaseGraph = true,
                    onClick = {
                        when (editorMode) {
                            CityCreatorMode.ADD_BUILDING -> {
                                if (newBuilding == null) {
                                    newBuilding = city.newBuilding()
                                }
                                if (newBuilding?.groundCoords?.isNotEmpty() == true &&
                                    newBuilding?.groundCoords?.first()?.x == mousePosition.x &&
                                    newBuilding?.groundCoords?.first()?.z == mousePosition.y) {

                                    newBuilding?.finish()
                                    newBuilding = null
                                    city.createGraphAtHeight()
                                }
                                newBuilding?.addGroundPoint(mousePosition.x, mousePosition.y)
                            }
                            CityCreatorMode.ADD_BASE_STATION -> {
                                val nearestVertex = city.getNearestVertex(mousePosition)
                                nearestVertex?.let {
                                    it.isBaseStation = !it.isBaseStation
                                }
                            }
                            CityCreatorMode.ADD_DESTINATION -> {
                                val nearestVertex = city.getNearestVertex(mousePosition)
                                nearestVertex?.let {
                                    it.isDestination = !it.isDestination
                                }
                            }
                            CityCreatorMode.REMOVE -> TODO()
                            CityCreatorMode.NONE -> {}
                        }
                    }
                ) { position, pressed ->
                    mousePosition = position
                }

                Divider(color = Color.Black, modifier = Modifier.width(1.dp).fillMaxHeight())

                BuildingList(
                    modifier = Modifier.width(250.dp).fillMaxHeight(),
                    city = city,
                ) {
                    newBuilding?.finish()
                    newBuilding = null
                    city.createGraphAtHeight()
                }

            }

        }

    }

}


fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        CityCreator()
    }
}


