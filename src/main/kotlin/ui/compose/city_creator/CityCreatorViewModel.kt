package ui.compose.city_creator

import com.jme3.math.Vector3f
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.City
import model.cargo.Cargo
import model.drone.Drone
import model.drone.DroneStatus
import model.landscape.Building

class CityCreatorViewModel {

    private val scope = CoroutineScope(Dispatchers.IO)

    val cityFlow = MutableStateFlow(City()
        .apply {
            drones = mutableListOf(
                Drone(id = 0, status = DroneStatus.CHARGING, maxCargoCapacityMass = 4, cargos = mutableListOf(Cargo(0, 3.4)), currentPosition = Vector3f(30f, 40f, 20f)),
                Drone(id = 1, status = DroneStatus.WAITING, batteryLevel = 10, maxCargoCapacityMass = 2, cargos = mutableListOf(Cargo(0, 0.4)), currentPosition = Vector3f(30f, 40f, 20f)),
                Drone(id = 2, status = DroneStatus.WAITING, batteryLevel = 90, maxCargoCapacityMass = 5, cargos = mutableListOf(Cargo(0, 5.0)), currentPosition = Vector3f(30f, 40f, 20f)),
            )
        }

    )

    fun setCity(city: City) {
        scope.launch {
            cityFlow.emit(city)
        }
    }

    fun updateBuilding(building: Building) {
        cityFlow.update { city ->
            city.copy(
                buildings = city.buildings.map { b ->
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


}