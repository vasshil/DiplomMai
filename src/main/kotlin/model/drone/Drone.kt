package model.drone

import com.jme3.math.Vector3f
import model.cargo.Cargo
import org.jetbrains.annotations.Range

data class Drone(
    val id: Int,
    var batteryLevel: Int = 100, // 0 - 100
    val maxCargoStorage: Int, // кг, макс вместимость
    val cargos: MutableList<Cargo> = mutableListOf(),
    var currentDestination: Vector3f? = null,
    var currentWayPoint: MutableList<Vector3f> = mutableListOf(),
) {

}
