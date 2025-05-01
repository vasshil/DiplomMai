package ui.compose.city_creator.widgets.delivery_panel.drones

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import model.drone.Drone
import ui.compose.common.*

@Composable
fun DroneItem(
    modifier: Modifier = Modifier,
    drone: Drone,
    onChanged: (Drone) -> Unit,
) {

    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier,
        ) {

            Icon(
                imageVector = Icons.Rounded.Flight,
                contentDescription = null,
                tint = DRONE_ICON_COLOR,
                modifier = Modifier.padding(12.dp).size(48.dp).background(DRONE_ICON_BG_COLOR),
            )

            Column(
                modifier = Modifier.padding(start = 5.dp)
            ) {

                Text(
                    text = "Дрон id = ${drone.id}",
                    fontSize = 10.sp
                )
                Text(
                    text = "Координаты:\n x = ${drone.currentPosition.x}\n y = ${drone.currentPosition.y}\n z = ${drone.currentPosition.z}\n ",
                    fontSize = 10.sp
                )

                Row {
                    Icon(
                        imageVector = drone.getBatteryIcon(),
                        contentDescription = null,
                        tint = drone.getBatteryIconColor(),
                        modifier = Modifier.size(24.dp).align(Alignment.CenterVertically)
                    )

                    Text(
                        text = "Уровень заряда: ${drone.batteryLevel}%",
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.CenterVertically).padding(start = 2.dp)
                    )
                }
                Text(
                    text = "Загруженность: ${drone.currentCargoMass} из ${drone.maxCargoCapacityMass} кг",
                    fontSize = 10.sp
                )

            }

        }

        Divider(color = Color.Black, modifier = Modifier.height(1.dp))

    }


}
