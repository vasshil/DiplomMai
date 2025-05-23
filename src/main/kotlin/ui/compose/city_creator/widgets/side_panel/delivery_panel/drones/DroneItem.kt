package ui.compose.city_creator.widgets.side_panel.delivery_panel.drones

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
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

        Box {
            Row(
                modifier = Modifier.align(Alignment.TopStart),
            ) {

                Column {

                    Icon(
                        painter = painterResource("icons/drone.png"),
                        contentDescription = null,
                        tint = DRONE_ICON_COLOR,
                        modifier = Modifier.padding(12.dp).size(36.dp).background(DRONE_ICON_BG_COLOR),
                    )
                    Icon(
                        imageVector = drone.getBatteryIcon(),
                        contentDescription = null,
                        tint = drone.getBatteryIconColor(),
                        modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally)
                    )

                    Text(
                        text = "${drone.batteryLevel}%",
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 3.dp, bottom = 5.dp),
                        lineHeight = 12.sp
                    )
                }


                Column(
                    modifier = Modifier.padding(start = 5.dp, bottom = 5.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {

                    Text(
                        text = "Дрон id = ${drone.id}",
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 5.dp)
                    )
                    Text(
                        text = "Координаты:\n x = ${drone.currentPosition.x}\n y = ${drone.currentPosition.y}\n z = ${drone.currentPosition.z}",
                        fontSize = 10.sp,
                        lineHeight = 12.sp
                    )

                    Text(
                        text = "Загруженность: ${drone.currentCargoMass} из ${drone.maxCargoCapacityMass} кг",
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )

                    Box(
                        modifier = Modifier
                            .background(color = drone.status.color, shape = RoundedCornerShape(3.dp))
                            .padding(start = 4.dp, end = 4.dp)
                    ) {
                        Text(
                            text = drone.status.localization,
                            fontSize = 10.sp,
                            lineHeight = 15.sp,
                            color = Color.White,
                            modifier = Modifier
                            .align(Alignment.Center)

//                                .padding(top = 5.dp, end = 5.dp)
                        )
                    }


                }

            }

        }


        Divider(color = DIVIDER_COLOR, modifier = Modifier.height(1.dp))

    }

}
