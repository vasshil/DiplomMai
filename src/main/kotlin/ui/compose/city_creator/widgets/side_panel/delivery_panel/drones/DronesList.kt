package ui.compose.city_creator.widgets.side_panel.delivery_panel.drones

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import model.FlyMap
import model.drone.Drone

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DronesList(
    modifier: Modifier = Modifier,
    flyMap: FlyMap,
    onFocusChange: (focused: Boolean, droneId: Long) -> Unit,
    onDroneChanged: (Drone) -> Unit,
) {

    LazyColumn(
        modifier = modifier.background(Color.White),
    ) {

        flyMap.drones.forEachIndexed { i, drone ->

            item {

                DroneItem(
                    modifier = Modifier
                        .onPointerEvent(PointerEventType.Enter) { event ->
                            onFocusChange(true, drone.id)
                        }
                        .onPointerEvent(PointerEventType.Exit) {
                            onFocusChange(false, drone.id)
                        },
                    drone = drone,
                    onChanged = {
                        onDroneChanged(drone)
                    },
                )

            }

        }

        item { Spacer(modifier = Modifier.height(70.dp)) }

    }

}