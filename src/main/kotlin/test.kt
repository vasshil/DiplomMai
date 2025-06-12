import java.net.Socket
import kotlin.math.sqrt

data class Point(val x: Double, val y: Double)

// Пример использования
fun main() {
    val socket = Socket("localhost", 12345)
    while (true) {
        val bytes = ByteArray(8000)
        socket.getInputStream().read(bytes)
        println(bytes.contentToString())
    }
}