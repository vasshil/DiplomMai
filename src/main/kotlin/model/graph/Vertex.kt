package model.graph

import com.jme3.math.Vector3f

data class Vertex(val position: Vector3f)

fun Vector3f.toVertex() = Vertex(this)
