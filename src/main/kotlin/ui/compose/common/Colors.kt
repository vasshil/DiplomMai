package ui.compose.common

import androidx.compose.ui.graphics.Color
import com.jme3.math.ColorRGBA
import java.awt.Transparency

// scheme colors
val BUILDING_COLOR = Color(111, 130, 237)
val FOCUSED_BUILDING_COLOR = Color(54, 79, 244)
val DELETE_FOCUSED_BUILDING_COLOR = Color(221, 17, 17)
val KEY_POINT_COLOR = Color(0, 0, 0)
val EDGE_COLOR = Color(87, 87, 87)
val SCHEME_BACKGROUND_COLOR = Color(250, 250, 250)
val MESH_COLOR = Color.Gray
val AXIS_COLOR = Color.Black
val BASE_STATION_COLOR = Color(221, 17, 17)
val DESTINATION_COLOR = Color(55, 126, 44)
val SHORTEST_PATH_COLOR = Color(177, 92, 22)
val MOUSE_POINT_COLOR = Color(0, 0, 0)

// ui colors
val TOP_BAR_BG_COLOR = Color(230, 230, 230)
val TEXT_FIELD_COLOR = Color(228, 228, 228)
val BUTTON_COLOR = Color(66, 137, 43)

val MODE_SWITCH_BG_COLOR = Color(186, 186, 186)
val MODE_SWITCH_SELECTED_COLOR = Color(255, 255, 255)
val MODE_SWITCH_UNSELECTED_COLOR = Color(255, 255, 255, 0x00000000)

val DRONE_ICON_BG_COLOR = Color(228, 228, 228)
val DRONE_ICON_COLOR = Color(0, 0, 0)
val DRONE_BATTERY_NORMAL_COLOR = Color(0, 0, 0)
val DRONE_BATTERY_LOW_COLOR = Color(221, 17, 17)


fun Color.toColorRGBA() = ColorRGBA(red, green, blue, alpha)
fun Color.toColorRGBA(transparency: Float) = ColorRGBA(red, green, blue, transparency)