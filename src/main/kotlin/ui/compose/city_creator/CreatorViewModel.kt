package ui.compose.city_creator

import com.jme3.math.Vector3f
import core.algo.DroneRoutingManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import model.FlyMap
import model.cargo.Cargo
import model.drone.Drone
import model.landscape.Building
import model.landscape.NoFlyZone
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer

class CreatorViewModel {

    private val scope = CoroutineScope(Dispatchers.IO)

    val flyMapFlow = MutableStateFlow(FlyMap())

    val openSetFlow = MutableStateFlow<List<Vector3f>?>(null)

    val droneRoutingManager = DroneRoutingManager(this) {
        scope.launch { openSetFlow.emit(it) }
    }


    private var sendSocket: Socket? = null

    init {
        scope.launch(Dispatchers.IO) {
            val serverSocket = ServerSocket(12345)
            while (true) {
                println("Ожидание клиента...")
                val client = serverSocket.accept()
                println("Клиент подключился: ${client.inetAddress}")
                scope.launch(Dispatchers.IO) {
                    try {
                        val out = client.getOutputStream()
                        while (true) {
                            val bytes = ByteArrayOutputStream().use { buf ->
                                ObjectOutputStream(buf).use { oos -> oos.writeObject(flyMapFlow.value) }
                                buf.toByteArray()
                            }
                            val len = bytes.size
                            out.write(ByteBuffer.allocate(4).putInt(len).array()) // отправляем длину (4 байта)
                            out.write(bytes) // отправляем объект
                            out.flush()
                            delay(200) // раз в секунду, например
                        }
                    } catch (e: Exception) {
                        println("Клиент отключился: ${e.stackTraceToString()}")
                    } finally {
                        try { client.close() } catch (_: Exception) {}
                    }
                }
            }

            while (true) {

//                try {
//                    if (sendSocket == null || sendSocket?.isConnected != true) {
//                        println("!!!!!!!!!!!!!!!! sendSocket == null ${sendSocket == null} / sendSocket?.isConnected $sendSocket?.isConnected")
//                        sendSocket = ServerSocket(12345).accept()
//                        println("connection accepted!! ${sendSocket}")
//                    }
//                } catch (e: Exception) {
//                    println("open socket error: ${e.stackTraceToString()}")
//                    try {
//                        sendSocket?.close()
//                        sendSocket = null
//                    } catch (e: Exception) {}
//                }
//
//                delay(1000)
            }
        }
//        scope.launch(Dispatchers.IO) {
//            flyMapFlow.collectLatest {
//                withContext(Dispatchers.IO) {
//                    try {
//                        if (sendSocket != null) {
//                            sendSocket?.getOutputStream()?.write(
//                                ByteArrayOutputStream().use { buf ->
//                                    ObjectOutputStream(buf).use { oos -> oos.writeObject(it) }
//                                    buf.toByteArray()
//                                }
//                            )
//                            println("send success!!")
//                        } else {
////                            throw Exception("open socket error")
//                        }
//
//                    } catch (e: Exception) {
//                        println("send err: ${e.stackTraceToString()}")
//                        try {
//                            sendSocket?.close()
//                            sendSocket = null
//                        } catch (e: Exception) {}
//                    }
//
//                }
//            }
//        }
    }


    fun setFlyMap(flyMap: FlyMap) {
        scope.launch {
            flyMapFlow.emit(flyMap)
        }
    }



    fun newBuilding(): Long {
        val newId = flyMapFlow.value.nextBuildingId()
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


    fun updateFlyMap(flyMap: FlyMap) {
        flyMapFlow.value = flyMap
    }

    fun updateDrone(drone: Drone) {
        flyMapFlow.value = flyMapFlow.value.copy(
            drones = flyMapFlow.value.drones.map { d ->
                if (d.id == drone.id) drone else d
            }.toMutableList()
        )
    }

    fun updateCargo(cargo: Cargo) {
        flyMapFlow.value = flyMapFlow.value.copy(
            cargos = flyMapFlow.value.cargos.map { c ->
                if (c.timeCreation == cargo.timeCreation) cargo else c
            }.toMutableList()
        )
    }

    fun destroy() {
        scope.cancel()
        sendSocket?.close()
    }

}
