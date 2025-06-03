package ui.compose.city_creator.widgets.side_panel.delivery_panel.drones

import com.jme3.math.Vector3f

sealed class DroneStartPoint {

    data object Idle : DroneStartPoint()

    data object WaitingStartPoint : DroneStartPoint()

    data class StartPointSelected(val start: Vector3f) : DroneStartPoint()

}