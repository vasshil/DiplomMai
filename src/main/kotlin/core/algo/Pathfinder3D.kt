package core.algo

import com.jme3.math.Vector3f
import java.util.PriorityQueue
import kotlin.math.min

/**
 * A* для «ленивого» 3-D-графа.
 * @return список вершин от start до goal (включая обе) или пустой, если путь не найден.
 */
fun fastestPath3D(
    start : Vertex3D,
    goal  : Vertex3D,
    graph : LazyGraph3D
): List<Vertex3D> {

    fun h(v: Vertex3D) = v.position.distance(goal.position) // эвристика – «по прямой»

    data class Node(val v: Vertex3D, val g: Float, val f: Float)

    val open   = PriorityQueue<Node>(compareBy { it.f })
    val gScore = mutableMapOf(start to 0f)
    val came   = mutableMapOf<Vertex3D, Vertex3D?>()

    open += Node(start, 0f, h(start)); came[start] = null

    while (open.isNotEmpty()) {
        val curr = open.poll().v
        if (curr == goal) break

        for (e in graph.neighbours(curr)) {
            val tentative = gScore.getValue(curr) + e.cost
            if (tentative < gScore.getOrDefault(e.to, Float.POSITIVE_INFINITY)) {
                gScore[e.to] = tentative
                open += Node(e.to, tentative, tentative + h(e.to))
                came[e.to] = curr
            }
        }
    }
    /* восстановление */
    return buildList {
        var v: Vertex3D? = goal
        while (v != null && v in came) { add(v); v = came[v] }
    }.asReversed()
}