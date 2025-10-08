package ui

import com.jme3.app.SimpleApplication
import com.jme3.light.AmbientLight
import com.jme3.light.DirectionalLight
import com.jme3.material.Material
import com.jme3.material.RenderState
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f
import com.jme3.post.FilterPostProcessor
import com.jme3.renderer.RenderManager
import com.jme3.renderer.queue.RenderQueue
import com.jme3.scene.Geometry
import com.jme3.scene.Node
import com.jme3.scene.shape.Box
import com.jme3.scene.shape.Line
import com.jme3.scene.shape.Sphere
import com.jme3.shadow.DirectionalLightShadowFilter
import com.jme3.shadow.DirectionalLightShadowRenderer
import com.jme3.system.AppSettings
import com.jme3.ui.Picture
import kotlinx.coroutines.*
import kotlinx.io.IOException
import model.FlyMap
import model.drone.Drone
import model.landscape.Building
import ui.compose.common.GROUND_COLOR
import java.io.InputStream
import java.net.Socket
import java.nio.ByteBuffer


class City1 : SimpleApplication() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var socket: Socket? = null

    private val buildingGeoms = mutableListOf<Geometry>()
    private val buildingMeshes = mutableListOf<Geometry>()
    private val droneGeoms = mutableListOf<Geometry>()
    private val dronePathNodes = mutableListOf<Node>()

    private val droneIcons = mutableMapOf<Long, Picture>()

    private val iconSize = 32f

    var flyMap = FlyMap.loadFromFile("flyMap1.txt")

    override fun simpleInitApp() {

        initSocket()

        val ground = initEnvironment()

        val buildingsGeometry = mutableListOf<Geometry>()



        flyMap?.buildings?.forEach {
            buildingsGeometry += displayBuilding(it)
        }
        buildingsGeometry += ground

        flyMap?.drones?.forEach { drone ->
            displayDrone(drone)
            displayDronePaths(drone)
        }

        // Настраиваем камеру, чтобы она была сбоку сверху и смотрела на сцену
        cam.location = Vector3f(140f, 100f, 130f)
        cam.lookAt(Vector3f(40f, 0f, 20f), Vector3f.UNIT_Y)
        flyCam.moveSpeed = 60f


    }

    private fun initSocket() {
        fun InputStream.readFully(buffer: ByteArray) {
            var read = 0
            while (read < buffer.size) {
                val r = this.read(buffer, read, buffer.size - read)
                if (r == -1) throw IOException("End of stream")
                read += r
            }
        }
        scope.launch(Dispatchers.IO) {
            while (true) {
                try {
                    println("Пробуем подключиться к серверу...")
                    val socket = Socket("localhost", 12345)
                    println("Подключено!")
                    val inp = socket.getInputStream()
                    while (true) {
                        // 1. Читаем длину
                        val lenBytes = ByteArray(4)
                        inp.readFully(lenBytes)
                        val len = ByteBuffer.wrap(lenBytes).int
                        // 2. Читаем нужное количество байт
                        val bytes = ByteArray(len)
                        inp.readFully(bytes)
                        // 3. Десериализуем объект
                        val flyMap = FlyMap.loadFromBytes(bytes)
                        println("get $lenBytes / $flyMap")
                        if (flyMap != null) {
                            enqueue {
                                updateFlyMap(flyMap)
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("Ошибка соединения: ${e.stackTraceToString()}")
                    // Закрытие сокета если открыт
                } finally {
                    try { socket?.close() } catch (_: Exception) {}
                    delay(2000) // Пауза перед повтором
                }
            }
        }
        scope.launch {

//            while (true) {
//                try {
//                    if (socket != null && socket?.isConnected == true) {
//                        val bytes = ByteArray(5000)
//                        val r = socket?.getInputStream()?.read(bytes)
//                        println(bytes.contentToString())
//                        if (r == -1) {
//                            // Сервер закрыл соединение
//                            socket?.close()
//                            socket = null
//                            delay(1000)
//                            continue
//                        }
//                        val newFlyMap = FlyMap.loadFromBytes(bytes)
//                        if (newFlyMap != null) {
//                            updateFlyMap(newFlyMap)
//                        }
//                    } else {
//                        socket = Socket("localhost", 12345)
//                        delay(1000)
//                    }
//                } catch (e: Exception) {
//                    try {
//                        println(e.stackTraceToString())
//                        socket?.close()
//                        socket = Socket("localhost", 12345)
//                        delay(1000)
//                    } catch (e: Exception) {}
//
//                }
//
//            }

        }
    }

    fun updateFlyMap(newMap: FlyMap) {
        val oldMap = flyMap

        // Сравни списки зданий
        val buildingsChanged = oldMap?.buildings != newMap.buildings
        // Сравни списки дронов
        val dronesChanged = oldMap?.drones != newMap.drones

        // Перерисовывай здания, если список изменился
        if (buildingsChanged) {
            clearBuildings()
            newMap.buildings.forEach { displayBuilding(it) }
        }

        // Перерисовывай дроны, если список изменился
        if (dronesChanged) {
            clearDrones()
            clearDroneIcons()
            clearDronePaths() // удаляем старые маршруты
            newMap.drones.forEach {
                displayDrone(it)
                displayDronePaths(it)
            }
        }

        // Сохраняй новое состояние
        flyMap = newMap
    }


    private fun displayBuilding(b: Building): Geometry {
        val buildingMesh = b.getMesh3D()

        val building = Geometry("Building", buildingMesh)

        // Устанавливаем прозрачный материал
        val material = Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
        material.setBoolean("UseMaterialColors", true)
        material.setColor("Ambient", ColorRGBA.Blue)
        material.setColor("Diffuse", ColorRGBA(0.1f, 0.2f, 0.8f, 1f)) // Глубокий синий
        material.setColor("Specular", ColorRGBA.White)  // блик
        material.setFloat("Shininess", 16f)
        material.additionalRenderState.faceCullMode = RenderState.FaceCullMode.Back  // рисуем только лицевые стороны

        building.material = material

        // Добавляем тени
        building.shadowMode = RenderQueue.ShadowMode.CastAndReceive

        rootNode.attachChild(building)

        val wireframeMaterial = Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
        wireframeMaterial.setColor("Color", ColorRGBA.Black)
        wireframeMaterial.additionalRenderState.isWireframe = true

        val wireframeGeom = Geometry("Building_Wireframe_${b.id}", buildingMesh)
        wireframeGeom.material = wireframeMaterial
        wireframeGeom.shadowMode = RenderQueue.ShadowMode.Off
        rootNode.attachChild(wireframeGeom)

        buildingMeshes += wireframeGeom

        buildingGeoms += building
        return building
    }

    private fun displayDrone(drone: Drone) {
        // Нарисуем дрона как маленькую сферу
        val sphere = Sphere(16, 16, 0.6f)
        val droneGeom = Geometry("Drone_${drone.id}", sphere)
        val mat = Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
        mat.setBoolean("UseMaterialColors", true)
        mat.setColor("Diffuse", ColorRGBA.Yellow)
        mat.setColor("Specular", ColorRGBA.White)
        mat.setFloat("Shininess", 32f)
        droneGeom.material = mat

        droneGeom.localTranslation = drone.currentPosition
        rootNode.attachChild(droneGeom)

        val droneIcon = Picture("DroneIcon")
        droneIcon.setImage(assetManager, "icons/ic_drone_mini_w.png", true)
        droneIcon.setWidth(iconSize)
        droneIcon.setHeight(iconSize)
        val p = cam.getScreenCoordinates(drone.currentPosition)
        println("d ${drone.id} / $p")
        droneIcon.setPosition(p.x, p.y) // В центре, для примера
        guiNode.attachChild(droneIcon)

        droneIcons[drone.id] = droneIcon

        droneGeoms += droneGeom

    }

    private fun displayDronePaths(drone: Drone) {
        // Основной маршрут
        if (drone.currentWayPoint.isNotEmpty()) {
            displayPath(drone.currentWayPoint, ColorRGBA.Yellow)
        }
        // К грузу
        if (drone.roadToCargoStart.isNotEmpty()) {
            displayPath(drone.roadToCargoStart, ColorRGBA.Green)
        }
        // К месту назначения груза
        if (drone.roadToCargoDestination.isNotEmpty()) {
            displayPath(drone.roadToCargoDestination, ColorRGBA.Cyan)
        }
        // К станции зарядки
        if (drone.roadToCargoChargeStation.isNotEmpty()) {
            displayPath(drone.roadToCargoChargeStation, ColorRGBA.Orange)
        }
    }

    private fun displayPath(waypoints: List<Vector3f>, color: ColorRGBA = ColorRGBA.Red): Node? {
        if (waypoints.size < 2) return null
        val pathNode = Node("Path")
        for (i in 0 until waypoints.size - 1) {
            val line = Line(waypoints[i], waypoints[i + 1])
            val lineGeom = Geometry("Line", line)
            val mat = Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
            mat.setColor("Color", color)
            lineGeom.material = mat
            pathNode.attachChild(lineGeom)
        }
        rootNode.attachChild(pathNode)
        dronePathNodes += pathNode
        return pathNode
    }

    private fun clearBuildings() {
        buildingGeoms.forEach { rootNode.detachChild(it) }
        buildingGeoms.clear()
        buildingMeshes.forEach { rootNode.detachChild(it) }
        buildingMeshes.clear()
    }

    private fun clearDrones() {
        droneGeoms.forEach { rootNode.detachChild(it) }
        droneGeoms.clear()
    }

    fun clearDronePaths() {
        dronePathNodes.forEach { rootNode.detachChild(it) }
        dronePathNodes.clear()
    }

    fun clearDroneIcons() {
        droneIcons.values.forEach { guiNode.detachChild(it) }
        droneIcons.clear()
    }

    override fun simpleUpdate(tpf: Float) {
        flyMap?.drones?.forEach { drone ->
            val icon = droneIcons[drone.id] ?: return@forEach
            val screenPos = cam.getScreenCoordinates(drone.currentPosition)
            // jME координаты экрана: (0,0) — внизу слева, для Picture тоже!
            icon.setPosition(screenPos.x - iconSize / 2, screenPos.y - iconSize / 2)
            // Можно скрывать иконку, если дрон за камерой:
//            icon. = screenPos.z > 0
        }
    }

    override fun simpleRender(rm: RenderManager) {
        //add render code here (if any)
    }

    override fun destroy() {
        super.destroy()
        println("destroy")
        scope.cancel()
        try {
            socket?.close()
        } catch (_: Exception) {}

    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val app = City1()
            app.isShowSettings = false //Settings dialog not supported on mac

            val settings = AppSettings(true)
            settings.width = 800
            settings.height = 970
            settings.centerWindow = false
            settings.windowXPosition = 1700
            settings.windowYPosition = 0
            settings.title = "City graph"
            app.setSettings(settings)

            app.start()
        }
    }
}


fun SimpleApplication.initEnvironment(): Geometry {

    // Создаем плоскость (основу) светло-серого цвета
    val groundBox = Box(300f, 0.1f, 300f) // Плоскость размером 100x100
    val ground = Geometry("Ground", groundBox)
    val groundMat = Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
    groundMat.setBoolean("UseMaterialColors", true)
    groundMat.setColor("Diffuse", GROUND_COLOR.mult(0.8f))
    groundMat.setColor("Specular", GROUND_COLOR)
    groundMat.setFloat("Shininess", 5f) // Отражающая способность
    ground.material = groundMat
    ground.setShadowMode(RenderQueue.ShadowMode.Receive)
    ground.setLocalTranslation(90f / 3, -0.1f, 60f / 3) // Чуть ниже оси XZ
    rootNode.attachChild(ground)


    // Добавляем направленный свет для освещения сцены
    val sun = DirectionalLight()
    sun.direction = Vector3f(-1.1f,-.8f,-0.5f).normalizeLocal()
    sun.color = ColorRGBA.White
    rootNode.addLight(sun)

    addShadows(sun)

    // Добавляем фоновое освещение, чтобы не было слишком темно
    val ambient = AmbientLight()
    ambient.color = ColorRGBA.White.mult(0.7f) // Было 0.3, теперь 0.7 или даже 1.0
    rootNode.addLight(ambient)

    // Устанавливаем фон сцены цвета неба
    viewPort.backgroundColor = ColorRGBA(0.5f, 0.7f, 1.0f, 1.0f)

    return ground
}

fun SimpleApplication.addShadows(sun: DirectionalLight) {
    // Создаем ShadowRenderer для динамических теней
    val shadowMapSize = 2048 // Размер карты теней, большее значение — лучшее качество теней
    val dlsr = DirectionalLightShadowRenderer(assetManager, shadowMapSize, 3)
    dlsr.light = sun
    viewPort.addProcessor(dlsr)

    // Также добавим ShadowFilter для улучшения качества теней
    val shadowFilter = DirectionalLightShadowFilter(assetManager, shadowMapSize, 3)
    shadowFilter.light = sun
    shadowFilter.isEnabled = true
    val fpp = FilterPostProcessor(assetManager)
    fpp.addFilter(shadowFilter)
    viewPort.addProcessor(fpp)
}
