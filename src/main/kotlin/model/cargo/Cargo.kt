package model.cargo

import java.io.Serializable

data class Cargo(
    val timeCreation: Long = System.currentTimeMillis(),
    val weight: Double // kg
): Serializable
