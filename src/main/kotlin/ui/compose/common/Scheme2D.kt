package ui.compose.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.jme3.math.Vector3f
import core.distanceBetween
import core.findShortestPathDijkstra
import core.pathLength
import model.City
import model.graph.Vertex
import ui.compose.city_creator.CityCreatorMode
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Scheme2D(
    modifier: Modifier = Modifier,
    city: City,
    cityCreatorMode: CityCreatorMode = CityCreatorMode.NONE,
    isEditorMode: Boolean = false,
    drawBaseGraph: Boolean = true,
    drawFullGraph: Boolean = true,
    focusedBuildingId: Long = -1,
    onClick: (() -> Unit) = {},
    showScaleButtons: Boolean = true,
    onMouseAction: (position: Offset, pressed: Boolean) -> Unit = { _, _ -> },
) {

    var mouseCoordinate by remember { mutableStateOf(Offset.Zero) }

    var scale by remember { mutableFloatStateOf(10f) } // Масштаб для перевода координат в пиксели
    val gridStep by remember { derivedStateOf { 10 * scale } }  // Шаг сетки
    var offset by remember { mutableStateOf(Offset(gridStep, gridStep)) }  // Смещение в одну клетку в левом верхнем углу

    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        scale *= zoomChange
//        rotation += rotationChange
        offset += offsetChange
//        println("$scale, offsetChange:$offset")
//        println("$zoomChange, offsetChange:$offsetChange, rotationChange:$rotationChange")
    }

    // Состояния для хранения двух выбранных вершин
    var selectedVertex1 by remember { mutableStateOf<Vertex?>(null) }
    var selectedVertex2 by remember { mutableStateOf<Vertex?>(null) }
    var shortestPath by remember { mutableStateOf<List<Vertex>>(emptyList()) }

    Box(
        modifier = modifier
    ) {

        Canvas(
            modifier = Modifier.fillMaxSize()
                .background(color = SCHEME_BACKGROUND_COLOR)
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->

                        onClick()

                        if (isEditorMode) return@detectTapGestures

                        // Переводим координаты клика в координаты графа с учетом смещения и масштаба
                        val clickedPosition = Vector3f(
                            (tapOffset.x - offset.x) / scale,
                            10f,
                            (tapOffset.y - offset.y) / scale,
                        )

                        // Определяем ближайшую вершину к месту клика
                        val nearestVertex = city.graph.vertices.minByOrNull {
                            distanceBetween(it.position, clickedPosition)
                        }

                        nearestVertex?.let { vertex ->
                            if (selectedVertex1 == null) {
                                selectedVertex1 = vertex
                            } else if (selectedVertex2 == null && vertex != selectedVertex1) {
                                selectedVertex2 = vertex
                                // Находим кратчайший путь между двумя вершинами
                                shortestPath = findShortestPathDijkstra(selectedVertex1!!, selectedVertex2!!)
                                val len = pathLength(shortestPath)
                                println("Длина пути ${len} м, потраченный заряд: ${len / 10}%")
                            } else {
                                // Сбрасываем выбор, если кликнули еще раз (начать с начала)
                                selectedVertex1 = null
                                selectedVertex2 = null
                                shortestPath = emptyList()
                            }
                        }
                    }
                }
                .onPointerEvent(PointerEventType.Move) { event ->
                    val x = ((event.changes.first().position.x - offset.x) / scale).roundToInt()
                    val y = ((event.changes.first().position.y - offset.y) / scale).roundToInt()
                    onMouseAction(Offset(x.toFloat(), y.toFloat()), event.changes.first().pressed)
                    mouseCoordinate = Offset(x * scale + offset.x, y * scale + offset.y)
                }
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, rotation ->
                        println("$centroid, $zoom, $rotation $pan")
                        scale *= zoom
                    }
                }
                .transformable(state = state)


        ) {
            // Рисуем сетку
            for (x in 0..(size.width.toInt() / gridStep.toInt()) + 1) {
                drawLine(
                    color = MESH_COLOR,
                    start = Offset((offset.x % gridStep + x.toFloat() * gridStep), 0f),
                    end = Offset((offset.x % gridStep + x.toFloat() * gridStep), size.height),
                    strokeWidth = 1f
                )
            }
            for (y in 0..(size.height.toInt() / gridStep.toInt()) + 1) {
                drawLine(
                    color = MESH_COLOR,
                    start = Offset(0f, (y.toFloat() * gridStep + offset.y % gridStep)),
                    end = Offset(size.width, (y.toFloat() * gridStep + offset.y % gridStep)),
                    strokeWidth = 1f
                )
            }

            // draw axis
            drawLine(
                color = AXIS_COLOR,
                start = Offset(offset.x, 0f),
                end = Offset(offset.x, size.height),
                strokeWidth = 2f
            )
            drawLine(
                color = AXIS_COLOR,
                start = Offset(0f, offset.y),
                end = Offset(size.width, offset.y),
                strokeWidth = 2f
            )

            // Рисуем здания
            city.buildings.forEach { building ->

                val path = Path().apply {
                    this.moveTo(building.groundCoords.first().x * scale + offset.x, building.groundCoords.first().z * scale + offset.y)
                    building.groundCoords.forEach { coordinate ->
                        lineTo(
                            coordinate.x * scale + offset.x,
                            coordinate.z * scale + offset.y
                        )
                    }
                }

                drawPath(
                    path = path,
                    color = if (focusedBuildingId == building.id) {
                        when(cityCreatorMode) {
                            CityCreatorMode.REMOVE -> DELETE_FOCUSED_BUILDING_COLOR
                            else -> FOCUSED_BUILDING_COLOR
                        }
                    } else BUILDING_COLOR,
                    style = Fill
                )

            }

            // Рисуем ребра графа
            city.graph.edges.forEach { edge ->
//            if (!(edge.isBase && !drawBaseGraph)) return@forEach
                val startX = edge.vertex1.position.x * scale + offset.x
                val startY = edge.vertex1.position.z * scale + offset.y
                val endX = edge.vertex2.position.x * scale + offset.x
                val endY = edge.vertex2.position.z * scale + offset.y

                drawLine(
                    color = EDGE_COLOR,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 2f
                )
            }

            // Рисуем вершины графа
            city.graph.vertices.forEach { vertex ->
                if (!drawBaseGraph && !drawFullGraph) return@forEach

                val x = vertex.position.x * scale + offset.x
                val y = vertex.position.z * scale + offset.y

                if (vertex.isBaseStation && vertex.isDestination) {

                    drawCircle(
                        DESTINATION_COLOR,
                        radius = 6f,
                        center = Offset(x, y)
                    )
                    drawArc(
                        color = BASE_STATION_COLOR,
                        startAngle = -90f,
                        sweepAngle = 180f,
                        style = Fill,
                        useCenter = false,
                        size = Size(12f, 12f),
                        topLeft = Offset(x - 6, y - 6),
                    )

                } else {
                    val color = if (vertex.isBaseStation) BASE_STATION_COLOR else if (vertex.isDestination) DESTINATION_COLOR else KEY_POINT_COLOR
                    drawCircle(
                        color = color,
                        radius = 6f,
                        center = Offset(x, y)
                    )
                }


            }

            if (isEditorMode) {
                // место курсора
                drawLine(MOUSE_POINT_COLOR, Offset(mouseCoordinate.x - 10, mouseCoordinate.y), Offset(mouseCoordinate.x + 10, mouseCoordinate.y), 1f)
                drawLine(MOUSE_POINT_COLOR, Offset(mouseCoordinate.x, mouseCoordinate.y - 10), Offset(mouseCoordinate.x, mouseCoordinate.y + 10), 1f)
            }
            else {

                // Рисуем кратчайший путь, если он есть
                if (shortestPath.isNotEmpty()) {
                    println(shortestPath)
                    shortestPath.zipWithNext { v1, v2 ->
                        val startX = v1.position.x * scale + offset.x
                        val startY = v1.position.z * scale + offset.y
                        val endX = v2.position.x * scale + offset.x
                        val endY = v2.position.z * scale + offset.y

                        drawLine(
                            color = SHORTEST_PATH_COLOR,
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = 4f
                        )
                    }
                }

                selectedVertex1?.let {
                    val x = it.position.x * scale + offset.x
                    val y = it.position.z * scale + offset.y

                    drawCircle(
                        color = BASE_STATION_COLOR,
                        radius = 9f,
                        center = Offset(x, y)
                    )
                }

                selectedVertex2?.let {
                    val x = it.position.x * scale + offset.x
                    val y = it.position.z * scale + offset.y

                    drawCircle(
                        color = DESTINATION_COLOR,
                        radius = 9f,
                        center = Offset(x, y)
                    )
                }

            }
        }

        if (showScaleButtons) {

            Column(
                modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp)
            ) {

                IconButton(
                    modifier = Modifier.size(48.dp).background(color = Color.Black, shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)),
                    onClick = {
                        scale += 0.5f
                    }
                ) {

                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null, tint = Color.White)

                }

                IconButton(
                    modifier = Modifier.size(48.dp).background(color = Color.Black, shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)),
                    onClick = {
                        if (scale > 1) scale -= 0.5f
                    }
                ) {

                    Icon(imageVector = Icons.Rounded.Remove, contentDescription = null, tint = Color.White)

                }

            }
        }

    }


}