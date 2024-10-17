package model.graph

import com.jme3.math.Vector3f

data class Vertex(
    val position: Vector3f,
    val edges: MutableList<Edge> = mutableListOf()
) {
    override fun hashCode(): Int {
        return position.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vertex

        return position == other.position
    }

    override fun toString(): String {
        return position.toString()
    }

}

//fun Vector3f.toVertex() = Vertex(this)
