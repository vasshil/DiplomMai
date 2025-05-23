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
import model.FlyMap
import model.cargo.Cargo
import model.drone.Drone
import ui.compose.city_creator.widgets.side_panel.delivery_panel.drones.CreateDroneDialog
import ui.compose.city_creator.widgets.side_panel.delivery_panel.drones.DronesList
import ui.compose.common.DELIVERY_PANEL_BAR_SELECTED_COLOR
import ui.compose.common.DELIVERY_PANEL_BAR_UNSELECTED_COLOR

@Composable
fun DeliveryPanel(
    modifier: Modifier = Modifier,
    city: FlyMap,
    addDrone: (Drone) -> Unit,
    addCargo: (Cargo) -> Unit,
) {

    var deliveryPanelMode by remember { mutableStateOf(DeliveryPanelMode.DRONES) }

    var showDialog by remember { mutableStateOf(false) }

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
                onClick = { showDialog = true },
                icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null, tint = Color.White) },
                text = { Text(text = "Добавить", color = Color.White) },
                backgroundColor = Color.Black
            )
        }
    ) { padding ->

        DronesList(
            modifier = Modifier.padding(padding),
            city = city,
            onFocusChange = { focused, id ->
//                                focusedBuildingId = if (!focused) -1 else id
            },
            onDroneChanged = { changedDrone ->
//                                viewModel.updateBuilding(changedBuilding)
            }
        )

    }


    if (showDialog) {
        CreateDroneDialog(
            newId = city.nextDroneId(),
            onConfirm = { newDrone ->
                addDrone(newDrone)
                showDialog = false
            },
            onDismiss = {
                showDialog = false
            }
        )
    }

}