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
import com.jme3.scene.Mesh
import com.jme3.scene.Node
import com.jme3.scene.shape.Box
import com.jme3.scene.shape.Line
import com.jme3.scene.shape.Sphere
import com.jme3.shadow.DirectionalLightShadowFilter
import com.jme3.shadow.DirectionalLightShadowRenderer
import com.jme3.system.AppSettings
import jme3tools.optimize.GeometryBatchFactory
import model.City
import model.graph.Edge
import model.graph.Graph3D
import model.graph.Vertex
import model.landscape.Building
import org.recast4j.recast.RecastBuilder
import ui.compose.common.FOCUSED_BUILDING_COLOR
import ui.compose.common.toColorRGBA
import java.util.*


class City1 : SimpleApplication() {

    override fun simpleInitApp() {

        val ground = initEnvironment()

        val city = City.loadFromFile("city1234.txt") ?: return

        val buildingsGeometry = mutableListOf<Geometry>()

        city.buildings.forEach {
            buildingsGeometry += displayBuilding(it)
        }
        buildingsGeometry += ground

//        displayGraph(city.graph)


        // Настраиваем камеру, чтобы она была сбоку сверху и смотрела на сцену
//        cam.location = Vector3f(0f, 200f, 0f)
        cam.location = Vector3f(140f, 100f, 130f)
//        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y)
        cam.lookAt(Vector3f(40f, 0f, 20f), Vector3f.UNIT_Y)
        flyCam.moveSpeed = 60f


    }

    private fun displayPath(waypoints: List<Vector3f>) {
        if (waypoints.size < 2) {
            println("Недостаточно точек для отображения пути.")
            return
        }

        val pathNode: Node = Node("Path")

        // Создаем линии между точками пути
        for (i in 0 until waypoints.size - 1) {
            val line = Line(waypoints[i], waypoints[i + 1])
            line.lineWidth = 2f // Толщина линии

            // Создаем геометрию линии
            val lineGeom = Geometry("Line", line)
            val mat = Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
            mat.setColor("Color", ColorRGBA.Red) // Красный цвет
            lineGeom.material = mat

            pathNode.attachChild(lineGeom)
        }

        // Добавляем путь к rootNode для отображения
        rootNode.attachChild(pathNode)
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




    private fun displayGraph(graph: Graph3D) {
        // Добавляем вершины (в виде сфер) и ребра (в виде линий) в сцену
        graph.vertices.forEachIndexed { i, it ->
            displayVertex(it, i)
        }

        graph.edges.forEach {
            displayEdge(it)
        }
    }

    private fun displayVertex(vertex: Vertex, index: Int) {
        val sphere = Sphere(16, 16, 0.3f) // Радиус сферы 0.3
        val vertexGeom = Geometry("Vertex_$index", sphere)
        val mat = Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
        mat.setColor("Diffuse", ColorRGBA.Cyan)
        mat.setColor("Specular", ColorRGBA.White)
        mat.setFloat("Shininess", 16f)
        vertexGeom.material = mat
        vertexGeom.localTranslation = vertex.position
        rootNode.attachChild(vertexGeom)
    }

    private fun displayEdge(edge: Edge) {
        val line = Line(edge.vertex1.position, edge.vertex2.position)
        val edgeGeom = Geometry("Edge", line)
        val mat = Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
        mat.setColor("Color", ColorRGBA.Blue)
        edgeGeom.material = mat
        rootNode.attachChild(edgeGeom)
    }



    override fun simpleUpdate(tpf: Float) {
        //this method will be called every game tick and can be used to make updates
    }

    override fun simpleRender(rm: RenderManager) {
        //add render code here (if any)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val app = City1()
            app.isShowSettings = false //Settings dialog not supported on mac

            val settings = AppSettings(true)
            settings.width = 1280
            settings.height = 720
            settings.title = "City graph"
            app.setSettings(settings)

            app.start()
        }
    }
}


fun SimpleApplication.initEnvironment(): Geometry {

    // Создаем плоскость (основу) светло-серого цвета
    val groundBox = Box(100f, 0.1f, 100f) // Плоскость размером 100x100
    val ground = Geometry("Ground", groundBox)
    val groundMat = Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
    groundMat.setBoolean("UseMaterialColors", true)
    groundMat.setColor("Diffuse", ColorRGBA.LightGray)
    groundMat.setColor("Specular", ColorRGBA.White)
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
    ambient.color = ColorRGBA.White
    rootNode.addLight(ambient)

    // Устанавливаем фон сцены цвета неба
    viewPort.backgroundColor = ColorRGBA(0.5f, 0.7f, 1.0f, 0.5f)

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
