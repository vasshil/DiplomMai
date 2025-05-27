package ui.compose.city_creator.widgets.side_panel.delivery_panel.cargos

import com.jme3.math.Vector3f

sealed class CargoPoints {

    data object Idle : CargoPoints()

    data object Waiting1 : CargoPoints()

    data class Waiting2(val start: Vector3f) : CargoPoints()

    data class TwoPointSelected(val start: Vector3f, val destination: Vector3f) : CargoPoints()

}