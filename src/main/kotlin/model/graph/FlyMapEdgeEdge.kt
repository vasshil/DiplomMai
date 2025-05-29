package model.graph

import java.io.Serializable

data class FlyMapEdgeEdge(
    val vertex1: FlyMapVertex,
    val vertex2: FlyMapVertex,
): Serializable
