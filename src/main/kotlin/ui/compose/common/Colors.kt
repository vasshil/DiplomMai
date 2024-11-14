package ui.compose.common

import androidx.compose.ui.graphics.Color
import com.jme3.math.ColorRGBA

val BUILDING_COLOR = Color(54, 79, 244)
val KEY_POINT_COLOR = Color(0, 0, 0)
val EDGE_COLOR = Color(87, 87, 87)
val MESH_COLOR = Color.Gray
val BASE_STATION_COLOR = Color(221, 17, 17)
val DESTINATION_COLOR = Color(55, 126, 44)
val SHORTEST_PATH_COLOR = Color(177, 92, 22)
val MOUSE_POINT_COLOR = Color(0, 0, 0)

fun Color.toColorRGBA() = ColorRGBA(red, green, blue, alpha)