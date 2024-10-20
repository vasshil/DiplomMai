package model.landscape

import com.jme3.math.Vector3f
import model.graph.Vertex
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Polygon
import org.locationtech.jts.operation.buffer.BufferOp
import org.locationtech.jts.operation.buffer.BufferParameters
import org.locationtech.jts.operation.buffer.BufferParameters.CAP_FLAT
import org.locationtech.jts.operation.buffer.BufferParameters.JOIN_MITRE

//data class Building(
//    val position: Vector3f,
//    val size: Vector3f
//)

data class Building(
    val id: Long,
    val groundCoords: Array<Vector3f>,
    val height: Float
) {

    companion object {
        const val safeDistance = 1f
    }

    constructor(id: Long, groundCoords: Array<Vertex>, height: Float) : this(id, groundCoords.map { it.position }.toTypedArray(), height)


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Building

        if (!groundCoords.contentEquals(other.groundCoords)) return false
        if (height != other.height) return false

        return true
    }

    override fun hashCode(): Int {
        var result = groundCoords.contentHashCode()
        result = 31 * result + height.hashCode()
        return result
    }

    fun toJTSPolygon(): Polygon {
        val coordinates = groundCoords.map {
            Coordinate(it.x.toDouble(), it.z.toDouble())
        }.toMutableList().also { it.add(it.first()) }.toTypedArray()
        return GeometryFactory().createPolygon(coordinates)
    }


    // Получаем список вершин с отступом от здания
    fun getKeyNodes(reqHeight: Float = 0f): List<Vertex> {
        var expandedVertices: List<Vertex> = emptyList()

        if (reqHeight > height + safeDistance) return expandedVertices

        expandedVertices =
            BufferOp.bufferOp(
                toJTSPolygon(),
                safeDistance.toDouble(),
                BufferParameters(0, CAP_FLAT, JOIN_MITRE, 1000.0))
                .coordinates.map {
                    Vertex(id, it.x.toFloat(), 0f, it.y.toFloat())
                }


        return expandedVertices

    }

}
