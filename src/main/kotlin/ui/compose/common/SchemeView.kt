package ui.compose.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.jme3.math.Vector3f
import core.algo.LazyGraph3D
import core.algo.fastestPath3D
import core.distanceBetween
import core.to3D
import model.FlyMap
import model.graph.FlyMapVertex
import model.landscape.Building
import ui.compose.city_creator.CreatorViewModel
import ui.compose.city_creator.widgets.side_panel.delivery_panel.cargos.CargoPoints
import ui.compose.city_creator.widgets.side_panel.delivery_panel.drones.DroneStartPoint
import ui.compose.city_creator.widgets.topbar.CreatorModeEnum
import kotlin.math.hypot
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SchemeView(
    modifier: Modifier = Modifier,
    viewModel: CreatorViewModel,
    flyMap: FlyMap,
    cityCreatorMode: CreatorModeEnum = CreatorModeEnum.NONE,
    isEditorMode: Boolean = false,
    focusedBuildingId: Long = -1,
    focusedNFZId: Long = -1,
    onClick: (() -> Unit) = {},
    showScaleButtons: Boolean = true,
    droneStartPoint: DroneStartPoint,
    onDroneStartPointChanged: (DroneStartPoint) -> Unit,
    cargoPoints: CargoPoints,
    onCargoPointsChanged: (CargoPoints) -> Unit,
    onMouseAction: (position: Offset, pressed: Boolean) -> Unit = { _, _ -> },
) {


    var mouseCoordinate by remember { mutableStateOf(Offset.Zero) }

    var scale by remember { mutableFloatStateOf(10f) } // Масштаб для перевода координат в пиксели
    val gridStep by remember { derivedStateOf { 10 * scale } }  // Шаг сетки
    var offset by remember { mutableStateOf(Offset(gridStep, gridStep)) }  // Смещение в одну клетку в левом верхнем углу

    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        scale *= zoomChange
//        rotation += rotationChange
        offset += offsetChange
    }

    // Состояния для хранения двух выбранных вершин
