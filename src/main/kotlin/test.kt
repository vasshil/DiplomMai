import kotlin.math.sqrt

data class Point(val x: Double, val y: Double)

// Функция для смещения точки на заданное расстояние вдоль нормали
fun offsetPoint(p1: Point, p2: Point, distance: Double): Point {
    val dx = p2.x - p1.x
    val dy = p2.y - p1.y
    val length = sqrt(dx * dx + dy * dy)
    // Нормализуем вектор
    val nx = dy / length
    val ny = -dx / length
    // Смещаем точку p1 на нормаль
    return Point(p1.x + nx * distance, p1.y + ny * distance)
}

// Функция для нахождения пересечения двух линий
fun lineIntersection(p1: Point, p2: Point, p3: Point, p4: Point): Point? {
    val a1 = p2.y - p1.y
    val b1 = p1.x - p2.x
    val c1 = a1 * p1.x + b1 * p1.y

    val a2 = p4.y - p3.y
    val b2 = p3.x - p4.x
    val c2 = a2 * p3.x + b2 * p3.y

    val determinant = a1 * b2 - a2 * b1

    if (determinant == 0.0) {
        // Линии параллельны, пересечения нет
        return null
    }

    val x = (b2 * c1 - b1 * c2) / determinant
    val y = (a1 * c2 - a2 * c1) / determinant

    return Point(x, y)
}

// Функция для расширения многоугольника
fun expandPolygon(polygon: List<Point>, distance: Double): List<Point> {
    val expandedPoints = mutableListOf<Point>()
    val n = polygon.size

    // Смещаем каждую сторону многоугольника
    val offsetLines = mutableListOf<Pair<Point, Point>>()
    for (i in 0 until n) {
        val p1 = polygon[i]
        val p2 = polygon[(i + 1) % n]
        val offsetP1 = offsetPoint(p1, p2, distance)
        val offsetP2 = offsetPoint(p2, p1, distance)
        offsetLines.add(offsetP1 to offsetP2)
    }

    // Вычисляем пересечения смещённых линий
    for (i in offsetLines.indices) {
        val (p1, p2) = offsetLines[i]
        val (p3, p4) = offsetLines[(i + 1) % n]
        val intersection = lineIntersection(p1, p2, p3, p4)
        if (intersection != null) {
            expandedPoints.add(intersection)
        }
    }

    return expandedPoints
}

// Пример использования
fun main() {
    val polygon = listOf(
        Point(0.0, 0.0),
        Point(4.0, 0.0),
        Point(4.0, 3.0),
        Point(0.0, 3.0)
    )
    val distance = 1.0
    val expandedPolygon = expandPolygon(polygon, distance)
    println("Расширенный многоугольник:")
    expandedPolygon.forEach { println("(${it.x}, ${it.y})") }
}