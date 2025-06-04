package model.landscape

import androidx.compose.runtime.Immutable
import com.jme3.math.Vector3f
import com.jme3.scene.Mesh
import com.jme3.scene.VertexBuffer
import com.jme3.util.BufferUtils
import core.distanceBetween
import model.graph.FlyMapVertex
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Polygon
import org.locationtech.jts.operation.buffer.BufferOp
import org.locationtech.jts.operation.buffer.BufferParameters
import org.locationtech.jts.operation.buffer.BufferParameters.CAP_FLAT
import org.locationtech.jts.operation.buffer.BufferParameters.JOIN_MITRE
import org.locationtech.jts.triangulate.polygon.ConstrainedDelaunayTriangulator
import org.locationtech.jts.triangulate.tri.Tri
import java.io.Serializable

@Immutable
data class Building(
    val id: Long,
    val groundCoords: List<Vector3f> = listOf(),
    val height: Float = 0f,
    val safeDistanceCoords: List<FlyMapVertex> = listOf()
): Serializable {

    companion object {
        const val safeDistance = 1f
        private const val serialVersionUID: Long = 1L
    }


//    constructor(id: Long, groundCoords: MutableList<Vertex>, height: Float) : this(id, groundCoords.map { it.position }.toMutableList(), height)

    fun toJTSPolygon(coords: List<Vector3f> = groundCoords): Polygon {
        val coordinates = coords.map {
            Coordinate(it.x.toDouble(), it.z.toDouble())
        }.toMutableList().also { it.add(it.first()) }.toTypedArray()
        return GeometryFactory().createPolygon(coordinates)
    }


    fun toSafeJTSPolygon(coords: List<Vector3f> = safeDistanceCoords.map { it.position }): Polygon {
        val coordinates = coords.map {
            Coordinate(it.x.toDouble(), it.z.toDouble())
        }.toMutableList().also { it.add(it.first()) }.toTypedArray()
        return GeometryFactory().createPolygon(coordinates)
    }


    fun getMesh3D(): Mesh {

        val mesh = Mesh()
        val numVertices = groundCoords.size
        val vertices = mutableListOf<Vector3f>()
        val indices = mutableListOf<Int>()

//        val groundTries = ConstrainedDelaunayTriangulator(toJTSPolygon()).triangles
        val groundTries = ConstrainedDelaunayTriangulator(toJTSPolygon()).triangles
//            .map {
//            it.toPolygon(GeometryFactory())
//        }

        val roofTries = groundTries.map { tri ->
            Tri(
                tri.getCoordinate(0),//.also { it.y + height },
                tri.getCoordinate(1),//.also { it.y + height },
                tri.getCoordinate(2),//.also { it.y + height }
            )
        }

        // Добавляем вершины основания
//        groundTries.forEach { tri ->
//            vertices.add(tri.getCoordinate(0).let { Vector3f(it.x.toFloat(), 0f, it.y.toFloat()) })
//            vertices.add(tri.getCoordinate(1).let { Vector3f(it.x.toFloat(), 0f, it.y.toFloat()) })
//            vertices.add(tri.getCoordinate(2).let { Vector3f(it.x.toFloat(), 0f, it.y.toFloat()) })
//        }

        println(roofTries)

//      Добавляем вершины для верха здания
        roofTries.forEach { tri ->
            vertices.add(tri.getCoordinate(0).let { Vector3f(it.x.toFloat(), height, it.y.toFloat()) })
            vertices.add(tri.getCoordinate(1).let { Vector3f(it.x.toFloat(), height, it.y.toFloat()) })
            vertices.add(tri.getCoordinate(2).let { Vector3f(it.x.toFloat(), height, it.y.toFloat()) })
        }

        for (i in 0 until vertices.size step 3) {
            indices.add(i)
            indices.add(i + 1)
            indices.add(i + 2)
        }

        // Индексы для верха
        val topOffset = vertices.size

        groundCoords.forEach {
            vertices.add(Vector3f(it.x, 0f, it.z))
            vertices.add(Vector3f(it.x, height, it.z))
        }

//         Индексы для боковых стен
        for (i in topOffset until   vertices.size - 2 step 2) {
            indices.add(i + 1)
            indices.add(i)
            indices.add(i + 2)
//            indices.add(i)

            indices.add(i + 2)
            indices.add(i + 3)
            indices.add(i + 1)
//            indices.add(i + 1)
//            println(vertices[i + 3])
            println("$i ${vertices.size}")
        }

        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(*vertices.toTypedArray()))
        mesh.setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createIntBuffer(*indices.toIntArray()))
        mesh.updateBound()
//        mesh.mode = Mode.TriangleFan

//        println(vertices.toTypedArray().contentToString())
//        println(indices.toTypedArray().contentToString())

        println(groundCoords)
//        println(groundTries)

        return mesh
    }

    // Получаем список вершин с отступом от здания
    fun getKeyNodes(reqHeight: Float = 0f): List<FlyMapVertex> {
        var expandedVertices: List<FlyMapVertex> = emptyList()

        if (reqHeight > height + safeDistance) return expandedVertices

        expandedVertices =
            BufferOp.bufferOp(
                toJTSPolygon(),
                safeDistance.toDouble(),
                BufferParameters(0, CAP_FLAT, JOIN_MITRE, 1000.0))
                .coordinates.map {
                    FlyMapVertex(id, it.x.toFloat(), 0f, it.y.toFloat())
                }


        return expandedVertices

    }

    /**
     * Возвращает ближайшую вершину из списка [safeDistanceCoords]
     * к указанным координатам [target]. Если все вершины дальше, чем
     * `safeDistance`, вернёт null.
     */
    fun findNearestSafeVertex(target: Vector3f): FlyMapVertex? =
        safeDistanceCoords
            .minByOrNull {
                distanceBetween(it.position, target)
            }

    override fun hashCode(): Int {
        return "$id$height${groundCoords.hashCode()}${safeDistanceCoords.hashCode()}".hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Building

        if (id != other.id) return false
        if (groundCoords != other.groundCoords) return false
        if (height != other.height) return false
        if (safeDistanceCoords != other.safeDistanceCoords) return false

        return true
    }

}


fun main() {
    Building(id = 0, groundCoords = mutableListOf(Vector3f(0f, 0f, 0f), Vector3f(10f, 0f, 10f), Vector3f(0f, 0f, 20f), Vector3f(-10f, 0f, 10f), )).getMesh3D()
}
