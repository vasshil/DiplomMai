package model

import androidx.compose.ui.geometry.Offset
import model.graph.Edge
import model.graph.Graph3D
import model.graph.Vertex
import model.landscape.Building
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Polygon
import kotlin.math.abs
import kotlin.math.hypot

data class City(
    val buildings: MutableList<Building> = mutableListOf(),
    var graph: Graph3D = Graph3D(),
) {

    fun newBuilding(): Building {
        val newId = if (buildings.isEmpty()) 0 else buildings.last().id + 1
        val newBuilding = Building(newId)
        buildings.add(newBuilding)
        return buildings.last()
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


}
