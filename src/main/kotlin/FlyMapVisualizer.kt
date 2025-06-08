import com.jme3.app.SimpleApplication
import com.jme3.input.KeyInput
import com.jme3.input.controls.ActionListener
import com.jme3.input.controls.KeyTrigger
import com.jme3.light.AmbientLight
import com.jme3.light.DirectionalLight
import com.jme3.material.Material
import com.jme3.material.RenderState
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector2f
import com.jme3.math.Vector3f
import com.jme3.post.FilterPostProcessor
import com.jme3.renderer.queue.RenderQueue
import com.jme3.scene.Geometry
import com.jme3.scene.Mesh
import com.jme3.scene.Node
import com.jme3.scene.shape.Box
import com.jme3.scene.shape.Quad
import com.jme3.scene.shape.Sphere
import com.jme3.shadow.DirectionalLightShadowRenderer
import model.*
import model.drone.Drone
import model.landscape.Building
import org.locationtech.jts.geom.GeometryFactory
import ui.compose.common.FOCUSED_BUILDING_COLOR
import ui.compose.common.toColorRGBA

class FlyMapVisualizer : SimpleApplication(), ActionListener {

    private var flyMap: FlyMap? = FlyMap.loadFromFile("flyMap1.txt")

    override fun simpleInitApp() {
        println(flyMap)
        // Камера
        flyCam.moveSpeed = 50f
        cam.location = Vector3f(0f, 100f, 100f)
        cam.lookAt(Vector3f(0f, 0f, 0f), Vector3f.UNIT_Y)

        // Свет
        val ambient = AmbientLight().apply { color = ColorRGBA.White.mult(0.5f) }
        val directional = DirectionalLight().apply {
            direction = Vector3f(-1f, -2f, -3f).normalizeLocal()
            color = ColorRGBA.White
        }
        rootNode.addLight(ambient)
        rootNode.addLight(directional)

        // Белая плоская поверхность (земля)
        val floor = Geometry("Floor", Quad(500f, 500f))
        val matFloor = Material(assetManager, "Common/MatDefs/Light/Lighting.j3md").apply {
            setBoolean("UseMaterialColors", true)
            setColor("Diffuse", ColorRGBA.White)
            setColor("Specular", ColorRGBA.White)
            setFloat("Shininess", 1f)
        }
        floor.material = matFloor
        floor.rotate(-Math.PI.toFloat() / 2f, 0f, 0f)
        floor.localTranslation = Vector3f(-250f, 0f, -250f)
        rootNode.attachChild(floor)

        // Настройка клавиш
        inputManager.addMapping("LoadMap", KeyTrigger(KeyInput.KEY_L))
        inputManager.addListener(this, "LoadMap")
    }

    override fun onAction(name: String?, isPressed: Boolean, tpf: Float) {
        if (name == "LoadMap" && isPressed) {
            // Тут ты можешь читать FlyMap из сокета
            loadFlyMap(mockFlyMap())
            visualizeFlyMap()
        }
    }

    private fun loadFlyMap(newMap: FlyMap) {
        flyMap = newMap
    }

    private fun visualizeFlyMap() {
        rootNode.detachAllChildren() // очищаем сцену
        createFloor()

        flyMap?.buildings?.forEach { building ->
            val geom = displayBuilding(building)
            rootNode.attachChild(geom)
        }

        flyMap?.drones?.forEach { drone ->
            val geom = createDroneGeometry(drone)
            rootNode.attachChild(geom)
        }
    }

    private fun createFloor() {
        val floor = Geometry("Floor", Quad(500f, 500f))
        val matFloor = Material(assetManager, "Common/MatDefs/Light/Lighting.j3md").apply {
            setBoolean("UseMaterialColors", true)
            setColor("Diffuse", ColorRGBA.White)
            setColor("Specular", ColorRGBA.White)
            setFloat("Shininess", 1f)
        }
        floor.material = matFloor
        floor.rotate(-Math.PI.toFloat() / 2f, 0f, 0f)
        floor.localTranslation = Vector3f(-250f, 0f, -250f)
        rootNode.attachChild(floor)
    }

