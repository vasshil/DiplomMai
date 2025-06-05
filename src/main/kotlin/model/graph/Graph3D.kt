package model.graph

import com.jme3.math.Vector3f
import java.io.Serializable

data class Graph3D(
    val vertices: MutableList<FlyMapVertex> = mutableListOf(),
    val edges: MutableList<FlyMapEdgeEdge> = mutableListOf(),
): Serializable {

    fun add(buildingId: Long, vertex: FlyMapVertex) {
        vertices.add(vertex)
    }

    fun add(edge: FlyMapEdgeEdge) {
        edges.add(edge)
//        edge.vertex1.edges.add(edge)
//        edge.vertex2.edges.add(edge)
    }

    fun add(buildingId: Long, vector: Vector3f) {
        vertices.add(FlyMapVertex(buildingId, vector))
    }

}