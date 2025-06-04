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
    private val obstacleIndex = STRtree()

    init {
//        buildObstacleIndex()
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
                moveDrones()
                delay(100)
            }
        }
    }

    fun stop() {
        isRunning = false
        routingJob?.cancel()
    }

    private fun buildObstacleIndex() {
        flyMap.buildings.forEach { building ->
            val poly = building.toSafeJTSPolygon()
            obstacleIndex.insert(poly.envelopeInternal, poly)
        }
        flyMap.noFlyZones.filter { it.isActive }.forEach { zone ->
            val coords = zone.groundCoords.map { Coordinate(it.x.toDouble(), it.z.toDouble()) } +
                    Coordinate(zone.groundCoords.first().x.toDouble(), zone.groundCoords.first().z.toDouble())
            val poly = geometryFactory.createPolygon(coords.toTypedArray())
            obstacleIndex.insert(poly.envelopeInternal, poly)
        }
        obstacleIndex.build()

    }

    private fun routeDrones() {
        val availableDrones = flyMap.drones.filter { it.status == DroneStatus.WAITING && it.batteryLevel > 20 && it.currentWayPoint.isEmpty() }
        val availableCargos = flyMap.cargos.filter { it.status == CargoStatus.WAITING }

        for (drone in availableDrones) {
            val suitableCargos = availableCargos.filter { it.weight <= drone.maxCargoCapacityMass }
            if (suitableCargos.isEmpty()) continue

            val selectedCargo = suitableCargos.minByOrNull { cargo ->
                distance(drone.currentPosition, cargo.startVertex)
            } ?: continue

            val pathToPickup = findPath(drone.currentPosition, selectedCargo.startVertex)
            val pathToDropoff = findPath(selectedCargo.startVertex, selectedCargo.destination)
            val pathToCharger = findPath(selectedCargo.destination, findNearestChargeStation(selectedCargo.destination))

            val totalPath = pathToPickup + pathToDropoff + pathToCharger
            val requiredBattery = estimateBatteryUsage(drone, totalPath, selectedCargo.weight)

            if (drone.batteryLevel >= requiredBattery) {
                val updDrone = drone.copy(
                    currentWayPoint = totalPath,
                    status = DroneStatus.DELIVERING,
                    cargos = drone.cargos + selectedCargo,
                )
                val updCargo = selectedCargo.copy(
                    status = CargoStatus.IN_WORK
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
        val step = 0.5f
        val directions = listOf(
            Vector3f(step, 0f, 0f),    // вправо
            Vector3f(-step, 0f, 0f),   // влево
            Vector3f(0f, 0f, step),    // вперёд
            Vector3f(0f, 0f, -step),   // назад
            Vector3f(step, 0f, step),  // вправо-вперёд (диагональ)
            Vector3f(-step, 0f, step), // влево-вперёд (диагональ)
            Vector3f(step, 0f, -step), // вправо-назад (диагональ)
            Vector3f(-step, 0f, -step) // влево-назад (диагональ)
        )
        return directions.map { Vector3f(point).add(it) }
    }

    private fun isLineBlocked(from: Vector3f, to: Vector3f): Boolean {
        val coord1 = Coordinate(from.x.toDouble(), from.z.toDouble())
        val coord2 = Coordinate(to.x.toDouble(), to.z.toDouble())
        val line = geometryFactory.createLineString(arrayOf(coord1, coord2))
        val envelope = Envelope(coord1, coord2)
        val hits = obstacleIndex.query(envelope)
//        return hits.any { (it as Polygon).intersects(seg.toGeometry(geometryFactory)) }

        for (hit in hits) {
            val polygon = hit as Polygon
//            if ((polygon.contains(line) || polygon.crosses(line)) && !polygon.boundary.covers(line)) {
//                return true
//            }
//            if (polygon.contains(line)) return true // запрещаем, если линия внутри
//            if (polygon.covers(line) && !polygon.boundary.covers(line)) return true // запрещаем, если линия строго внутри
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
        for (drone in flyMap.drones.filter { it.currentWayPoint.isNotEmpty() }) {
            val target = drone.currentWayPoint.first()
            val direction = Vector3f(target).subtract(drone.currentPosition)
            val distanceToTarget = direction.length()

            var updDrone = drone.copy(
                currentPosition = target,
                currentWayPoint = drone.currentWayPoint.subList(1, drone.currentWayPoint.size)
            )

            var updCargo: Cargo? = drone.cargos.firstOrNull()

            if (distanceToTarget <= speed) {

                if (updDrone.currentWayPoint.isEmpty()) {
                    val (d, c) = completeDroneMission(drone)
                    updDrone = d
                    updCargo = c
                } else if (drone.currentWayPoint.size == 1) {
                    // Последняя точка маршрута — зарядная станция
                    updDrone = updDrone.copy(
                        status = DroneStatus.RETURNING
                    )
                }
            } else {
                direction.normalize().mult(speed)
                updDrone = updDrone.copy(
                    batteryLevel = max(0, updDrone.batteryLevel - 1),
                    currentPosition = updDrone.currentPosition.add(direction)
                )
            }
            viewModel.updateDrone(updDrone)
            updCargo?.let {
                viewModel.updateCargo(it)
            }
        }

    }

    private fun completeDroneMission(drone: Drone): Pair<Drone, Cargo?> {
        val updDroneAndCargo = if (drone.cargos.isNotEmpty()) {
            Pair(
                drone.copy(
                    cargos = drone.cargos.subList(1, drone.cargos.size),
                    status = DroneStatus.RETURNING
                ),
                drone.cargos.first().copy(
                    status = CargoStatus.DONE
                )
            )
        } else if (drone.batteryLevel < 100) {
            Pair(
                drone.copy(
                    batteryLevel = min(100, drone.batteryLevel + 10), // имитация зарядки
                    status = DroneStatus.CHARGING
                ),
                drone.cargos.firstOrNull()
            )
        } else {
            Pair(
                drone.copy(
                    status = DroneStatus.WAITING
                ),
                drone.cargos.firstOrNull()
            )
        }
        return updDroneAndCargo
    }

    private fun findNearestChargeStation(from: Vector3f): Vector3f {
        return flyMap.buildings.flatMap { it.safeDistanceCoords }
            .filter { it.isChargeStation }
            .minByOrNull { v -> distance(from, v.position) }?.position ?: Vector3f(0f, 0f, 0f)
//            .minByOrNull { v -> distance(from, v.position) }?.position ?: Vector3f(0f, 0f, 0f)
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

//    private fun Double.roundToInt(): Int = roundToInt()
}