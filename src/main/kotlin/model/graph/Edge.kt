package model.graph

import java.io.Serializable

data class Edge(val vertex1: Vertex, val vertex2: Vertex, val isBase: Boolean): Serializable
