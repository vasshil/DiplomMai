package ui.compose.city_creator

import com.jme3.math.Vector3f
import core.algo.DroneRoutingManager
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

    val flyMapFlow = MutableStateFlow(FlyMap())

    val droneRoutingManager = DroneRoutingManager(flyMapFlow)

    fun setCity(flyMap: FlyMap) {
        scope.launch {
            flyMapFlow.emit(flyMap)
        }
    }



    fun newBuilding(): Long {
        val newId = flyMapFlow.value.nextDroneId()
        val newBuilding = Building(newId)
        flyMapFlow.update {
            it.copy(buildings = it.buildings + newBuilding)
        }
        return newId
    }

    fun removeBuilding(id: Long) {
        flyMapFlow.update {
            it.copy(buildings = it.buildings.toMutableList().apply {
                removeIf { b ->
                    b.id == id
                }
            })
        }
    }

    fun removeLastBuilding() {
        flyMapFlow.update {
            it.copy(buildings = it.buildings.toMutableList().apply {
                removeLast()
            })
        }
    }

    fun addBuildingGroundPoint(buildingId: Long, x: Float, z: Float) {
        flyMapFlow.update {
            it.copy(buildings = it.buildings.toMutableList().apply {
                replaceAll { b ->
                    if (b.id == buildingId) {
                        b.copy(groundCoords = b.groundCoords + Vector3f(x, 0f, z))
                    } else b
                }
            })
        }
    }

    fun finishBuilding(buildingId: Long) {
        flyMapFlow.value.buildings.firstOrNull { b ->
            b.id == buildingId
        }?.let { b ->
            var groundCoords = b.groundCoords
            if (groundCoords.size > 1) {
                if (groundCoords.first() != groundCoords.last()) {
                    groundCoords = groundCoords + groundCoords.first()
                }
                var newBuilding = b.copy(groundCoords = groundCoords)
                newBuilding = newBuilding.copy(safeDistanceCoords = newBuilding.getKeyNodes())
                flyMapFlow.update {
                    val newBuildings = it.buildings.toMutableList().apply {
                        replaceAll { b ->
                            if (b.id == buildingId) {
                                newBuilding
                            } else b
                        }
                    }
                    it.copy(buildings = newBuildings)
                }

            }
        }

    }


    fun newNFZ(): Long {
        val newId = flyMapFlow.value.nextNFZId()
        val newNFZ = NoFlyZone(newId)
        flyMapFlow.update {
            it.copy(noFlyZones = it.noFlyZones + newNFZ)
        }
        return newId
    }

    fun removeNFZ(id: Long) {
        flyMapFlow.update {
            it.copy(noFlyZones = it.noFlyZones.toMutableList().apply {
                removeIf { nfz ->
                    nfz.id == id
                }
            })
        }
    }

    fun removeLastNFZ() {
        flyMapFlow.update {
            it.copy(noFlyZones = it.noFlyZones.toMutableList().apply {
                removeLast()
            })
        }
    }

    fun addNFZGroundPoint(nfzId: Long, x: Float, z: Float) {
        flyMapFlow.update {
            it.copy(noFlyZones = it.noFlyZones.toMutableList().apply {
                replaceAll { nfz ->
                    if (nfz.id == nfzId) {
                        nfz.copy(groundCoords = nfz.groundCoords + Vector3f(x, 0f, z))
                    } else nfz
                }
            })
        }
    }

    fun finishNFZ(nfzId: Long) {
        flyMapFlow.value.noFlyZones.firstOrNull { nfz ->
            nfz.id == nfzId
        }?.let { nfz ->
            var groundCoords = nfz.groundCoords
            if (groundCoords.size > 1) {
                if (groundCoords.first() != groundCoords.last()) {
                    groundCoords = groundCoords + groundCoords.first()
                }
                val newNFZ = nfz.copy(groundCoords = groundCoords)
                flyMapFlow.update {
                    val newNFZs = it.noFlyZones.toMutableList().apply {
                        replaceAll { nfz ->
                            if (nfz.id == nfzId) {
                                newNFZ
                            } else nfz
                        }
                    }
                    it.copy(noFlyZones = newNFZs)
                }

            }
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
                noFlyZones = flyMap.noFlyZones.map {
                    if (it.id == nfz.id) nfz else it
                }
            )
        }
    }


    fun addDrone(drone: Drone) {
        flyMapFlow.update {
            it.copy(drones = it.drones + drone)
        }
    }


    fun addCargo(cargo: Cargo) {
        flyMapFlow.update {
            it.copy(cargos = it.cargos + cargo)
        }
    }


}