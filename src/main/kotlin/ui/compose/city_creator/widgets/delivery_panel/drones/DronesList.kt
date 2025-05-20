package ui.compose.city_creator.widgets.delivery_panel.drones

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import model.City
import model.drone.Drone

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DronesList(
    modifier: Modifier = Modifier,
    city: City,
    onFocusChange: (focused: Boolean, droneId: Long) -> Unit,
    onDroneCreated: (Drone) -> Unit,
    onDroneChanged: (Drone) -> Unit,
) {

    var showCreateDroneDialog by remember { mutableStateOf(false) }

    Box {

        LazyColumn(
            modifier = modifier.background(Color.White),
        ) {

            city.drones.forEachIndexed { i, drone ->

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

            item { Spacer(modifier = Modifier.height(60.dp)) }

        }

        Button(
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 10.dp, end = 10.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black),
            shape = RoundedCornerShape(50.dp),
            onClick = {
                showCreateDroneDialog = true
            }
        ) {
            Row {

                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)

                Text(text = "Добавить", color = Color.White, modifier = Modifier.align(Alignment.CenterVertically).padding(start = 5.dp))

            }

        }

        if (showCreateDroneDialog) {
            CreateDroneDialog(
                newId = city.nextDroneId(),
                onConfirm = {
                    onDroneCreated(it)
                    showCreateDroneDialog = false
                },
                onDismiss = {
                    showCreateDroneDialog = false
                }
            )
        }

    }

}