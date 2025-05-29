package ui.compose.city_creator

import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.TrayState
import androidx.compose.ui.window.rememberNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.FlyMap
import model.cargo.Cargo
import model.drone.Drone
import model.landscape.Building
import model.landscape.NoFlyZone

class CreatorViewModel() {

    private val scope = CoroutineScope(Dispatchers.IO)

    val flyMapFlow = MutableStateFlow(FlyMap()
        .apply {
            drones = mutableListOf(
//                Drone(id = 0, status = DroneStatus.CHARGING, maxCargoCapacityMass = 4f, cargos = mutableListOf(Cargo(0, 3.4)), currentPosition = Vector3f(30f, 40f, 20f)),
//                Drone(id = 1, status = DroneStatus.WAITING, batteryLevel = 10, maxCargoCapacityMass = 2f, cargos = mutableListOf(Cargo(0, 0.4)), currentPosition = Vector3f(30f, 40f, 20f)),
//                Drone(id = 2, status = DroneStatus.WAITING, batteryLevel = 90, maxCargoCapacityMass = 5f, cargos = mutableListOf(Cargo(0, 5.0)), currentPosition = Vector3f(30f, 40f, 20f)),
            )
        }

    )

    fun setCity(flyMap: FlyMap) {
        scope.launch {
            flyMapFlow.emit(flyMap)
        }
    }

    fun updateBuilding(building: Building) {
        flyMapFlow.update { flyMap ->
            flyMap.copy(
                buildings = flyMap.buildings.map { b ->
                    if (b.id == building.id) building else b
                }.toMutableList()
            )
        }
//        scope.launch {
//            cityFlow.emit (//{ city ->
//                cityFlow.value.copy(
//                    buildings = cityFlow.value.buildings.map { b ->
//                        if (b.id == building.id) building else b
//                    }.toMutableList()
//                )
//            )
//        }

    }

    fun updateNoFlyZone(nfz: NoFlyZone) {
        println("upd nfz old ${flyMapFlow.value.noFlyZones} / new  $nfz")
        flyMapFlow.update { flyMap ->
            flyMap.copy(
                noFlyZones = flyMap.noFlyZones.apply {
                    this.replaceAll {
                        if (it.id == nfz.id) nfz else it
                    }
                }
            )
        }
    }

    fun addDrone(drone: Drone) {
        flyMapFlow.update {
            it.apply {
                this.drones += drone
            }
        }
    }

    fun addCargo(cargo: Cargo) {
        flyMapFlow.update {
            it.apply {
                this.cargos += cargo
            }
        }
    }


}