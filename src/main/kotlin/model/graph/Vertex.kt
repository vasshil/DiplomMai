package model.graph

import com.jme3.math.Vector3f

data class Vertex(
    val position: Vector3f,
    val edges: MutableList<Edge> = mutableListOf()
)

//fun Vector3f.toVertex() = Vertex(this)
