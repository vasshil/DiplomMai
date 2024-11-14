package ui.compose.common

import com.jme3.math.Vector3f
import org.locationtech.jts.triangulate.polygon.ConstrainedDelaunayTriangulator
import org.locationtech.jts.triangulate.quadedge.QuadEdgeSubdivision

//fun triangulatePolygon(polygon: List<Vector3f>): List<Vector3f> {
//    val polygon = createJTSPolygon(building)
//    val triangulationBuilder = ConstrainedDelaunayTriangulator()
//    triangulationBuilder.setSites(polygon)
//
//    // Получаем триангуляцию в виде под-деления
//    val subdivision = triangulationBuilder.getSubdivision() as QuadEdgeSubdivision
//    val triangles = subdivision.triangles(true)
//
//    val triangleVertices = mutableListOf<Vector3f>()
//    for (i in 0 until triangles.numGeometries) {
//        val triangle = triangles.getGeometryN(i)
//        for (j in 0 until 3) {
//            val coord = triangle.coordinates[j]
//            triangleVertices.add(Vector3f(coord.x.toFloat(), 0f, coord.y.toFloat()))
//        }
//    }
//    return triangleVertices
//}