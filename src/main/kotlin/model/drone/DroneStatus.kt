package model.drone

import java.io.Serializable

enum class DroneStatus: Serializable {
    WAITING,
    CHARGING,
    DELIVERING,
    RETURNING,
}