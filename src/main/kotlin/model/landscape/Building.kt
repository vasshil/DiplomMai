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
    val groundCoords: MutableList<Vector3f> = mutableListOf(),
    val height: Float = 0f
) {

    companion object {
        const val safeDistance = 1f
    }

//    constructor(id: Long, groundCoords: MutableList<Vertex>, height: Float) : this(id, groundCoords.map { it.position }.toMutableList(), height)

    fun addGroundPoint(x: Float, z: Float) {
        groundCoords.add(Vector3f(x, 0f, z))
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
