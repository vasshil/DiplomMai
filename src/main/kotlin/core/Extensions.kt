package core

import com.jme3.math.Vector3f
import org.locationtech.jts.geom.Coordinate
import kotlin.math.cos
import kotlin.math.sin

fun Vector3f.perpendicularXZ() = Vector3f(z, 0f, -x) // Перпендикулярный вектор по часовой стрелке в плоскости XY

fun Vector3f.rotateXZ(angleRad: Float): Vector3f {
    val rotX = x * cos(angleRad) - z * sin(angleRad)
    val rotZ = x * sin(angleRad) + z * cos(angleRad)
    return Vector3f(rotX, 0f, rotZ)
}

fun Coordinate.toVector3f(): Vector3f = let { Vector3f(it.x.toFloat(), it.y.toFloat(), it.y.toFloat()) }