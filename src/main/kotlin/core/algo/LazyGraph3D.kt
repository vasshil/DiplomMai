package core.algo

import model.City
import model.landscape.Building
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.index.strtree.STRtree
import kotlin.math.abs
import kotlin.math.hypot


/**
 * «Ленивый» граф: узлы и рёбра создаются только по мере запроса алгоритмом.
 * @param levels    дискретные высоты (шаг 1 м: 0,1,2,…,maxH)
 */
class LazyGraph3D(
    private val city: City,
    private val levels: List<Float>,
    private val climbRate: Float = 3f,      // м/с
    private val horizSpeed: Float = 10f     // м/с
) {

    /* --- пространственный индекс JTS для фильтра подозрений --- */
    private val index = STRtree().apply {
        city.buildings.forEach { insert(it.toJTSPolygon().envelopeInternal, it) }
    }

    /* --- кеши: контуры зданий и вершины на уровнях ------------ */
    private val ringCache = mutableMapOf<Long, List<Pair<Float, Float>>>()
    private val keyCache  = mutableMapOf<Pair<Long, Int>, List<Vertex3D>>()  // (id,level) -> узлы
    private val visible   = mutableSetOf<Pair<Vertex3D, Vertex3D>>()
    private val blocked   = mutableSetOf<Pair<Vertex3D, Vertex3D>>()

    private fun ringXY(b: Building): List<Pair<Float, Float>> =
        ringCache.getOrPut(b.id) {
            b.getKeyNodes(0f).map { it.position.x to it.position.z }
        }

    private fun keyNodes(b: Building, level: Int): List<Vertex3D> =
        keyCache.getOrPut(b.id to level) {
            if (level > b.height + Building.safeDistance) emptyList()
            else ringXY(b).map { (x, z) -> Vertex3D(b.id, x, level.toFloat(), z) }
        }

    private fun canSee(a: Vertex3D, b: Vertex3D): Boolean {
        val p = if (a.hashCode() < b.hashCode()) a to b else b to a
        if (p in visible) return true
        if (p in blocked) return false

        val seg = GeometryFactory().createLineString(
            arrayOf(a.toJTSCoordinate(), b.toJTSCoordinate())
        )

        @Suppress("UNCHECKED_CAST")
        val hit = (index.query(seg.envelopeInternal) as List<Building>)
            .any { it.toJTSPolygon().intersection(seg).coordinates.size > 1 }

        (if (hit) blocked else visible).add(p)
        return !hit
    }

    /** Отдаёт соседние рёбра для вершины v */
    fun neighbours(v: Vertex3D): Sequence<Edge3D> = sequence {
        val levelIdx = v.y.toInt()
        val building = city.buildings.first { it.id == v.buildingId }

        /* 1) ринг того же здания */
        val ring = keyNodes(building, levelIdx)
        val i    = ring.indexOf(v)
        if (i >= 0) {
            ring.getOrNull(i - 1)?.let { yield(edge(v, it, true)) }
            ring.getOrNull(i + 1)?.let { yield(edge(v, it, true)) }
        }

        /* 2) видимые узлы других зданий */
        for (b in city.buildings) if (b.id != building.id)
            for (w in keyNodes(b, levelIdx))
                if (canSee(v, w)) yield(edge(v, w, false))

        /* 3) вертикальные «лифты» в слой ±1 м */
        for (dy in listOf(-1, +1)) {
            val newY = levelIdx + dy
            if (newY in levels.indices &&
                newY <= building.height + Building.safeDistance
            ) yield(edge(v, v.copy(y = newY.toFloat()), false))
        }
    }

    private fun edge(a: Vertex3D, b: Vertex3D, alongWall: Boolean): Edge3D {
        val dxz  = hypot(a.x - b.x, a.z - b.z)
        val dy   = abs(a.y - b.y)
        val time = dxz / horizSpeed + dy / climbRate
        return Edge3D(a, b, alongWall, time)
    }
}