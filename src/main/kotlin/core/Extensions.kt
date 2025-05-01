package core

import com.jme3.math.Vector3f
import core.algo.Vertex3D
import model.graph.Vertex
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.math.Vector3D
import kotlin.math.cos
import kotlin.math.sin

fun Vector3f.perpendicularXZ() = Vector3f(z, 0f, -x) // Перпендикулярный вектор по часовой стрелке в плоскости XY

fun Vector3f.rotateXZ(angleRad: Float): Vector3f {
    val rotX = x * cos(angleRad) - z * sin(angleRad)
    val rotZ = x * sin(angleRad) + z * cos(angleRad)
    return Vector3f(rotX, 0f, rotZ)
}

fun Coordinate.toVector3f(): Vector3f = let { Vector3f(it.x.toFloat(), it.y.toFloat(), it.y.toFloat()) }

fun Vector3f.toJTSCoordinate(): Coordinate = Coordinate(x.toDouble(), z.toDouble())


/** Быстро превращаем старую Vertex (XY) в 3-D-вершину конкретного уровня */
fun Vertex.to3D(y: Float, buildingId: Long): Vertex3D =
    Vertex3D(buildingId, position.x, y, position.z)