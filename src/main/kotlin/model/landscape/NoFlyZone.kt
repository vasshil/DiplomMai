package model.landscape

import androidx.compose.runtime.Immutable
import com.jme3.math.Vector3f
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Polygon
import java.io.Serializable

@Immutable
data class NoFlyZone(
    val id: Long,
    val isActive: Boolean = true,
    val groundCoords: List<Vector3f> = listOf(),
): Serializable {

    companion object {
        const val safeDistance = 1f
        private const val serialVersionUID: Long = 1L
    }

    fun toJTSPolygon(coords: List<Vector3f> = groundCoords): Polygon {
        val coordinates = coords.map {
            Coordinate(it.x.toDouble(), it.z.toDouble())
        }.toMutableList().also { it.add(it.first()) }.toTypedArray()
        return GeometryFactory().createPolygon(coordinates)
    }


//    fun getMesh3D(): Mesh {
//
//        val mesh = Mesh()
//        val numVertices = groundCoords.size
//        val vertices = mutableListOf<Vector3f>()
//        val indices = mutableListOf<Int>()
//
////        val groundTries = ConstrainedDelaunayTriangulator(toJTSPolygon()).triangles
//        val groundTries = ConstrainedDelaunayTriangulator(toJTSPolygon()).triangles
////            .map {
////            it.toPolygon(GeometryFactory())
////        }
//
//        val roofTries = groundTries.map { tri ->
//            Tri(
//                tri.getCoordinate(0),//.also { it.y + height },
//                tri.getCoordinate(1),//.also { it.y + height },
//                tri.getCoordinate(2),//.also { it.y + height }
//            )
//        }
//
//        // Добавляем вершины основания
////        groundTries.forEach { tri ->
////            vertices.add(tri.getCoordinate(0).let { Vector3f(it.x.toFloat(), 0f, it.y.toFloat()) })
////            vertices.add(tri.getCoordinate(1).let { Vector3f(it.x.toFloat(), 0f, it.y.toFloat()) })
////            vertices.add(tri.getCoordinate(2).let { Vector3f(it.x.toFloat(), 0f, it.y.toFloat()) })
////        }
//
//        println(roofTries)
//
////      Добавляем вершины для верха здания
//        roofTries.forEach { tri ->
//            vertices.add(tri.getCoordinate(0).let { Vector3f(it.x.toFloat(), height, it.y.toFloat()) })
//            vertices.add(tri.getCoordinate(1).let { Vector3f(it.x.toFloat(), height, it.y.toFloat()) })
//            vertices.add(tri.getCoordinate(2).let { Vector3f(it.x.toFloat(), height, it.y.toFloat()) })
//        }
//
//        for (i in 0 until vertices.size step 3) {
//            indices.add(i)
//            indices.add(i + 1)
//            indices.add(i + 2)
//        }
//
//        // Индексы для верха
//        val topOffset = vertices.size
//
//        groundCoords.forEach {
//            vertices.add(Vector3f(it.x, 0f, it.z))
//            vertices.add(Vector3f(it.x, height, it.z))
//        }
//
////         Индексы для боковых стен
//        for (i in topOffset until   vertices.size - 2 step 2) {
//            indices.add(i + 1)
//            indices.add(i)
//            indices.add(i + 2)
////            indices.add(i)
//
//            indices.add(i + 2)
//            indices.add(i + 3)
//            indices.add(i + 1)
////            indices.add(i + 1)
////            println(vertices[i + 3])
//            println("$i ${vertices.size}")
//        }
//
//        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(*vertices.toTypedArray()))
//        mesh.setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createIntBuffer(*indices.toIntArray()))
//        mesh.updateBound()
////        mesh.mode = Mode.TriangleFan
//
////        println(vertices.toTypedArray().contentToString())
////        println(indices.toTypedArray().contentToString())
//
//        println(groundCoords)
////        println(groundTries)
//
//        return mesh
//    }

    // Получаем список вершин с отступом от здания
//    fun getKeyNodes(reqHeight: Float = 0f): List<Vertex> {
//        var expandedVertices: List<Vertex> = emptyList()
//
//        if (reqHeight > height + safeDistance) return expandedVertices
//
//        expandedVertices =
//            BufferOp.bufferOp(
//                toJTSPolygon(),
//                safeDistance.toDouble(),
//                BufferParameters(0, CAP_FLAT, JOIN_MITRE, 1000.0))
//                .coordinates.map {
//                    Vertex(id, it.x.toFloat(), 0f, it.y.toFloat())
//                }
//
//
//        return expandedVertices
//
//    }

    override fun hashCode(): Int {
        return "$id$isActive${groundCoords.hashCode()}".hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NoFlyZone

        if (id != other.id) return false
        if (isActive != other.isActive) return false
        if (groundCoords != other.groundCoords) return false

        return true
    }

}

