package ui.compose.city_creator.widgets.side_panel.delivery_panel

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jme3.math.Vector3f
import kotlinx.coroutines.launch
import model.FlyMap
import model.cargo.Cargo
import model.drone.Drone
import ui.compose.city_creator.widgets.side_panel.delivery_panel.cargos.CargoPoints
import ui.compose.city_creator.widgets.side_panel.delivery_panel.cargos.CargosList
import ui.compose.city_creator.widgets.side_panel.delivery_panel.cargos.CreateCargoDialog
import ui.compose.city_creator.widgets.side_panel.delivery_panel.drones.CreateDroneDialog
import ui.compose.city_creator.widgets.side_panel.delivery_panel.drones.DronesList
import ui.compose.common.DELIVERY_PANEL_BAR_SELECTED_COLOR
import ui.compose.common.DELIVERY_PANEL_BAR_UNSELECTED_COLOR

@Composable
fun DeliveryPanel(
    modifier: Modifier = Modifier,
    flyMap: FlyMap,
    addDrone: (Drone) -> Unit,
    addCargo: (Cargo) -> Unit,
    cargoPoints: CargoPoints,
    onCargoPointsChanged: (CargoPoints) -> Unit,
) {

    var deliveryPanelMode by remember { mutableStateOf(DeliveryPanelMode.DRONES) }

    var showDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = cargoPoints) {

        println("cargoPoints: $cargoPoints")

        if (cargoPoints is CargoPoints.Waiting2) {
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()

                val result = snackbarHostState
                    .showSnackbar(
                        message = "Выберите точку назначения",
                        actionLabel = "Отмена",
                        duration = SnackbarDuration.Indefinite
                    )
                when (result) {
                    SnackbarResult.ActionPerformed -> {
                        onCargoPointsChanged(CargoPoints.Idle)
                    }
                    SnackbarResult.Dismissed -> {
//                    onCargoPointsChanged(CargoPoints.Idle)
                    }
                }
            }
        } else if (cargoPoints is CargoPoints.TwoPointSelected) {
            snackbarHostState.currentSnackbarData?.dismiss()
            showDialog = true
        }

    }

    Scaffold(
        modifier = Modifier.width(250.dp).fillMaxHeight(),
        bottomBar = {
            BottomNavigation(
                windowInsets = BottomNavigationDefaults.windowInsets,
                backgroundColor = Color.Black
            ) {
                DeliveryPanelMode.entries.forEach { mode ->
                    val selected = mode == deliveryPanelMode
                    BottomNavigationItem(
                        selected = selected,
                        onClick = {
                            deliveryPanelMode = mode
                        },
                        icon = {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(mode.iconPath),
                                tint = if (selected) DELIVERY_PANEL_BAR_SELECTED_COLOR else DELIVERY_PANEL_BAR_UNSELECTED_COLOR,
                                contentDescription = null
                            )
                        },
                        label = {
                            Text(
                                text = mode.localization,
                                color = if (selected) DELIVERY_PANEL_BAR_SELECTED_COLOR else DELIVERY_PANEL_BAR_UNSELECTED_COLOR
                            )
                        }
                    )
                }
            }

        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (deliveryPanelMode == DeliveryPanelMode.DRONES) {
                        showDialog = true
                    } else if (deliveryPanelMode == DeliveryPanelMode.CARGOS) {
                        if (cargoPoints == CargoPoints.Idle) {
                            scope.launch {
                                onCargoPointsChanged(CargoPoints.Waiting1)
                                val result = snackbarHostState
                                    .showSnackbar(
                                        message = "Выберите точку отправления",
                                        actionLabel = "Отмена",
                                        duration = SnackbarDuration.Indefinite
                                    )
                                when (result) {
                                    SnackbarResult.ActionPerformed -> {
                                        onCargoPointsChanged(CargoPoints.Idle)
                                    }
                                    SnackbarResult.Dismissed -> {
//                                        onCargoPointsChanged(CargoPoints.Idle)
                                    }
                                }
                            }

                        }

                    }

                },
                icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null, tint = Color.White) },
                text = { Text(text = "Добавить", color = Color.White) },
                backgroundColor = Color.Black
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { padding ->

        if (deliveryPanelMode == DeliveryPanelMode.DRONES) {

            DronesList(
                modifier = Modifier.padding(padding),
                flyMap = flyMap,
                onFocusChange = { focused, id ->
//                                focusedBuildingId = if (!focused) -1 else id
                },
                onDroneChanged = { changedDrone ->
//                                viewModel.updateBuilding(changedBuilding)
                }
            )

        }
        else if (deliveryPanelMode == DeliveryPanelMode.CARGOS) {
            CargosList(
                modifier = Modifier.padding(padding),
                flyMap = flyMap,
                onFocusChange = { focused, id ->

                },
                onCargoChanged = { changedCargo ->

                },
            )
        }

    }


    if (showDialog) {
        if (deliveryPanelMode == DeliveryPanelMode.DRONES) {
            CreateDroneDialog(
                newId = flyMap.nextDroneId(),
                onConfirm = { newDrone ->
                    addDrone(newDrone)
                    showDialog = false
                },
                onDismiss = {
                    showDialog = false
                }
            )
        } else if (deliveryPanelMode == DeliveryPanelMode.CARGOS) {
            CreateCargoDialog(
                onConfirm = { newCargo ->
                    addCargo(newCargo)
                    showDialog = false
                    onCargoPointsChanged(CargoPoints.Idle)
                },
                onDismiss = {
                    showDialog = false
                    onCargoPointsChanged(CargoPoints.Idle)
                },
                cargoPoints as CargoPoints.TwoPointSelected
            )
        }

    }

}