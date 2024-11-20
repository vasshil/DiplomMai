package model

import androidx.compose.ui.geometry.Offset
import com.jme3.math.Vector3f
import model.graph.Edge
import model.graph.Graph3D
import model.graph.Vertex
import model.landscape.Building
import org.locationtech.jts.geom.*
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import kotlin.math.abs
import kotlin.math.hypot

data class City(
    val buildings: MutableList<Building> = mutableListOf(),
    var graph: Graph3D = Graph3D(),
): Serializable {

    fun newBuilding(): Building {
        val newId = if (buildings.isEmpty()) 0 else buildings.last().id + 1
        val newBuilding = Building(newId)
        buildings.add(newBuilding)
        return buildings.last()
    }

    fun removeBuilding(id: Long) {
        buildings.removeIf { it.id == id }
    }

//    fun addBaseStation(station: Vertex) {
//        baseStations.add(station)
//    }
//
//    fun addDestinationStation(station: Vertex) {
//        destinations.add(station)
//    }

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
    fun saveToFile(filePath: String) {

        File(filePath).outputStream().use {
            ObjectOutputStream(it).writeObject(this)
        }

//        val json = Json {
//            serializersModule = SerializersModule {
//                contextual(Vector3fSerializer) // Регистрируем кастомный сериализатор
//            }
//        }
//        val jsonString = json.encodeToString(this)
//        File(filePath).writeText(jsonString)
    }

    fun checkPointAt(x: Float, y: Float): Building? {
        buildings.forEach {
            if (it.toJTSPolygon().contains(GeometryFactory().createPoint(Coordinate(x.toDouble(), y.toDouble())))) {
                return it
            }
        }
        return null
    }

    companion object {
        // Загрузка объекта из файла в формате JSON
        fun loadFromFile(filePath: String): City? {

            val file = File(filePath)
            return if (file.exists()) {
                try {
                    file.inputStream().use {
                        (ObjectInputStream(it).readObject() as City).apply {
//                            graph = createGraphAtHeight()
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


//            val json = Json {
//                serializersModule = SerializersModule {
//                    contextual(Vector3fSerializer) // Регистрируем кастомный сериализатор
//                }
//            }
//
//            val file = File(filePath)
//            return if (file.exists()) {
//                val jsonString = file.readText()
//                try {
//                    json.decodeFromString<City>(jsonString)
//                } catch (_: Exception) {
//                    null
//                }
//            } else {
//                null
//            }
        }
    }

}
