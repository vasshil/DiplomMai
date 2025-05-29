package core.algo

import com.jme3.math.LineSegment
import com.jme3.math.Vector3f
import kotlinx.coroutines.*
import model.FlyMap
import model.cargo.CargoStatus
import model.drone.Drone
import model.drone.DroneStatus
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Envelope
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Polygon
import org.locationtech.jts.index.strtree.STRtree
import kotlin.math.max

class DroneRoutingManager(private val flyMap: FlyMap) {
    private var routingJob: Job? = null
    private var isRunning = false
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val speed = 5f // условная скорость дрона (м/с)
    private val geometryFactory = GeometryFactory()
    private val obstacleIndex = STRtree()

    init {
        buildObstacleIndex()
    }

    fun start() {
        if (isRunning) return
        isRunning = true
        routingJob = coroutineScope.launch {
            while (isActive && isRunning) {
                routeDrones()
                moveDrones()
                delay(1000)
            }
        }
    }

    fun stop() {
        isRunning = false
        routingJob?.cancel()
    }

    private fun buildObstacleIndex() {
        flyMap.buildings.forEach { building ->
            val poly = building.toJTSPolygon()
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
                drone.currentWayPoint.clear()
                drone.currentWayPoint.addAll(totalPath)
                drone.status = DroneStatus.DELIVERING
                drone.cargos.add(selectedCargo)
                selectedCargo.status = CargoStatus.ON_ROAD
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
            if (current.distance(end) < 1f) return (reconstructPath(cameFrom, current) + end).toMutableList()

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
        val step = 10f
        val directions = listOf(
            Vector3f(step, 0f, 0f), Vector3f(-step, 0f, 0f),
            Vector3f(0f, 0f, step), Vector3f(0f, 0f, -step),
            Vector3f(step, 0f, step), Vector3f(-step, 0f, -step),
            Vector3f(step, 0f, -step), Vector3f(-step, 0f, step)
        )
        return directions.map { Vector3f(point).add(it) }
    }

    private fun isLineBlocked(from: Vector3f, to: Vector3f): Boolean {
        val coord1 = Coordinate(from.x.toDouble(), from.z.toDouble())
        val coord2 = Coordinate(to.x.toDouble(), to.z.toDouble())
        val seg = org.locationtech.jts.geom.LineSegment(coord1, coord2)
        val envelope = Envelope(coord1, coord2)
        val hits = obstacleIndex.query(envelope)
        return hits.any { (it as Polygon).intersects(seg.toGeometry(geometryFactory)) }
    }

//    private fun LineSegment.toGeometry(factory: GeometryFactory) = factory.createLineString(arrayOf(p0, p1))

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

            if (distanceToTarget <= speed) {
                drone.currentPosition.set(target)
                drone.currentWayPoint.removeAt(0)
                if (drone.currentWayPoint.isEmpty()) completeDroneMission(drone)
            } else {
                direction.normalize().mult(speed)
                drone.currentPosition.add(direction)
                drone.batteryLevel = max(0, drone.batteryLevel - 1)
            }
        }
    }

    private fun completeDroneMission(drone: Drone) {
        drone.cargos.forEach { it.status = CargoStatus.DONE }
        drone.cargos.clear()
        drone.status = DroneStatus.RETURNING
        drone.status = DroneStatus.WAITING
    }

    private fun findNearestChargeStation(from: Vector3f): Vector3f {
        return flyMap.buildings.flatMap { it.safeDistanceCoords }
            .filter { it.isChargeStation }
            .minByOrNull { v -> distance(from, v.position) }?.position ?: Vector3f(0f, 0f, 0f)
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

    private fun Double.roundToInt(): Int = roundToInt()
}