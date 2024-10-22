package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.jme3.math.Vector3f
import model.City
import model.graph.Edge
import model.graph.Graph3D
import model.landscape.Building
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Polygon
import ui.compose.common.Scheme2D

@Composable
@Preview
fun App() {

    val buildings by remember {
        mutableStateOf(createBuildings())
    }


    MaterialTheme {
        Scheme2D(
            modifier = Modifier.size(width = 600.dp, height = 500.dp),
            city = City(buildings, createGraphAtHeight(buildings, 0f))
        )
    }

}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}


fun createBuildings(): MutableList<Building> {
    val buildings = mutableListOf<Building>(
        // Треугольное здание
        Building(
            id = 0,
            groundCoords = mutableListOf(
                Vector3f(10f, 0f, 10f),
                Vector3f(20f, 0f, 10f),
                Vector3f(15f, 0f, 20f)
            ),
            height = 15f
        ),
        // Здание в виде буквы "Г"
        Building(
            id = 1,
            groundCoords = mutableListOf(
                Vector3f(30f, 0f, 30f),
                Vector3f(40f, 0f, 30f),
                Vector3f(40f, 0f, 35f),
                Vector3f(35f, 0f, 35f),
                Vector3f(35f, 0f, 40f),
                Vector3f(30f, 0f, 40f)
            ),
            height = 20f
        ),
        // Прямоугольное здание 1
        Building(
            id = 2,
            groundCoords = mutableListOf(
                Vector3f(50f, 0f, 50f),
                Vector3f(60f, 0f, 50f),
                Vector3f(60f, 0f, 60f),
                Vector3f(50f, 0f, 60f)
            ),
            height = 25f
        ),
        // Прямоугольное здание 2
        Building(
            id = 3,
            groundCoords = mutableListOf(
                Vector3f(70f, 0f, 70f),
                Vector3f(85f, 0f, 70f),
                Vector3f(85f, 0f, 80f),
                Vector3f(70f, 0f, 80f)
            ),
            height = 30f
        ),
        // Прямоугольное здание 3
        Building(
            id = 4,
            groundCoords = mutableListOf(
                Vector3f(90f, 0f, 10f),
                Vector3f(100f, 0f, 10f),
                Vector3f(100f, 0f, 20f),
                Vector3f(90f, 0f, 20f)
            ),
            height = 10f
        )
    )

    return buildings
}

fun createGraphAtHeight(buildings: List<Building>, height: Float): Graph3D {
    val graph = Graph3D()

    val buildingsGeometry = mutableListOf<Polygon>()

    for (building in buildings) {
        val keyNodes = building.getKeyNodes(height)
        if (keyNodes.isEmpty()) continue

        buildingsGeometry.add(Building(0, keyNodes.map { it.position }.toMutableList(), 0f).toJTSPolygon())

        keyNodes.forEach {
            graph.add(building.id, it)
        }

        for (i in 0 until keyNodes.size - 1) {
            graph.add(Edge(keyNodes[i], keyNodes[i + 1]))
        }
        graph.add(Edge(keyNodes.first(), keyNodes.last()))

    }

    for (vertex1i in graph.vertices.indices) {
        for (vertex2i in vertex1i until graph.vertices.size) {
            val vertex1 = graph.vertices[vertex1i]
            val vertex2 = graph.vertices[vertex2i]
            if (vertex1.buildingId != vertex2.buildingId) {

                val edge = GeometryFactory().createLineString(arrayOf(vertex1.toJTSCoordinate(), vertex2.toJTSCoordinate()))

                if (!checkEdgeBuildingsIntersection(edge, buildingsGeometry)) {
                    graph.add(Edge(vertex1, vertex2))
                }

            }
        }
    }

    return graph
}

fun checkEdgeBuildingsIntersection(edge: LineString, buildings: List<Polygon>): Boolean {
    for (building in buildings) {
        println(building.intersection(edge).coordinates.contentToString())
        if (building.intersection(edge).coordinates.size > 1) {
            return true
        }
    }
    return false
}
