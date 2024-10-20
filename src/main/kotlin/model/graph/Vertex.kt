package model.graph

import com.jme3.math.Vector3f
import org.locationtech.jts.geom.Coordinate

data class Vertex(
    val buildingId: Long,
    val position: Vector3f,
    val edges: MutableList<Edge> = mutableListOf()
) {

    constructor( buildingId: Long, x: Float, y: Float, z: Float) : this(buildingId, Vector3f(x, y, z))

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

    fun toJTSCoordinate(): Coordinate {
        return Coordinate(position.x.toDouble(), position.z.toDouble())
    }

}

//fun Vector3f.toVertex() = Vertex(this)
