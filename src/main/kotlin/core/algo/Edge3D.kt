package core.algo

import com.jme3.math.Vector3f
import java.io.Serializable

/** Ребро с заранее подсчитанной «стоимостью времени полёта» */
data class Edge3D(
    val from: Vertex3D,
    val to:   Vertex3D,
    val alongWall: Boolean,
    val cost: Float
) : Serializable