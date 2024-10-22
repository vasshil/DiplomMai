package ui.compose.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import com.jme3.math.Vector3f
import core.distanceBetween
import core.findShortestPathDijkstra
import core.pathLength
import model.City
import model.graph.Vertex
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Scheme2D(
    modifier: Modifier = Modifier,
    city: City,
    editorMode: Boolean = false,
    onClick: (() -> Unit) = {},
    onMouseAction: (position: Offset, pressed: Boolean) -> Unit = { _, _ -> },
) {

    var mouseCoordinate by remember { mutableStateOf(Offset.Zero) }

    val scale = 10f // Масштаб для перевода координат в пиксели
    val gridStep = 10 * scale // Шаг сетки
    val offset = gridStep // Смещение в одну клетку в левом верхнем углу

    // Состояния для хранения двух выбранных вершин
    var selectedVertex1 by remember { mutableStateOf<Vertex?>(null) }
    var selectedVertex2 by remember { mutableStateOf<Vertex?>(null) }
    var shortestPath by remember { mutableStateOf<List<Vertex>>(emptyList()) }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->

                    onClick()

                    if (editorMode) return@detectTapGestures

                    // Переводим координаты клика в координаты графа с учетом смещения и масштаба
                    val clickedPosition = Vector3f(
                        (tapOffset.x - offset) / scale,
                        10f,
                        (tapOffset.y - offset) / scale,
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
                val x = ((event.changes.first().position.x - offset) / scale).roundToInt()
                val y = ((event.changes.first().position.y - offset) / scale).roundToInt()
                onMouseAction(Offset(x.toFloat(), y.toFloat()), event.changes.first().pressed)
                mouseCoordinate = Offset(x * scale + offset, y * scale + offset)
            }
    ) {
        // Рисуем сетку
        for (x in 0..size.width.toInt() step gridStep.toInt()) {
            drawLine(
                color = MESH_COLOR,
                start = Offset(x.toFloat(), 0f),
                end = Offset(x.toFloat(), size.height),
                strokeWidth = 1f
            )
        }
        for (y in 0..size.height.toInt() step gridStep.toInt()) {
            drawLine(
                color = MESH_COLOR,
                start = Offset(0f, y.toFloat()),
                end = Offset(size.width, y.toFloat()),
                strokeWidth = 1f
            )
        }

        // Рисуем здания
        city.buildings.forEach { building ->

            val path = Path().apply {
                this.moveTo(building.groundCoords.first().x * scale + offset, building.groundCoords.first().z * scale + offset)
                building.groundCoords.forEach { coordinate ->
                    lineTo(
                        coordinate.x * scale + offset,
                        coordinate.z * scale + offset
                    )
                }
            }

            drawPath(
                path = path,
                color = BUILDING_COLOR,
                style = Fill
            )

        }

        // Рисуем ребра графа
        city.graph.edges.forEach { edge ->
            val startX = edge.vertex1.position.x * scale + offset
            val startY = edge.vertex1.position.z * scale + offset
            val endX = edge.vertex2.position.x * scale + offset
            val endY = edge.vertex2.position.z * scale + offset

            drawLine(
                color = EDGE_COLOR,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 2f
            )
        }

        // Рисуем вершины графа
        city.graph.vertices.forEach { vertex ->
            val x = vertex.position.x * scale + offset
            val y = vertex.position.z * scale + offset

            drawCircle(
                color = KEY_POINT_COLOR,
                radius = 6f,
                center = Offset(x, y)
            )
        }

        if (editorMode) {
            // место курсора
            drawLine(MOUSE_POINT_COLOR, Offset(mouseCoordinate.x - 10, mouseCoordinate.y), Offset(mouseCoordinate.x + 10, mouseCoordinate.y), 1f)
            drawLine(MOUSE_POINT_COLOR, Offset(mouseCoordinate.x, mouseCoordinate.y - 10), Offset(mouseCoordinate.x, mouseCoordinate.y + 10), 1f)
        } else {

            // Рисуем кратчайший путь, если он есть
            if (shortestPath.isNotEmpty()) {
                println(shortestPath)
                shortestPath.zipWithNext { v1, v2 ->
                    val startX = v1.position.x * scale + offset
                    val startY = v1.position.z * scale + offset
                    val endX = v2.position.x * scale + offset
                    val endY = v2.position.z * scale + offset

                    drawLine(
                        color = SHORTEST_PATH_COLOR,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 4f
                    )
                }
            }

            selectedVertex1?.let {
                val x = it.position.x * scale + offset
                val y = it.position.z * scale + offset

                drawCircle(
                    color = BASE_STATION_COLOR,
                    radius = 9f,
                    center = Offset(x, y)
                )
            }

            selectedVertex2?.let {
                val x = it.position.x * scale + offset
                val y = it.position.z * scale + offset

                drawCircle(
                    color = DESTINATION_COLOR,
                    radius = 9f,
                    center = Offset(x, y)
                )
            }

        }
    }

}