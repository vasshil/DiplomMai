package ui.compose.city_creator.widgets.side_panel.landscape.nfz

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import model.landscape.Building
import model.landscape.NoFlyZone
import ui.compose.common.*

@Composable
fun NoFlyZoneItem(
    modifier: Modifier = Modifier,
    nfz: NoFlyZone,
    onChanged: (NoFlyZone) -> Unit,
    onFinished: () -> Unit,
) {

    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier,
        ) {

            Box(
                modifier = Modifier.size(96.dp)
            ) {
                NoFlyZonePreviewImage(Modifier.align(Alignment.Center), nfz)
            }

            Column {
                Text(
                    modifier = Modifier.padding(start = 5.dp),
                    text = "Бесполетная зона ${nfz.id}",
                    fontSize = 10.sp
                )
                Text(
                    modifier = Modifier.padding(start = 5.dp),
                    text = "Точек: ${nfz.groundCoords.size - 1}",
                    fontSize = 10.sp
                )
                if (nfz.groundCoords.size > 2) {
                    if (nfz.groundCoords.first() != nfz.groundCoords.last() || nfz.groundCoords.isEmpty()) {
                        // полигон зоны не закончен
                        Button(
                            modifier = Modifier.padding(start = 5.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = BUTTON_COLOR),
                            onClick = {
                                onFinished()
                            }) {
                            Text("Готово")
                        }

                    } else {
                        Row {

                            Checkbox(
                                checked = nfz.isActive,
                                onCheckedChange = {
                                    println("nfz check curr ${nfz.isActive} / new ${!nfz.isActive}")
                                    onChanged(nfz.copy(isActive = !nfz.isActive))
                                                  },
                                colors = CheckboxDefaults.colors(
                                    checkmarkColor = Color.White,
                                    checkedColor = Color.Black,
                                    uncheckedColor = Color.Black
                                )
                            )

                            Text(
                                text = "Включено",
                                color = Color.Black,
                                fontSize = 12.sp,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }

                    }

                }

            }

        }

        Divider(color = Color.Black, modifier = Modifier.height(1.dp))

    }


}

@Composable
private fun NoFlyZonePreviewImage(modifier: Modifier, nfz: NoFlyZone) {

    if (nfz.groundCoords.isEmpty()) return

    val minX by derivedStateOf {
        nfz.groundCoords.minBy { it.x }.x
    }

    val minY by derivedStateOf {
        nfz.groundCoords.minBy { it.z }.z
    }

    val maxX by derivedStateOf {
        nfz.groundCoords.maxBy { it.x }.x
    }

    val maxY by derivedStateOf {
        nfz.groundCoords.maxBy { it.z }.z
    }

    val nfzWidth by derivedStateOf {
        with(maxX - minX) { if (this == 0f) 1f else this }
    }

    val nfzHeight by derivedStateOf {
        with(maxY - minY) { if (this == 0f) 1f else this }
    }

    var scaledPoints by remember { mutableStateOf(mutableListOf<Offset>()) }

    val baseSize = 72f
    val canvasSizeDp by derivedStateOf {
        if (nfzWidth > nfzHeight) Offset(baseSize, baseSize * nfzHeight / nfzWidth)
        else Offset(baseSize * nfzWidth / nfzHeight, baseSize)
    }

    Canvas(
        modifier = modifier.size(width = canvasSizeDp.x.dp, canvasSizeDp.y.dp)//.padding(10.dp)
    ) {
        scaledPoints = nfz.groundCoords.map {
            Offset(
                (it.x - minX) / nfzWidth * canvasSizeDp.x.dp.toPx(),
                (it.z - minY) / nfzHeight * canvasSizeDp.y.dp.toPx()
            )
        }.toMutableList()

        if (scaledPoints.isEmpty()) return@Canvas
//        println(building.groundCoords.toTypedArray().contentToString() + " ${canvasSizeDp.toPx()} ${scaledPoints.toTypedArray().contentToString()}")

        val path = Path().apply {
            this.moveTo(scaledPoints.first().x, scaledPoints.first().y)
            for (i in 1 until scaledPoints.size) {
                lineTo(
                    scaledPoints[i].x,
                    scaledPoints[i].y
                )
            }
        }

        drawPath(
            path = path,
            color = NO_FLY_ZONE_FILL_COLOR,
            style = Fill
        )

        drawPath(
            path = path,
            color = NO_FLY_ZONE_BORDER_COLOR,
            style = Stroke(
                width = 2f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(10f, 10f), // 10px линия, 10px пробел
                    phase = 0f              // сдвиг, можно анимировать
                )
            )
        )

    }

}