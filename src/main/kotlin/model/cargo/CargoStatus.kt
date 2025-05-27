package model.cargo

import androidx.compose.ui.graphics.Color
import ui.compose.common.*
import java.io.Serializable

enum class CargoStatus(val color: Color, val localization: String): Serializable {
    WAITING(CARGO_STATUS_WAITING_COLOR, "ожидание"),
    ON_ROAD(CARGO_STATUS_ON_ROAD_COLOR, "в пути"),
    DONE(CARGO_STATUS_DONE_COLOR, "доставлен"),
    ERROR(CARGO_STATUS_ERROR_COLOR, "ошибка"),
}