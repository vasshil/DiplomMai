package core.algo

import com.jme3.math.Vector3f
import org.locationtech.jts.geom.Coordinate
import java.io.Serializable

/** Вершина 3-D-графа: XY-координата + дискретная высота (уровень) */
data class Vertex3D(
    val buildingId: Long,
    val x: Float,
    val y: Float,          // высота слоя
    val z: Float
) : Serializable {

    fun toJTSCoordinate(): Coordinate = Coordinate(x.toDouble(), z.toDouble())
    val position: Vector3f get() = Vector3f(x, y, z)
}