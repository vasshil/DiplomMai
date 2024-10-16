package model.graph

import com.jme3.math.Vector3f

data class Graph3D(
    val vertices: MutableList<Vertex> = mutableListOf(),
    val edges: MutableList<Edge> = mutableListOf(),
) {

    fun add(vertex: Vertex) {
        vertices.add(vertex)
    }

    fun add(edge: Edge) {
        edges.add(edge)
        edge.vertex1.edges.add(edge)
        edge.vertex2.edges.add(edge)
    }

    fun add(vector: Vector3f) {
        vertices.add(Vertex(vector))
    }

}