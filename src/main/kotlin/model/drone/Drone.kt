package model.drone

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.BatteryUnknown
import androidx.compose.material.icons.rounded.*
import com.jme3.math.Vector3f
import model.cargo.Cargo
import ui.compose.common.DRONE_BATTERY_LOW_COLOR
import ui.compose.common.DRONE_BATTERY_NORMAL_COLOR
import java.io.Serializable

data class Drone(
    val id: Long,
    var status: DroneStatus = DroneStatus.WAITING,
    var batteryLevel: Int = 100, // 0 - 100
    val maxCargoCapacityMass: Float, // кг, макс вместимость
    val cargos: MutableList<Cargo> = mutableListOf(),
    var currentDestination: Vector3f? = null,
    var currentWayPoint: MutableList<Vector3f> = mutableListOf(),
    var currentPosition: Vector3f = Vector3f.NAN,
): Serializable {

    fun getBatteryIcon() = if (status != DroneStatus.CHARGING) {
        when (batteryLevel) {
            0 -> Icons.Rounded.Battery0Bar
            in 1..15 -> Icons.Rounded.Battery1Bar
            in 16..25 -> Icons.Rounded.Battery2Bar
            in 26..35 -> Icons.Rounded.Battery3Bar
            in 36..65 -> Icons.Rounded.Battery4Bar
            in 66..75 -> Icons.Rounded.Battery5Bar
            in 76..95 -> Icons.Rounded.Battery6Bar
            in 96..100 -> Icons.Rounded.BatteryFull
            else -> Icons.AutoMirrored.Rounded.BatteryUnknown
        }
    } else Icons.Rounded.BatteryChargingFull

    fun getBatteryIconColor() = if (batteryLevel <= 20) DRONE_BATTERY_LOW_COLOR else DRONE_BATTERY_NORMAL_COLOR

    val currentCargoMass: Double get() = cargos.sumOf { it.weight }

}
