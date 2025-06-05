package core.algo

import com.jme3.math.LineSegment
import com.jme3.math.Vector3f
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import model.FlyMap
import model.cargo.Cargo
import model.cargo.CargoStatus
import model.drone.Drone
import model.drone.DroneStatus
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Envelope
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Polygon
import org.locationtech.jts.index.strtree.STRtree
import ui.compose.city_creator.CreatorViewModel
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class DroneRoutingManager(private val viewModel: CreatorViewModel) {

    private var flyMap = viewModel.flyMapFlow.value

    private var routingJob: Job? = null
    private var isRunning = false
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val speed = 5f // условная скорость дрона (м/с)
    private val geometryFactory = GeometryFactory()
    private var obstacleIndex = STRtree()

    init {
        coroutineScope.launch {
            viewModel.flyMapFlow.collectLatest {
                if (flyMap.buildings != it.buildings || flyMap.noFlyZones != it.noFlyZones) {
                    println("buildObstacleIndex")
                    flyMap = it
                    buildObstacleIndex()
                } else {
                    flyMap = it
                }
            }
        }
    }

    fun start() {
        if (isRunning) return
        isRunning = true
        routingJob = coroutineScope.launch {
            while (isRunning) {
                routeDrones()
                try {
                    moveDrones()
                } catch (_: Exception) {}
                delay(50)
            }
        }
    }

    fun stop() {
        isRunning = false
        routingJob?.cancel()
    }

    private fun buildObstacleIndex() {
        obstacleIndex = STRtree()
        flyMap.buildings.forEach { building ->
            if (building.groundCoords.size > 1) {
                val poly = building.toSafeJTSPolygon()
                obstacleIndex.insert(poly.envelopeInternal, poly)
            }

        }
        flyMap.noFlyZones.filter { it.isActive }.forEach { zone ->
            if (zone.groundCoords.size > 1) {
                val coords = zone.groundCoords.map { Coordinate(it.x.toDouble(), it.z.toDouble()) } +
                        Coordinate(zone.groundCoords.first().x.toDouble(), zone.groundCoords.first().z.toDouble())
                val poly = geometryFactory.createPolygon(coords.toTypedArray())
                obstacleIndex.insert(poly.envelopeInternal, poly)
            }

        }
        obstacleIndex.build()

    }

    private fun routeDrones() {
        val availableDrones = flyMap.drones.filter {
            (it.status == DroneStatus.WAITING || it.status == DroneStatus.CHARGING) &&
                    it.batteryLevel > 20 &&
                    it.currentWayPoint.isEmpty() }
        val availableCargos = flyMap.cargos.filter { it.status == CargoStatus.WAITING }

        for (drone in availableDrones) {
            val suitableCargos = availableCargos.filter { it.weight <= drone.maxCargoCapacityMass }
            if (suitableCargos.isEmpty()) continue

            val selectedCargo = suitableCargos.minByOrNull { cargo ->
                distance(drone.currentPosition, cargo.startVertex)
            } ?: continue

            if (flyMap.getCargoByDroneId(drone.id)?.timeCreation == selectedCargo.timeCreation) continue

            val pathToPickup = findPath(drone.currentPosition, selectedCargo.startVertex)
            val pathToDropoff = findPath(selectedCargo.startVertex, selectedCargo.destination)
            val pathToCharger = findPath(selectedCargo.destination, findNearestChargeStation(selectedCargo.destination) ?: selectedCargo.destination)

            val totalPath = pathToPickup + pathToDropoff + pathToCharger
            val requiredBattery = estimateBatteryUsage(drone, totalPath, selectedCargo.weight)

            if (drone.batteryLevel >= requiredBattery) {
                val updDrone = drone.copy(
                    currentWayPoint = pathToPickup,
                    status = DroneStatus.DELIVERING,
                    roadToCargoStart = pathToPickup,
                    roadToCargoDestination = pathToDropoff,
                    roadToCargoChargeStation = pathToCharger,
                )
                val updCargo = selectedCargo.copy(
                    status = CargoStatus.IN_WORK,
                    droneId = drone.id
                )

                viewModel.updateDrone(updDrone)
                viewModel.updateCargo(updCargo)
            }
        }
    }

    private fun findPath(start: Vector3f, end: Vector3f): MutableList<Vector3f> {
        val openSet = mutableListOf(start)
        val cameFrom = mutableMapOf<Vector3f, Vector3f?>()
        val gScore = mutableMapOf(start to 0.0)
        val fScore = mutableMapOf(start to heuristic(start, end))

        while (openSet.isNotEmpty()) {
            val current = openSet.minByOrNull { fScore[it] ?: Double.POSITIVE_INFINITY } ?: break
            if (current.distance(end) <= 1f) return (reconstructPath(cameFrom, current) + end).toMutableList()

            openSet.remove(current)

            val neighbors = generateNeighbors(current)
            for (neighbor in neighbors) {
                if (isLineBlocked(current, neighbor)) continue
                val tentativeG = gScore[current]!! + current.distance(neighbor)
                if (tentativeG < (gScore[neighbor] ?: Double.POSITIVE_INFINITY)) {
                    cameFrom[neighbor] = current
                    gScore[neighbor] = tentativeG
                    fScore[neighbor] = tentativeG + heuristic(neighbor, end)
                    if (neighbor !in openSet) openSet.add(neighbor)
                }
            }
        }

        return mutableListOf(start, end) // fallback
    }

    private fun generateNeighbors(point: Vector3f): List<Vector3f> {
        val step = 1f
        val directions = mutableListOf(
            Vector3f(step, 0f, 0f),    // вправо
            Vector3f(-step, 0f, 0f),   // влево
            Vector3f(0f, 0f, step),    // вперёд
            Vector3f(0f, 0f, -step),   // назад
            Vector3f(step, 0f, step),  // вправо-вперёд (диагональ)
            Vector3f(-step, 0f, step), // влево-вперёд (диагональ)
            Vector3f(step, 0f, -step), // вправо-назад (диагональ)
            Vector3f(-step, 0f, -step) // влево-назад (диагональ)
        )

        // Дополнительно: проверяем прямую видимость до всех безопасных вершин
//        val st = System.currentTimeMillis()
//        val keyNodes = flyMap.buildings.flatMap { it.safeDistanceCoords }
//        keyNodes.forEach { node ->
//            if (!isLineBlocked(point, node.position)) {
//                directions.add(node.position)
//            }
//        }
//        println("time ${System.currentTimeMillis() - st}")
        return directions.map { Vector3f(point).add(it) }
    }

    private fun isLineBlocked(from: Vector3f, to: Vector3f): Boolean {
        val coord1 = Coordinate(from.x.toDouble(), from.z.toDouble())
        val coord2 = Coordinate(to.x.toDouble(), to.z.toDouble())
        val line = geometryFactory.createLineString(arrayOf(coord1, coord2))
        val envelope = Envelope(coord1, coord2)
        val hits = obstacleIndex.query(envelope)

        for (hit in hits) {
            val polygon = hit as Polygon
            // Проверяем, содержится ли линия ВНУТРИ полигона (без касания границ)
            if (polygon.contains(line)) {
                return true // пересечение запрещено
            }
        }
        return false // пересечений нет, либо линия только касается границы
    }

    private fun heuristic(a: Vector3f, b: Vector3f): Double {
        return a.distance(b).toDouble()
    }

    private fun reconstructPath(cameFrom: Map<Vector3f, Vector3f?>, current: Vector3f): MutableList<Vector3f> {
        val path = mutableListOf(current)
        var node = current
        while (cameFrom[node] != null) {
            node = cameFrom[node]!!
            path.add(0, node)
        }
        return path
    }

    private fun moveDrones() {
        for (drone in flyMap.drones) {
            if (drone.status == DroneStatus.CHARGING) {
                // заряжаемся
                viewModel.updateDrone(
                    drone.copy(
                        status = if (drone.batteryLevel < 100) DroneStatus.CHARGING else DroneStatus.WAITING,
                        batteryLevel = if (drone.batteryLevel < 100) drone.batteryLevel + 1 else 100,
                    )
                )
            } else {
                if (drone.currentWayPoint.isNotEmpty()) {
                    // перемещаемся по текущему маршруту

                    val target = drone.currentWayPoint.first()

                    val updDrone = drone.copy(
                        currentPosition = target,
                        currentWayPoint = drone.currentWayPoint.subList(1, drone.currentWayPoint.size)
//                        batteryLevel = max(0, updDrone.batteryLevel - 1),
                    )

                    viewModel.updateDrone(updDrone)

                } else {
                    if (drone.roadToCargoDestination.isNotEmpty() && drone.currentPosition.distance(drone.roadToCargoDestination.first()) <= 1) {
                        // переходит на начало доставки

                        println("drone ${drone.id} start delivery")

                        val cargoInWork: Cargo? = flyMap.getCargoByDroneId(drone.id)

                        viewModel.updateDrone(drone.copy(
                            currentWayPoint = drone.roadToCargoDestination,
                            roadToCargoStart = emptyList(),
                            status = DroneStatus.DELIVERING,
                            cargos = cargoInWork?.let { listOf(cargoInWork) } ?: drone.cargos
//                            cargos = cargoInWork?.let { drone.cargos + cargoInWork } ?: drone.cargos
                        ))

                        flyMap.getCargoByDroneId(drone.id)?.let {
                            viewModel.updateCargo(it.copy(
                                status = CargoStatus.ON_ROAD
                            ))
                        }

                    }
                    else if (drone.currentPosition.distance(drone.roadToCargoChargeStation.first()) <= 1 && drone.status == DroneStatus.DELIVERING) {

                        println("drone ${drone.id} finish delivery, go to charging ${drone.roadToCargoChargeStation.toTypedArray().contentToString()}")

                        // на зарядку
                        viewModel.updateDrone(drone.copy(
                            currentWayPoint = drone.roadToCargoChargeStation,
                            roadToCargoDestination = emptyList(),
                            status = DroneStatus.RETURNING,
                            cargos = emptyList(),
                        ))
                        drone.cargos.firstOrNull()?.let {
                            viewModel.updateCargo(it.copy(
                                status = CargoStatus.DONE,
                                droneId = -1
                            ))
                        }


                        if (findNearestChargeStation(drone.currentPosition) == null) {
                            // нет зарядных станций
                            viewModel.updateDrone(drone.copy(
                                status = DroneStatus.WAITING,
                                currentWayPoint = emptyList(),
                                roadToCargoStart = emptyList(),
                                roadToCargoDestination = emptyList(),
                                roadToCargoChargeStation = emptyList(),
                                cargos = emptyList()
                            ))
                        }
                    } else if (findNearestChargeStation(drone.currentPosition) != null &&
                        drone.currentPosition.distance(findNearestChargeStation(drone.currentPosition)) <= 1 &&
                        drone.status == DroneStatus.RETURNING &&
                        drone.cargos.isEmpty()) {

                        println("drone ${drone.id} charging")

                        // заряжаемся
                        viewModel.updateDrone(drone.copy(
                            status = DroneStatus.CHARGING,
                            currentWayPoint = emptyList(),
                            roadToCargoStart = emptyList(),
                            roadToCargoDestination = emptyList(),
                            roadToCargoChargeStation = emptyList()
                        ))

                    }

                }

            }

        }

    }

    private fun findNearestChargeStation(from: Vector3f): Vector3f? {
        return flyMap.buildings.flatMap { it.safeDistanceCoords }
            .filter { it.isChargeStation }
            .minByOrNull { v -> distance(from, v.position) }?.position
    }

    private fun distance(a: Vector3f, b: Vector3f): Float {
        return a.distance(b)
    }

    private fun estimateBatteryUsage(drone: Drone, route: List<Vector3f>, cargoWeight: Double): Int {
        var totalUsage = 0.0
        var last = drone.currentPosition
        for (point in route) {
            val segmentLength = distance(last, point)
            val weightFactor = 1 + (cargoWeight / drone.maxCargoCapacityMass)
            totalUsage += segmentLength * weightFactor / 10.0
            last = point
        }
        return totalUsage.roundToInt()
    }

}