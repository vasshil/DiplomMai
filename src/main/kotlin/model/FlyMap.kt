package model

import androidx.compose.ui.geometry.Offset
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.write
import io.github.vinceglb.filekit.writeString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import model.cargo.Cargo
import model.drone.Drone
import model.graph.Edge
import model.graph.Graph3D
import model.graph.Vertex
import model.landscape.Building
import model.landscape.NoFlyZone
import org.locationtech.jts.geom.*
import java.io.*
import kotlin.math.hypot

data class FlyMap(
    val buildings: MutableList<Building> = mutableListOf(),
    val noFlyZones: MutableList<NoFlyZone> = mutableListOf(),
    var graph: Graph3D = Graph3D(),
    var drones: MutableList<Drone> = mutableListOf(),
    var cargos: MutableList<Cargo> = mutableListOf(),
): Serializable {

    fun nextDroneId(): Long {
        return (drones.maxOfOrNull { it.id } ?: -1) + 1
    }

    fun newBuilding(): Building {
        val newId = (buildings.maxOfOrNull { it.id } ?: -1) + 1
        val newBuilding = Building(newId)
        buildings.add(newBuilding)
        return buildings.last()
    }

    fun removeBuilding(id: Long) {
        buildings.removeIf { it.id == id }
    }

    fun newNFZ(): NoFlyZone {
        val newId = (noFlyZones.maxOfOrNull { it.id } ?: -1) + 1
        val newNFZ = NoFlyZone(newId, true)
        noFlyZones.add(newNFZ)
        return noFlyZones.last()
    }

    fun removeNFZ(id: Long) {
        noFlyZones.removeIf { it.id == id }
    }

    fun getNearestVertex(mouse: Offset): Vertex? {
        for (vertex in graph.vertices.reversed()) {
            if (hypot(vertex.position.x - mouse.x, vertex.position.z - mouse.y) <= 3) return vertex
        }
        return null
    }


    fun createGraphAtHeight(height: Float = 0f): Graph3D {
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
                graph.add(Edge(keyNodes[i], keyNodes[i + 1], true))
            }
            graph.add(Edge(keyNodes.first(), keyNodes.last(), true))

        }

        for (vertex1i in graph.vertices.indices) {
            for (vertex2i in vertex1i until graph.vertices.size) {
                val vertex1 = graph.vertices[vertex1i]
                val vertex2 = graph.vertices[vertex2i]
                if (vertex1.buildingId != vertex2.buildingId) {

                    val edge = GeometryFactory().createLineString(arrayOf(vertex1.toJTSCoordinate(), vertex2.toJTSCoordinate()))

                    if (!checkEdgeBuildingsIntersection(edge, buildingsGeometry)) {
                        graph.add(Edge(vertex1, vertex2, false))
                    }

                }
            }
        }

        this.graph = graph

        return graph
    }

    private fun checkEdgeBuildingsIntersection(edge: LineString, buildings: List<Polygon>): Boolean {
        for (building in buildings) {
            println(building.intersection(edge).coordinates.contentToString())
            if (building.intersection(edge).coordinates.size > 1) {
                return true
            }
        }
        return false
    }


    // Сохранение объекта в файл в формате JSON
    fun saveToFile(scope: CoroutineScope) {

        scope.launch {
            val out = FileKit.openFileSaver(
                suggestedName = "flyMap",
                extension     = "txt"
            )
            out?.let { f ->
                // сериализуем во временный буфер
                val data = ByteArrayOutputStream().use { buf ->
                    ObjectOutputStream(buf).use { oos -> oos.writeObject(this@FlyMap) }
                    buf.toByteArray()
                }
                // и пишем байты в выбранное место
                f.write(data)
            }
        }


    }

    fun checkBuildingPointAt(x: Float, y: Float): Building? {
        buildings.forEach {
            if (it.groundCoords.size >= 3) {
                if (it.toJTSPolygon().contains(GeometryFactory().createPoint(Coordinate(x.toDouble(), y.toDouble())))) {
                    return it
                }
            }
        }
        return null
    }

    fun checkNFZPointAt(x: Float, y: Float): NoFlyZone? {
        noFlyZones.forEach {
            if (it.groundCoords.size >= 3) {
                if (it.toJTSPolygon().contains(GeometryFactory().createPoint(Coordinate(x.toDouble(), y.toDouble())))) {
                    return it
                }
            }

        }
        return null
    }

    companion object {
        // Загрузка объекта из файла в формате JSON
        fun loadFromFile(filePath: String): FlyMap? {

            val file = File(filePath)
            return if (file.exists()) {
                try {
                    file.inputStream().use {
                        (ObjectInputStream(it).readObject() as FlyMap).apply {
                            graph = createGraphAtHeight()
//                            println(graph.toString())
                        }
                    }
                } catch (e: Exception) {
                    println("read file error $e")
                    null
                }
            } else {
                println("file not found $filePath")
                null
            }

        }
    }

}
