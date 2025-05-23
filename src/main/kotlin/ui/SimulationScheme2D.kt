package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import model.FlyMap
import model.landscape.Building
import ui.compose.city_creator.widgets.side_panel.buildings.BuildingList
import ui.compose.city_creator.widgets.topbar.CreatorModeEnum
import ui.compose.city_creator.CreatorViewModel
import ui.compose.city_creator.Scheme2DMode
import ui.compose.city_creator.widgets.side_panel.delivery_panel.DeliveryPanel
import ui.compose.city_creator.widgets.topbar.SimulationMode
import ui.compose.city_creator.widgets.topbar.TopBar
import ui.compose.common.DIVIDER_COLOR
import ui.compose.common.SchemeView

@Composable
@Preview
fun CityScheme2D(viewModel: CreatorViewModel) {

    val city by viewModel.cityFlow.collectAsState()
    println("collected city $city")

    var schemeMode by remember { mutableStateOf(Scheme2DMode.VIEW) }

    var simulationMode by remember { mutableStateOf(SimulationMode.PAUSE) }

    var editorMode by remember { mutableStateOf(CreatorModeEnum.NONE) }

    var newBuilding: Building? by remember { mutableStateOf(null) }

    var focusedBuildingId by remember { mutableLongStateOf(-1) }

    var mousePosition by remember { mutableStateOf(Offset.Zero) }

    MaterialTheme {

        Column(
            modifier = Modifier
        ) {

            TopBar(
                modifier = Modifier,
                mousePosition = mousePosition,
                simulationMode = simulationMode,
                editorMode = editorMode,
                saveCity = {
                    city.saveToFile("city1234.txt")
                },
                loadCity = {
                    FlyMap.loadFromFile("city1234.txt")?.let { loadedCity ->
                        viewModel.setCity(loadedCity)
                    }
                },
                onSimulationModeChange = {
                    mode -> simulationMode = mode
                },
                onEditorModeChange = { mode ->
                    editorMode = mode
                },
                schemeMode = schemeMode,
                onSchemeModeChange = { mode ->
                    schemeMode = mode
                }
            )

            Divider(color = DIVIDER_COLOR, modifier = Modifier.height(1.dp).fillMaxWidth())

            Row(
                modifier = Modifier,
            ) {

                SchemeView(
                    modifier = Modifier.width(width = 600.dp).weight(1f).fillMaxHeight(),
                    city = city,
                    cityCreatorMode = editorMode,
                    isEditorMode = schemeMode == Scheme2DMode.EDITOR,
                    drawBaseGraph = true,
                    focusedBuildingId = focusedBuildingId,
                    onClick = {
                        if (schemeMode == Scheme2DMode.EDITOR) {
                            when (editorMode) {
                                CreatorModeEnum.ADD_BUILDING -> {
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
                                CreatorModeEnum.ADD_BASE_STATION -> {
                                    val nearestVertex = city.getNearestVertex(mousePosition)
                                    nearestVertex?.let {
                                        it.isBaseStation = !it.isBaseStation
                                    }
                                }
                                CreatorModeEnum.ADD_DESTINATION -> {
                                    val nearestVertex = city.getNearestVertex(mousePosition)
                                    nearestVertex?.let {
                                        it.isDestination = !it.isDestination
                                    }
                                }
                                CreatorModeEnum.REMOVE -> {
                                    if (focusedBuildingId != -1L) {
                                        city.removeBuilding(focusedBuildingId)
                                        city.createGraphAtHeight()
                                    }

                                }
                                CreatorModeEnum.NONE -> {}
                            }
                        } else {
                            // TODO: добавить
                        }

                    }
                ) { position, pressed ->
                    mousePosition = position
                    if (editorMode != CreatorModeEnum.ADD_BUILDING) {
                        focusedBuildingId = city.checkPointAt(position.x, position.y)?.id ?: -1
                    }

                }

                Divider(color = DIVIDER_COLOR, modifier = Modifier.width(1.dp).fillMaxHeight())


                when (schemeMode) {
                    Scheme2DMode.EDITOR -> {
                        BuildingList(
                            modifier = Modifier.width(250.dp).fillMaxHeight(),
                            city = city,
                            onFocusChange = { focused, id ->
                                focusedBuildingId = if (!focused) -1 else id
                            },
                            onBuildingChanged = { changedBuilding ->
                                viewModel.updateBuilding(changedBuilding)
                            }
                        ) {
                            newBuilding?.finish()
                            newBuilding = null
                            city.createGraphAtHeight()
                        }
                    }
                    Scheme2DMode.VIEW -> {
                        DeliveryPanel(
                            modifier = Modifier,
                            city = city,
                            addDrone = { newDrone ->
                                viewModel.addDrone(newDrone)
                            },
                            addCargo = {

                            }
                        )
                    }


                }

            }

        }

    }

}


fun main() = application {

    val viewModel = CreatorViewModel()

    Window(
        onCloseRequest = ::exitApplication,
        state = WindowState(
            size = DpSize(1300.dp, 768.dp),
            position = WindowPosition(Alignment.Center),
        )
    ) {
        CityScheme2D(viewModel)
    }
}


