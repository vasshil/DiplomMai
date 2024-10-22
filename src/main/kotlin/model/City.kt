package model

import model.graph.Graph3D
import model.landscape.Building

data class City(
    val buildings: MutableList<Building> = mutableListOf(),
    val graph: Graph3D = Graph3D(),
) {

    fun newBuilding(): Building {
        val newId = if (buildings.isEmpty()) 0 else buildings.last().id + 1
        val newBuilding = Building(newId)
        buildings.add(newBuilding)
        return buildings.last()
    }

}
