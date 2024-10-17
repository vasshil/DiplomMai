package core

import com.jme3.math.Vector3f
import model.graph.Vertex
import java.util.*
import kotlin.math.sqrt

fun findShortestPathDijkstra(start: Vertex, end: Vertex): List<Vertex> {
    // Если стартовая и конечная вершины совпадают, возвращаем их как путь
    if (start == end) return listOf(start)

    // PriorityQueue для вершин с расстояниями (по умолчанию отсортирована по минимальному расстоянию)
    val queue = PriorityQueue<Pair<Vertex, Float>>(compareBy { it.second })
    // Карта для отслеживания кратчайшего пути к каждой вершине
    val distances = mutableMapOf<Vertex, Float>().withDefault { Float.POSITIVE_INFINITY }
    // Карта для отслеживания предыдущих вершин в пути
    val previousVertices = mutableMapOf<Vertex, Vertex?>()

    // Инициализируем расстояние до стартовой вершины как 0
    distances[start] = 0f
    queue.add(Pair(start, 0f))

    while (queue.isNotEmpty()) {
        // Получаем вершину с наименьшим расстоянием
        val (currentVertex, currentDistance) = queue.poll()

        if (currentDistance > distances[currentVertex]!!) {
            continue
        }

        // Если достигли конечной вершины, то восстанавливаем путь
        if (currentVertex == end) {
            val path = mutableListOf<Vertex>()
            var vertex: Vertex? = end
            while (vertex != null) {
                path.add(vertex)
                vertex = previousVertices[vertex]
            }
            return path.reversed()
        }

        // Обрабатываем все рёбра текущей вершины
        for (edge in currentVertex.edges) {
            val neighbor = if (edge.vertex1 == currentVertex) edge.vertex2 else edge.vertex1
            val distance = currentDistance + distanceBetween(currentVertex, neighbor)

            // Если найдено более короткое расстояние до соседней вершины
            if (distance < distances.getValue(neighbor)) {
                distances[neighbor] = distance
                previousVertices[neighbor] = currentVertex
                queue.add(Pair(neighbor, distance))
            }
        }
    }

    // Если путь не найден, возвращаем пустой список
    return emptyList()
}



//def dijkstra(graph, start):
//    distances = {vertex: float('infinity') for vertex in graph}
//    distances[start] = 0
//    queue = [(0, start)]
//
//    while queue:
//        current_distance, current_vertex = heapq.heappop(queue)
//
//        # Обрабатываем только вершину с наименьшим расстоянием
//        if current_distance > distances[current_vertex]:
//            continue
//
//        for neighbor, weight in graph[current_vertex].items():
//            distance = current_distance + weight
//
//            # Рассматриваем этот новый путь только в том случае, если он лучше любого пути, который мы нашли до сих пор
//            if distance < distances[neighbor]:
//                distances[neighbor] = distance
//                heapq.heappush(queue, (distance, neighbor))
//
//    return distances

// Функция для вычисления Евклидова расстояния между двумя вершинами
fun distanceBetween(vertex1: Vertex, vertex2: Vertex): Float {
    val dx = vertex1.position.x - vertex2.position.x
    val dy = vertex1.position.y - vertex2.position.y
    val dz = vertex1.position.z - vertex2.position.z
    return sqrt(dx * dx + dy * dy + dz * dz)
}

fun distanceBetween(pos1: Vector3f, pos2: Vector3f): Float {
    val dx = pos1.x - pos2.x
    val dy = pos1.y - pos2.y
    val dz = pos1.z - pos2.z
    return sqrt(dx * dx + dy * dy + dz * dz)
}

fun pathLength(path: List<Vertex>): Float {
    var length = 0f
    for (i in 0 until path.size - 1) {
        length += distanceBetween(path[i], path[i + 1])
    }
    return length
}