    private fun createBuildingGeometry(building: Building): Geometry {
        val geometryFactory = GeometryFactory()

        // Базовые и верхние точки здания
        val baseVertices = building.groundCoords.map { Vector3f(it.x, 0f, it.z) }
        val topVertices = baseVertices.map { Vector3f(it.x, building.height, it.z) }

        // Контейнеры для позиций, нормалей и индексов
        val positions = mutableListOf<Float>()
        val normals = mutableListOf<Float>()
        val indices = mutableListOf<Int>()

        // Добавляем боковые стены
        for (i in baseVertices.indices) {
            val next = (i + 1) % baseVertices.size
            val p1 = baseVertices[i]
            val p2 = baseVertices[next]
            val p3 = topVertices[next]
            val p4 = topVertices[i]

            // Добавляем вершины стены (каждой стены 4 вершины)
            positions.addAll(listOf(
                p1.x, p1.y, p1.z,
                p2.x, p2.y, p2.z,
                p3.x, p3.y, p3.z,
                p4.x, p4.y, p4.z
            ))

            // Вычисляем нормаль стены
            val edge1 = p2.subtract(p1)
            val edge2 = p4.subtract(p1)
            val normal = edge1.cross(edge2).normalizeLocal()

            repeat(4) {
                normals.addAll(listOf(normal.x, normal.y, normal.z))
            }

            val offset = i * 4
            indices.addAll(
                listOf(
                    offset, offset + 1, offset + 2,
                    offset, offset + 2, offset + 3
                )
            )
        }

        // Добавляем верхнюю крышу (треугольный фан)
        val roofStartIndex = positions.size / 3
        val roofCenter = topVertices.reduce { acc, v -> acc.add(v) }.mult(1f / topVertices.size)
        positions.addAll(listOf(roofCenter.x, roofCenter.y, roofCenter.z))
        normals.addAll(listOf(0f, 1f, 0f))
        topVertices.forEach { vertex ->
            positions.addAll(listOf(vertex.x, vertex.y, vertex.z))
            normals.addAll(listOf(0f, 1f, 0f))
        }
        for (i in topVertices.indices) {
            val v1 = roofStartIndex
            val v2 = roofStartIndex + 1 + i
            val v3 = roofStartIndex + 1 + (i + 1) % topVertices.size
            indices.addAll(listOf(v1, v2, v3))
        }

        // Создаем меш
        val mesh = Mesh()
        mesh.setBuffer(com.jme3.scene.VertexBuffer.Type.Position, 3, positions.toFloatArray())
        mesh.setBuffer(com.jme3.scene.VertexBuffer.Type.Normal, 3, normals.toFloatArray())
        mesh.setBuffer(com.jme3.scene.VertexBuffer.Type.Index, 3, indices.toIntArray())
        mesh.updateBound()
        mesh.updateCounts()

        val geom = Geometry("Building_${building.id}", mesh)

        // Материал
        val mat = Material(assetManager, "Common/MatDefs/Light/Lighting.j3md").apply {
            setBoolean("UseMaterialColors", true)
            setColor("Diffuse", ColorRGBA.Blue.mult(1f))
            setColor("Specular", ColorRGBA.White)
            setFloat("Shininess", 4f)
//            additionalRenderState.blendMode = RenderState.BlendMode.Alpha
        }
        geom.material = mat
//        geom.setQueueBucket(com.jme3.renderer.queue.RenderQueue.Bucket.Transparent)

        return geom
    }


    private fun displayBuilding(b: Building): Geometry {
        val baseSize = 1f//m
        val buildingMesh = b.getMesh3D()
//        println(buildingMesh.)

        val building = Geometry("Building", buildingMesh)

        // Устанавливаем прозрачный материал
        val material = Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
//        material.setBoolean("UseMaterialColors",true)
//        material.setColor("Ambient", ColorRGBA.Blue)
//        material.setColor("Diffuse", ColorRGBA.Blue)
        material.setColor("Color", FOCUSED_BUILDING_COLOR.toColorRGBA(0.9f))
//        material.setColor("Color", ColorRGBA.Blue.mult(ColorRGBA(1f, 1f, 1f, 0.5f))) // Полупрозрачный синий
        material.isTransparent = true
        material.additionalRenderState.blendMode = RenderState.BlendMode.Alpha;
        material.additionalRenderState.faceCullMode = RenderState.FaceCullMode.Off
//        material.additionalRenderState.isWireframe = true
        building.material = material
        building.queueBucket = RenderQueue.Bucket.Transparent

        // Добавляем тени
        building.shadowMode = RenderQueue.ShadowMode.CastAndReceive

        // Добавляем свет и тени
        val light = DirectionalLight()
        light.direction = Vector3f(-0.5f, -0.5f, -0.5f).normalizeLocal()
        rootNode.addLight(light)

        val shadowRenderer = DirectionalLightShadowRenderer(assetManager, 1024, 3)
        shadowRenderer.light = light
        viewPort.addProcessor(shadowRenderer)

        // Добавляем фильтр для корректной работы прозрачности
        val fpp = FilterPostProcessor(assetManager)
        viewPort.addProcessor(fpp)

        rootNode.attachChild(building)

        return building
    }


    private fun createDroneGeometry(drone: Drone): Geometry {
        val sphere = Sphere(8, 8, 1f)
        val geom = Geometry("Drone_${drone.id}", sphere)
        geom.localTranslation = Vector3f(drone.currentPosition.x, drone.currentPosition.y, drone.currentPosition.z)
        val mat = Material(assetManager, "Common/MatDefs/Light/Lighting.j3md").apply {
            setBoolean("UseMaterialColors", true)
            setColor("Diffuse", ColorRGBA.Yellow)
            setColor("Specular", ColorRGBA.White)
            setFloat("Shininess", 10f)
        }
        geom.material = mat
        return geom
    }

    private fun mockFlyMap(): FlyMap {
        return FlyMap(
            buildings = listOf(
                Building(
                    id = 1,
                    groundCoords = listOf(
                        Vector3f(-10f, 0f, -10f),
                        Vector3f(10f, 0f, -10f),
                        Vector3f(10f, 0f, 10f),
                        Vector3f(-10f, 0f, 10f)
                    ),
                    height = 20f
                )
            ),
            drones = listOf(
                Drone(
                    id = 1,
                    currentPosition = Vector3f(0f, 10f, 0f),
                    maxCargoCapacityMass = 5.0
                )
            )
        )
    }
}

fun main() {
    FlyMapVisualizer().start()
}