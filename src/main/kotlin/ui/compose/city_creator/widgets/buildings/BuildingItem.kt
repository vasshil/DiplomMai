package ui.compose.city_creator.widgets.buildings

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
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import model.landscape.Building
import ui.compose.common.BUILDING_COLOR
import ui.compose.common.BUTTON_COLOR
import ui.compose.common.TEXT_FIELD_COLOR

@Composable
fun BuildingItem(
    modifier: Modifier = Modifier,
    building: Building,
    onChanged: (Building) -> Unit,
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
                BuildingPreviewImage(Modifier.align(Alignment.Center), building)
            }

            Column {
                Text(
                    modifier = Modifier.padding(start = 5.dp),
                    text = "Здание ${building.id}",
                    fontSize = 10.sp
                )
                Text(
                    modifier = Modifier.padding(start = 5.dp),
                    text = "Точек: ${building.groundCoords.size - 1}",
                    fontSize = 10.sp
                )
                if (building.groundCoords.first() != building.groundCoords.last()) {
                    // полигон здания закончен
                    Button(
                        modifier = Modifier.padding(start = 5.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = BUTTON_COLOR),
                        onClick = {
                            onFinished()
                        }) {
                        Text("Готово")
                    }

                } else {

                    Row(
                        modifier = Modifier.padding(top = 5.dp).background(color = TEXT_FIELD_COLOR, shape = CircleShape),
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 3.dp),
                        ) {
                            BasicTextField(
                                modifier = Modifier.align(Alignment.CenterEnd).padding(start = 5.dp),
                                value = building.height.toString(),
                                onValueChange = {
                                    it.toFloatOrNull()?.let { height ->
                                        onChanged(building.copy(height = height))
                                    }
                                },
                            )
                            Text(
                                text = "м",
                                fontSize = 14.sp,
                                color = Color(159, 159, 159),
                                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 10.dp)
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
private fun BuildingPreviewImage(modifier: Modifier, building: Building) {

    if (building.groundCoords.isEmpty()) return

    val minX by derivedStateOf {
        building.groundCoords.minBy { it.x }.x
    }

    val minY by derivedStateOf {
        building.groundCoords.minBy { it.z }.z
    }

    val maxX by derivedStateOf {
        building.groundCoords.maxBy { it.x }.x
    }

    val maxY by derivedStateOf {
        building.groundCoords.maxBy { it.z }.z
    }

    val buildingWidth by derivedStateOf {
        with(maxX - minX) { if (this == 0f) 1f else this }
    }

    val buildingHeight by derivedStateOf {
        with(maxY - minY) { if (this == 0f) 1f else this }
    }

    var scaledPoints by remember { mutableStateOf(mutableListOf<Offset>()) }

    val baseSize = 72f
    val canvasSizeDp by derivedStateOf {
        if (buildingWidth > buildingHeight) Offset(baseSize, baseSize * buildingHeight / buildingWidth)
        else Offset(baseSize * buildingWidth / buildingHeight, baseSize)
    }

    Canvas(
        modifier = modifier.size(width = canvasSizeDp.x.dp, canvasSizeDp.y.dp)//.padding(10.dp)
    ) {
        scaledPoints = building.groundCoords.map {
            Offset(
                (it.x - minX) / buildingWidth * canvasSizeDp.x.dp.toPx(),
                (it.z - minY) / buildingHeight * canvasSizeDp.y.dp.toPx()
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
            color = BUILDING_COLOR,
            style = Fill
        )

    }

}