package model

import model.graph.Graph3D
import model.landscape.Building

data class City(
    val buildings: List<Building>,
    val graph: Graph3D
)
