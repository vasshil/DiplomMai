package model.cargo

import com.jme3.math.Vector3f
import core.algo.Vertex3D
import ui.compose.common.CARGO_STATUS_WAITING_COLOR
import java.io.Serializable

data class Cargo(
    val timeCreation: Long = System.currentTimeMillis(),
    val weight: Double, // kg
    val startVertex: Vector3f,
    val destination: Vector3f,
    val status: CargoStatus = CargoStatus.WAITING
): Serializable
