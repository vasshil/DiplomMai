package model.drone

import androidx.compose.ui.graphics.Color
import ui.compose.common.*
import java.io.Serializable

enum class DroneStatus(val color: Color, val localization: String): Serializable {
    WAITING(DRONE_STATUS_WAITING_COLOR, "ожидание"),
    CHARGING(DRONE_STATUS_CHARGING_COLOR, "зарядка"),
    DELIVERING(DRONE_STATUS_DELIVERING_COLOR, "доставка"),
    RETURNING(DRONE_STATUS_RETURNING_COLOR, "возвращение"),
    ERROR(DRONE_STATUS_ERROR_COLOR, "ошибка"),
}