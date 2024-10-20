package model

import model.graph.Graph3D
import model.landscape.Building

data class City(
    val buildings: MutableList<Building> = mutableListOf(),
    val graph: Graph3D = Graph3D(),
)
