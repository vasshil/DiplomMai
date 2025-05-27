package ui.compose.city_creator.widgets.side_panel.delivery_panel.cargos

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import model.cargo.Cargo
import model.drone.Drone
import ui.compose.common.*

@Composable
fun CargoItem(
    modifier: Modifier = Modifier,
    cargo: Cargo,
    onChanged: (Cargo) -> Unit,
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
                        painter = painterResource("icons/ic_box.png"),
                        contentDescription = null,
                        tint = CARGO_ICON_COLOR,
                        modifier = Modifier.padding(12.dp).size(36.dp),
                    )
                }


                Column(
                    modifier = Modifier.padding(start = 5.dp, bottom = 5.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {

                    Text(
                        text = "Масса: ${cargo.weight} кг",
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        modifier = Modifier.padding(bottom = 5.dp, top = 5.dp)
                    )

                    Row {

                        Text(
                            text = "Старт:\n x = ${cargo.startVertex.x}\n y = ${cargo.startVertex.y}\n z = ${cargo.startVertex.z}",
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            modifier = Modifier.weight(1f),
                        )

                        Text(
                            text = "Конец:\n x = ${cargo.destination.x}\n y = ${cargo.destination.y}\n z = ${cargo.destination.z}",
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            modifier = Modifier.weight(1f),
                        )

                    }

                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .background(color = cargo.status.color, shape = RoundedCornerShape(3.dp))
                            .padding(start = 4.dp, end = 4.dp,)
                    ) {
                        Text(
                            text = cargo.status.localization,
                            fontSize = 10.sp,
                            lineHeight = 15.sp,
                            color = Color.White,
                            modifier = Modifier
                            .align(Alignment.Center)
                        )
                    }


                }

            }

        }


        Divider(color = DIVIDER_COLOR, modifier = Modifier.height(1.dp))

    }

}
