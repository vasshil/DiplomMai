package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.launch
import model.FlyMap
import model.landscape.Building
import model.landscape.NoFlyZone
import ui.compose.city_creator.widgets.side_panel.landscape.LandscapeList
import ui.compose.city_creator.widgets.topbar.CreatorModeEnum
import ui.compose.city_creator.CreatorViewModel
import ui.compose.city_creator.Scheme2DMode
import ui.compose.city_creator.widgets.InfoDialog
import ui.compose.city_creator.widgets.side_panel.delivery_panel.DeliveryPanel
import ui.compose.city_creator.widgets.side_panel.delivery_panel.cargos.CargoPoints
import ui.compose.city_creator.widgets.side_panel.delivery_panel.drones.DroneStartPoint
import ui.compose.city_creator.widgets.topbar.SimulationMode
import ui.compose.city_creator.widgets.topbar.TopBar
import ui.compose.common.DIVIDER_COLOR
import ui.compose.common.SchemeView

@Composable
@Preview
fun CityScheme2D(viewModel: CreatorViewModel) {

    val scope = rememberCoroutineScope()

    val flyMap by viewModel.flyMapFlow.collectAsState()

//    println("collected fly map ${flyMap.buildings.toTypedArray().contentToString()}")

    var schemeMode by remember { mutableStateOf(Scheme2DMode.VIEW) }

    var simulationMode by remember { mutableStateOf(SimulationMode.PAUSE) }

    var editorMode by remember { mutableStateOf(CreatorModeEnum.NONE) }

    var newBuildingId: Long? by remember { mutableStateOf(null) }

    var newNFZId: Long? by remember { mutableStateOf(null) }

    var focusedBuildingId by remember { mutableLongStateOf(-1) }

    var focusedNFZId by remember { mutableLongStateOf(-1) }

    var focusedDroneId by remember { mutableLongStateOf(-1) }

    var mousePosition by remember { mutableStateOf(Offset.Zero) }


    var droneStartPoint by remember { mutableStateOf<DroneStartPoint>(DroneStartPoint.Idle) }

    // точки отправки и назначения для нового груза
    var cargoPoints by remember { mutableStateOf<CargoPoints>(CargoPoints.Idle) }


    var showWrongFileDialog by remember { mutableStateOf(false) }

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
                    flyMap.saveToFile(scope)
                },
                loadCity = {
                    scope.launch {
                        val file = FileKit.openFilePicker(
                            type = FileKitType.File(listOf("txt"))
                        )
                        println("file read ${file?.path}")
                        file?.let {
                            FlyMap.loadFromFile(it.path)?.let { loadedCity ->
                                viewModel.setCity(loadedCity)
                            } ?: kotlin.run {
                                showWrongFileDialog = true
                            }
                        }

                    }
                },
                onSimulationModeChange = { mode ->
                    simulationMode = mode
                    if (mode == SimulationMode.PLAY) {
                        viewModel.droneRoutingManager.start()
                    } else {
                        viewModel.droneRoutingManager.stop()
                    }
                },
                onEditorModeChange = { mode ->
                    editorMode = mode
                    if (mode != CreatorModeEnum.ADD_BUILDING) {
                        if (newBuildingId != null) {
                            newBuildingId = null
                            viewModel.removeLastBuilding()
                        }
                    }
                    if (mode != CreatorModeEnum.ADD_NO_FLY_ZONE) {
                        if (newNFZId != null) {
                            newNFZId = null
                            viewModel.removeLastNFZ()
                        }
                    }
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
                    flyMap = flyMap,
                    viewModel = viewModel,
                    cityCreatorMode = editorMode,
                    isEditorMode = schemeMode == Scheme2DMode.EDITOR,
                    focusedBuildingId = focusedBuildingId,
                    focusedNFZId = focusedNFZId,
                    focusedDroneId = focusedDroneId,
                    onClick = {
                        if (schemeMode == Scheme2DMode.EDITOR) {
                            when (editorMode) {
                                CreatorModeEnum.ADD_BUILDING -> {
                                    if (newBuildingId == null) {
                                        newBuildingId = viewModel.newBuilding()
                                    }
                                    newBuildingId?.let {
                                        viewModel.addBuildingGroundPoint(it, mousePosition.x, mousePosition.y)
                                    }
                                    if (flyMap.lastBuilding()?.groundCoords?.isNotEmpty() == true &&
                                        flyMap.lastBuilding()?.groundCoords?.first()?.x == mousePosition.x &&
                                        flyMap.lastBuilding()?.groundCoords?.first()?.z == mousePosition.y) {

                                        newBuildingId?.let {
                                            viewModel.finishBuilding(it)
                                        }

                                        newBuildingId = null
//                                        flyMap.createGraphAtHeight()
                                    }

                                }
                                CreatorModeEnum.ADD_CHARGE_STATION -> {
                                    val nearestVertex = flyMap.getNearestVertex(mousePosition)
                                    nearestVertex?.let {
                                        it.isChargeStation = !it.isChargeStation
                                    }
                                }
                                CreatorModeEnum.ADD_NO_FLY_ZONE -> {
                                    if (newNFZId == null) {
                                        newNFZId = viewModel.newNFZ()
                                    }
                                    newNFZId?.let {
                                        viewModel.addNFZGroundPoint(it, mousePosition.x, mousePosition.y)
                                    }
                                    if (flyMap.lastNFZ()?.groundCoords?.isNotEmpty() == true &&
                                        flyMap.lastNFZ()?.groundCoords?.first()?.x == mousePosition.x &&
                                        flyMap.lastNFZ()?.groundCoords?.first()?.z == mousePosition.y) {

                                        newNFZId?.let {
                                            viewModel.finishNFZ(it)
                                        }
                                        newNFZId = null
//                                        flyMap.createGraphAtHeight()
                                    }
                                }
                                CreatorModeEnum.REMOVE -> {
                                    if (focusedBuildingId != -1L) {
                                        viewModel.removeBuilding(focusedBuildingId)
//                                        flyMap.createGraphAtHeight()
                                    }
                                    if (focusedNFZId != -1L) {
                                        viewModel.removeNFZ(focusedNFZId)
//                                        flyMap.createGraphAtHeight()
                                    }

                                }
                                CreatorModeEnum.NONE -> {}
                            }
                        } else {
                            // TODO: добавить
                        }

                    },
                    droneStartPoint = droneStartPoint,
                    onDroneStartPointChanged = { newDroneStartPoint ->
                        println("newDroneStartPoint $newDroneStartPoint")
                        droneStartPoint = newDroneStartPoint
                    },
                    cargoPoints = cargoPoints,
                    onCargoPointsChanged = { newCargoPoints ->
                        cargoPoints = newCargoPoints
                    }
                ) { position, pressed ->
                    mousePosition = position
                    if (editorMode != CreatorModeEnum.ADD_BUILDING && editorMode != CreatorModeEnum.ADD_NO_FLY_ZONE && editorMode != CreatorModeEnum.ADD_CHARGE_STATION) {
                        focusedNFZId = flyMap.checkNFZPointAt(position.x, position.y)?.id ?: -1
                        focusedBuildingId = flyMap.checkBuildingPointAt(position.x, position.y)?.id ?: -1

                        // приоритет фокуса на бесполетной зоне
                        if (focusedNFZId != -1L) focusedBuildingId = -1

                    }

                }

                Divider(color = DIVIDER_COLOR, modifier = Modifier.width(1.dp).fillMaxHeight())

                when (schemeMode) {
                    Scheme2DMode.EDITOR -> {
                        LandscapeList(
                            modifier = Modifier.width(250.dp).fillMaxHeight(),
                            flyMap = flyMap,
                            onBuildingFocusChange = { focused, id ->
                                focusedBuildingId = if (!focused) -1 else id
                            },
                            onNFZFocusChange = { focused, id ->
                                focusedNFZId = if (!focused) -1 else id
                            },
                            onBuildingChanged = { changedBuilding ->
                                viewModel.updateBuilding(changedBuilding)
                            },
                            onBuildingFinished = {
                                newBuildingId?.let {
                                    viewModel.finishBuilding(it)
                                }
                                newBuildingId = null
//                                flyMap.createGraphAtHeight()
                            },
                            onNFZChanged = { changedNFZ ->
                                viewModel.updateNoFlyZone(changedNFZ)
//                                flyMap.createGraphAtHeight()
                            },
                            onNFZFinished = {
                                newNFZId?.let {
                                    viewModel.finishNFZ(it)
                                }
                                newNFZId = null
                            }
                        )
                    }
                    Scheme2DMode.VIEW -> {
                        DeliveryPanel(
                            flyMap = flyMap,
                            addDrone = { newDrone ->
                                viewModel.addDrone(newDrone)
                            },
                            addCargo = { newCargo ->
                                viewModel.addCargo(newCargo)
                            },
                            droneStartPoint = droneStartPoint,
                            onDroneStartPointChanged = { newStartPoint ->
                                droneStartPoint = newStartPoint
                            },
                            cargoPoints = cargoPoints,
                            onCargoPointsChanged = { newCargoPoints ->
                                cargoPoints = newCargoPoints
                            },
                            onDroneFocusChanged = { id ->
                                focusedDroneId = id
                            }
                        )
                    }

                }


                if (showWrongFileDialog) {
                    InfoDialog("Некорректный файл сохранения") {
                        showWrongFileDialog = false // закрываем
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


