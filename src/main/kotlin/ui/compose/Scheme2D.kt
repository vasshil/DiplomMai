package ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.jme3.math.Vector3f
import core.distanceBetween
import core.findShortestPathDijkstra
import core.pathLength
import model.City
import model.graph.Vertex

@Composable
fun Scheme2D(modifier: Modifier = Modifier, city: City) {
    val scale = 10f // Масштаб для перевода координат в пиксели
    val gridStep = 10 * scale // Шаг сетки
    val offset = gridStep // Смещение в одну клетку в левом верхнем углу

    // Состояния для хранения двух выбранных вершин
    var selectedVertex1 by remember { mutableStateOf<Vertex?>(null) }
    var selectedVertex2 by remember { mutableStateOf<Vertex?>(null) }
    var shortestPath by remember { mutableStateOf<List<Vertex>>(emptyList()) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
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
    ) {
        // Рисуем сетку
        for (x in 0..size.width.toInt() step gridStep.toInt()) {
            drawLine(
                color = Color.Gray,
                start = Offset(x.toFloat(), 0f),
                end = Offset(x.toFloat(), size.height),
                strokeWidth = 1f
            )
        }
        for (y in 0..size.height.toInt() step gridStep.toInt()) {
            drawLine(
                color = Color.Gray,
                start = Offset(0f, y.toFloat()),
                end = Offset(size.width, y.toFloat()),
                strokeWidth = 1f
            )
        }

        // Рисуем здания
        city.buildings.forEach { building ->
            val left = building.position.x * scale * 10 + offset
            val top = building.position.z * scale * 10 + offset
            val width = building.size.x * scale * 10
            val height = building.size.z * scale * 10

            drawRoundRect(
                color = Color.Blue,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(width, height),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }

        // Рисуем ребра графа
        city.graph.edges.forEach { edge ->
            val startX = edge.vertex1.position.x * scale + offset
            val startY = edge.vertex1.position.z * scale + offset
            val endX = edge.vertex2.position.x * scale + offset
            val endY = edge.vertex2.position.z * scale + offset

            drawLine(
                color = Color.Blue,
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
                color = Color.Black,
                radius = 6f,
                center = Offset(x, y)
            )
        }

        // Рисуем кратчайший путь, если он есть
        if (shortestPath.isNotEmpty()) {
            println(shortestPath)
            shortestPath.zipWithNext { v1, v2 ->
                val startX = v1.position.x * scale + offset
                val startY = v1.position.z * scale + offset
                val endX = v2.position.x * scale + offset
                val endY = v2.position.z * scale + offset

                drawLine(
                    color = Color.Red,
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
                color = Color.Red,
                radius = 9f,
                center = Offset(x, y)
            )
        }

        selectedVertex2?.let {
            val x = it.position.x * scale + offset
            val y = it.position.z * scale + offset

            drawCircle(
                color = Color.Red,
                radius = 9f,
                center = Offset(x, y)
            )
        }

    }

}