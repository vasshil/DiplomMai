package model.cargo

data class Cargo(
    val timeCreation: Long = System.currentTimeMillis(),
    val weight: Int // kg
)
