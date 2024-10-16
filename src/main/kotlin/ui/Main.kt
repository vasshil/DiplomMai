package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.jme3.math.Vector3f
import model.City
import model.graph.Edge
import model.graph.Graph3D
import model.landscape.Building
import ui.compose.Scheme2D

@Composable
@Preview
fun App() {

    MaterialTheme {
        Scheme2D(modifier = Modifier.size(width = 600.dp, height = 500.dp), City(createBuildings(), createGraph()))
    }

}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}


fun createBuildings(): List<Building> {
    val buildings = mutableListOf<Building>()
    buildings.add(Building(Vector3f(0f, 0f, 0f), Vector3f(2f, 5f, 2f)))
    buildings.add(Building(Vector3f(4f, 0f, 0f), Vector3f(3f, 5f, 1f)))
    buildings.add(Building(Vector3f(8f, 0f, 0f), Vector3f(1f, 4f, 3f)))
    buildings.add(Building(Vector3f(0f, 0f, 3f), Vector3f(3f, 3f, 1f)))
    buildings.add(Building(Vector3f(5f, 0f, 2f), Vector3f(2f, 3f, 3f)))
    buildings.add(Building(Vector3f(8f, 0f, 4f), Vector3f(1f, 2f, 1f)))
    buildings.add(Building(Vector3f(0f, 0f, 5f), Vector3f(4f, 1f, 1f)))

    return buildings
}

fun createGraph(): Graph3D {
    val SD = 1f
    val HGT = 10f
    val graph = Graph3D()
    graph.add(Vector3f(-SD, HGT, -SD))
    graph.add(Vector3f(20 + SD, HGT, -SD))
    graph.add(Vector3f(40 - SD, HGT, -SD))
    graph.add(Vector3f(70 + SD, HGT, -SD))
    graph.add(Vector3f(80 - SD, HGT, -SD))
    graph.add(Vector3f(90 + SD, HGT, -SD))
    graph.add(Vector3f(90 + SD, HGT, 30 + SD))
    graph.add(Vector3f(90 + SD, HGT, 40 - SD))
    graph.add(Vector3f(90 + SD, HGT, 50 + SD))
    graph.add(Vector3f(80 - SD, HGT, 50 + SD))
    graph.add(Vector3f(70 + SD, HGT, 50 + SD))
    graph.add(Vector3f(50 - SD, HGT, 50 + SD))
    graph.add(Vector3f(40 + SD, HGT, 50 + SD))
    graph.add(Vector3f(40 + SD, HGT, 60 + SD))
    graph.add(Vector3f(-SD, HGT, 60 + SD))
    graph.add(Vector3f(-SD, HGT, 50 - SD))
    graph.add(Vector3f(-SD, HGT, 40 + SD))
    graph.add(Vector3f(-SD, HGT, 30 - SD))
    graph.add(Vector3f(-SD, HGT, 20 + SD))
//        graph.add(Vector3f(f, HGT, f))

    for (i in 0 until graph.vertices.size - 1) {
        graph.add(Edge(graph.vertices[i], graph.vertices[i + 1]))
    }
    graph.add(Edge(graph.vertices.last(), graph.vertices.first()))


    graph.add(Vector3f(20 + SD, HGT, 20 + SD)) // 19
    graph.add(Vector3f(40 - SD, HGT, 10 + SD)) // 20
    graph.add(Vector3f(70 + SD, HGT, 10 + SD)) // 21
    graph.add(Vector3f(70 + SD, HGT, 20 - SD)) // 22
    graph.add(Vector3f(80 - SD, HGT, 30 + SD)) // 23
    graph.add(Vector3f(80 - SD, HGT, 40 - SD)) // 24
    graph.add(Vector3f(50 - SD, HGT, 20 - SD)) // 25
    graph.add(Vector3f(30 + SD, HGT, 30 - SD)) // 26
    graph.add(Vector3f(30 + SD, HGT, 40 + SD)) // 27
    graph.add(Vector3f(40 + SD, HGT, 50 - SD)) // 28
    graph.add(Vector3f(40 - SD, HGT, 50 - SD)) // 29
//        graph.add(Vector3f(, HGT, )) // 3

    graph.add(Edge(graph.vertices[1], graph.vertices[19]))
    graph.add(Edge(graph.vertices[1], graph.vertices[20]))
    graph.add(Edge(graph.vertices[2], graph.vertices[20]))
    graph.add(Edge(graph.vertices[3], graph.vertices[21]))
    graph.add(Edge(graph.vertices[3], graph.vertices[23]))
    graph.add(Edge(graph.vertices[4], graph.vertices[23]))
    graph.add(Edge(graph.vertices[6], graph.vertices[23]))
    graph.add(Edge(graph.vertices[6], graph.vertices[24]))
    graph.add(Edge(graph.vertices[7], graph.vertices[24]))
    graph.add(Edge(graph.vertices[7], graph.vertices[23]))
    graph.add(Edge(graph.vertices[9], graph.vertices[24]))
    graph.add(Edge(graph.vertices[10], graph.vertices[22]))
    graph.add(Edge(graph.vertices[11], graph.vertices[25]))
    graph.add(Edge(graph.vertices[12], graph.vertices[28]))
    graph.add(Edge(graph.vertices[15], graph.vertices[29]))
    graph.add(Edge(graph.vertices[16], graph.vertices[27]))
    graph.add(Edge(graph.vertices[17], graph.vertices[26]))
    graph.add(Edge(graph.vertices[18], graph.vertices[19]))
    graph.add(Edge(graph.vertices[19], graph.vertices[25]))
    graph.add(Edge(graph.vertices[19], graph.vertices[26]))
    graph.add(Edge(graph.vertices[20], graph.vertices[19]))
    graph.add(Edge(graph.vertices[20], graph.vertices[25]))
    graph.add(Edge(graph.vertices[20], graph.vertices[26]))
    graph.add(Edge(graph.vertices[20], graph.vertices[21]))
    graph.add(Edge(graph.vertices[20], graph.vertices[29]))
    graph.add(Edge(graph.vertices[21], graph.vertices[22]))
    graph.add(Edge(graph.vertices[22], graph.vertices[25]))
    graph.add(Edge(graph.vertices[22], graph.vertices[23]))
    graph.add(Edge(graph.vertices[23], graph.vertices[24]))
    graph.add(Edge(graph.vertices[25], graph.vertices[26]))
    graph.add(Edge(graph.vertices[25], graph.vertices[28]))
    graph.add(Edge(graph.vertices[26], graph.vertices[27]))
    graph.add(Edge(graph.vertices[27], graph.vertices[29]))
    graph.add(Edge(graph.vertices[28], graph.vertices[29]))
//        graph.add(Edge(graph.vertices[], graph.vertices[]))


    return graph
}
