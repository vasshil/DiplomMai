package model.graph

import androidx.compose.runtime.Immutable
import com.jme3.math.Vector3f
import org.locationtech.jts.geom.Coordinate
import java.io.Serializable


@Immutable
data class FlyMapVertex(
    val buildingId: Long,
    val position: Vector3f,
    val edges: MutableList<FlyMapEdgeEdge> = mutableListOf(),
    var isChargeStation: Boolean = false,
): Serializable {

    constructor(buildingId: Long, x: Float, y: Float, z: Float) : this(buildingId, Vector3f(x, y, z))

    override fun hashCode(): Int {
        return position.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FlyMapVertex

        return position == other.position
    }

    override fun toString(): String {
        return position.toString()
    }

    fun toJTSCoordinate(): Coordinate {
        return Coordinate(position.x.toDouble(), position.z.toDouble())
    }

    companion object {
        // Менять это число НЕЛЬЗЯ, пока вы хотите читать старые файлы
        private const val serialVersionUID: Long = 1L
    }
}

//fun Vector3f.toVertex() = Vertex(this)