//    var selectedVertex1 by remember { mutableStateOf<FlyMapVertex?>(null) }
//    var selectedVertex2 by remember { mutableStateOf<FlyMapVertex?>(null) }
//    var shortestPath by remember { mutableStateOf<List<FlyMapVertex>>(emptyList()) }

    Box(
        modifier = modifier
    ) {

        Canvas(
            modifier = Modifier.fillMaxSize()
                .background(color = SCHEME_BACKGROUND_COLOR)
                .clipToBounds()
                .pointerInput(flyMap, isEditorMode, focusedBuildingId, droneStartPoint, cargoPoints) {
                    detectTapGestures { tapOffset ->

                        onClick()

                        if (isEditorMode) return@detectTapGestures

                        // Переводим координаты клика в координаты графа с учетом смещения и масштаба
                        val clickedPosition = Vector3f(
                            (tapOffset.x - offset.x) / scale,
                            10f,
                            (tapOffset.y - offset.y) / scale,
                        )

                        // Определяем ближайшую вершину к месту клика
                        val nearestVertex = flyMap.buildings.map {
                            it.findNearestSafeVertex(clickedPosition)
                        }.minByOrNull {
                            distanceBetween(it?.position, clickedPosition)
                        }
//                        println("nearest: $clickedPosition / $nearestVertex / ${flyMap}")

                        nearestVertex?.let { vertex ->

                            println("nearest: $cargoPoints / $nearestVertex / ${droneStartPoint} ")
                            if (cargoPoints is CargoPoints.Waiting1) {
                                onCargoPointsChanged(CargoPoints.Waiting2(vertex.position))
                            } else if (cargoPoints is CargoPoints.Waiting2) {
                                onCargoPointsChanged(CargoPoints.TwoPointSelected(cargoPoints.start, vertex.position))
                            } else if (droneStartPoint is DroneStartPoint.WaitingStartPoint) {
                                println(": $droneStartPoint / $nearestVertex  ")
                                onDroneStartPointChanged(DroneStartPoint.StartPointSelected(nearestVertex.position))
                            }
                            else {

//                                if (selectedVertex1 == null) {
//                                    selectedVertex1 = vertex
//                                }
//                                else if (selectedVertex2 == null && vertex != selectedVertex1) {
//                                    selectedVertex2 = vertex
//
//
//
//
//                                    // Находим кратчайший путь между двумя вершинами
////                                shortestPath = findShortestPathAStar(city.graph, selectedVertex1!!, selectedVertex2!!)
////                                val len = pathLength(shortestPath)
////                                println("Длина пути ${len} м, потраченный заряд: ${len / 10}%")
//
//
//
//
//                                    val bId1 = flyMap.buildings.find { it.getKeyNodes().contains(selectedVertex1!!) }
//                                    val bId2 = flyMap.buildings.find { it.getKeyNodes().contains(selectedVertex2!!) }
//
//                                    val start2D   = selectedVertex1
//                                    val goal2D    = selectedVertex2
//                                    val startYHgt    = 0f            // высота старта, м
//                                    val goalYHgt     = 0f            // высота финиша, м
//
//                                    // 1) уровни через каждый метр
//                                    val maxH  = maxOf(
//                                        startYHgt, goalYHgt,
//                                        flyMap.buildings.maxOfOrNull { it.height + Building.safeDistance } ?: 0f
//                                    ).toInt()
//                                    val levels = (0..maxH).map { it.toFloat() }
//
//                                    // 2) ленивый граф + поиск
//                                    val navigator = LazyGraph3D(flyMap, levels)
//                                    val route = fastestPath3D(
//                                        start2D!!.to3D(startYHgt, bId1!!.id),
//                                        goal2D!!.to3D(goalYHgt, bId2!!.id),
//                                        navigator
//                                    )
//
//                                    shortestPath = route.map { FlyMapVertex(it.buildingId, it.x, it.y, it.z) }
//
//
//                                }
//                                else {
//                                    // Сбрасываем выбор, если кликнули еще раз (начать с начала)
//                                    selectedVertex1 = null
//                                    selectedVertex2 = null
//                                    shortestPath = emptyList()
//                                }
                            }


                        }
                    }
                }
                .onPointerEvent(PointerEventType.Move) { event ->
                    val x = ((event.changes.first().position.x - offset.x) / scale).roundToInt()
                    val y = ((event.changes.first().position.y - offset.y) / scale).roundToInt()
                    onMouseAction(Offset(x.toFloat(), y.toFloat()), event.changes.first().pressed)
                    mouseCoordinate = Offset(x * scale + offset.x, y * scale + offset.y)
                }
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, rotation ->
                        println("$centroid, $zoom, $rotation $pan")
                        scale *= zoom
                    }
                }
                .transformable(state = state)
        ) {
            // Рисуем сетку
            for (x in 0..(size.width.toInt() / gridStep.toInt()) + 1) {
                drawLine(
                    color = MESH_COLOR,
                    start = Offset((offset.x % gridStep + x.toFloat() * gridStep), 0f),
                    end = Offset((offset.x % gridStep + x.toFloat() * gridStep), size.height),
                    strokeWidth = 1f
                )
            }
            for (y in 0..(size.height.toInt() / gridStep.toInt()) + 1) {
                drawLine(
                    color = MESH_COLOR,
                    start = Offset(0f, (y.toFloat() * gridStep + offset.y % gridStep)),
                    end = Offset(size.width, (y.toFloat() * gridStep + offset.y % gridStep)),
                    strokeWidth = 1f
                )
            }

            // draw axis
            drawLine(
                color = AXIS_COLOR,
                start = Offset(offset.x, 0f),
                end = Offset(offset.x, size.height),
                strokeWidth = 2f
            )
            drawLine(
                color = AXIS_COLOR,
                start = Offset(0f, offset.y),
                end = Offset(size.width, offset.y),
                strokeWidth = 2f
            )

            // Рисуем здания
            flyMap.buildings.forEach { building ->

                val path = Path().apply {
                    this.moveTo(building.groundCoords.first().x * scale + offset.x, building.groundCoords.first().z * scale + offset.y)
                    building.groundCoords.forEach { coordinate ->
                        lineTo(
                            coordinate.x * scale + offset.x,
                            coordinate.z * scale + offset.y
                        )
                    }
                }

                drawPath(
                    path = path,
                    color = if (focusedBuildingId == building.id) {
                        when(cityCreatorMode) {
                            CreatorModeEnum.REMOVE -> DELETE_FOCUSED_BUILDING_COLOR
                            else -> FOCUSED_BUILDING_COLOR
                        }
                    } else BUILDING_COLOR,
                    style = Fill
                )

                // безопасные вершины у здания

                for (i in 0 until building.safeDistanceCoords.size) {
                    val from = building.safeDistanceCoords[i]
                    val to = if (i < building.safeDistanceCoords.size - 1) building.safeDistanceCoords[i + 1]
                    else building.safeDistanceCoords.first()
                    val startX = from.position.x * scale + offset.x
                    val startY = from.position.z * scale + offset.y
                    val endX = to.position.x * scale + offset.x
                    val endY = to.position.z * scale + offset.y

                    drawLine(
                        color = EDGE_COLOR,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 2f
                    )

                }

                building.safeDistanceCoords.forEachIndexed { i, vertex ->
                    if (i == building.safeDistanceCoords.lastIndex) return@forEachIndexed

                    val x = vertex.position.x * scale + offset.x
                    val y = vertex.position.z * scale + offset.y

                    val r = if (hypot(x - mouseCoordinate.x, y - mouseCoordinate.y) <= 11) 12f else 8f

                    val color = if (vertex.isChargeStation) CHARGE_STATION_COLOR else KEY_POINT_COLOR
                    drawCircle(
                        color = color,
                        radius = r,
                        center = Offset(x, y)
                    )
                }

            }


            // бесполётные зоны
            flyMap.noFlyZones.forEach { nfz ->

                if (nfz.isActive) {

                    if (nfz.groundCoords.isNotEmpty()) {
                        val path = Path().apply {
                            this.moveTo(nfz.groundCoords.first().x * scale + offset.x, nfz.groundCoords.first().z * scale + offset.y)
                            nfz.groundCoords.forEach { coordinate ->
                                lineTo(
                                    coordinate.x * scale + offset.x,
                                    coordinate.z * scale + offset.y
                                )
                            }
                        }

                        drawPath(
                            path = path,
                            color = if (focusedNFZId == nfz.id && cityCreatorMode == CreatorModeEnum.REMOVE) {
                                NO_FLY_ZONE_FOCUSED_DELETE_FILL_COLOR
                            } else NO_FLY_ZONE_FILL_COLOR,
                            style = Fill
                        )
                        drawPath(
                            path = path,
                            color = if (focusedNFZId == nfz.id && cityCreatorMode != CreatorModeEnum.REMOVE) {
                                NO_FLY_ZONE_FOCUSED_BORDER_COLOR
                            } else NO_FLY_ZONE_BORDER_COLOR,
//                    if (focusedBuildingId == building.id) {
//                        when(cityCreatorMode) {
//                            CreatorModeEnum.REMOVE -> DELETE_FOCUSED_BUILDING_COLOR
//                            else -> FOCUSED_BUILDING_COLOR
//                        }
//                    } else BUILDING_COLOR,
                            style = Stroke(
                                width = if (focusedNFZId == nfz.id) 6f else 3f,
                                cap = StrokeCap.Round,
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(10f, 10f), // 10px линия, 10px пробел
                                    phase = 0f              // сдвиг, можно анимировать
                                )
                            )
                        )
                    }


                }

            }

            // место курсора
            drawLine(MOUSE_POINT_COLOR, Offset(mouseCoordinate.x - 10, mouseCoordinate.y), Offset(mouseCoordinate.x + 10, mouseCoordinate.y), 1f)
            drawLine(MOUSE_POINT_COLOR, Offset(mouseCoordinate.x, mouseCoordinate.y - 10), Offset(mouseCoordinate.x, mouseCoordinate.y + 10), 1f)

        }

        if (showScaleButtons) {

            Column(
                modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp)
            ) {

                IconButton(
                    modifier = Modifier.size(48.dp).background(color = SCALE_BUTTONS_BG_COLOR, shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)),
                    onClick = {
                        scale += 0.5f
                    }
                ) {

                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null, tint = Color.White)

                }

                IconButton(
                    modifier = Modifier.size(48.dp).background(color = SCALE_BUTTONS_BG_COLOR, shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)),
                    onClick = {
                        if (scale > 1) scale -= 0.5f
                    }
                ) {

                    Icon(imageVector = Icons.Rounded.Remove, contentDescription = null, tint = Color.White)

                }

            }
        }

    }


}