package ui.compose.city_creator

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp
import model.landscape.Building
import ui.compose.common.BUILDING_COLOR

@Composable
fun BuildingItem(modifier: Modifier = Modifier, building: Building) {

    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier,
        ) {

            BuildingPreview(building)

            Column {
                Text(
                    modifier = Modifier.padding(start = 5.dp),
                    text = "Здание ${building.id}"
                )
                Text(
                    modifier = Modifier.padding(start = 5.dp),
                    text = "${building.groundCoords.size} точек"
                )
            }

        }

        Divider(color = Color.Black, modifier = Modifier.height(1.dp))

    }


}
@Composable
private fun BuildingPreview(building: Building) {

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

    val canvasSizeDp = 96.dp

    Canvas(
        modifier = Modifier.size(canvasSizeDp).padding(10.dp)
    ) {
        scaledPoints = building.groundCoords.map {
            Offset(
                it.x / buildingWidth * canvasSizeDp.toPx(),
                it.z / buildingHeight * canvasSizeDp.toPx()
            )
        }.toMutableList()

        if (scaledPoints.isEmpty()) return@Canvas
        println(building.groundCoords.toTypedArray().contentToString() + " ${canvasSizeDp.toPx()} ${scaledPoints.toTypedArray().contentToString()}")

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