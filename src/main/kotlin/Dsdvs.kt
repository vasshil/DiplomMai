import com.jme3.app.SimpleApplication
import com.jme3.light.AmbientLight
import com.jme3.light.DirectionalLight
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f
import com.jme3.post.FilterPostProcessor
import com.jme3.renderer.RenderManager
import com.jme3.renderer.queue.RenderQueue
import com.jme3.scene.Geometry
import com.jme3.scene.shape.Box
import com.jme3.scene.shape.Line
import com.jme3.scene.shape.Sphere
import com.jme3.shadow.DirectionalLightShadowFilter
import com.jme3.shadow.DirectionalLightShadowRenderer
import model.graph.Edge
import model.graph.Graph3D
import model.graph.Vertex
import model.landscape.Building


class City1 : SimpleApplication() {

    override fun simpleInitApp() {
//        val b = Box(1f, 1f, 1f)
//        val geom = Geometry("Box", b)
//
//        val mat = Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
//        mat.setColor("Color", ColorRGBA.Blue)
//        geom.material = mat
//
//        rootNode.attachChild(geom)

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


        // Создаем несколько параллелепипедов (зданий) полупрозрачного голубого цвета
        val buildings = createBuildings()
        buildings.forEach {
            displayBuilding(it)
        }

        val graph = createGraph()
        displayGraph(graph)


        // Добавляем направленный свет для освещения сцены
        val sun = DirectionalLight()
        sun.direction = Vector3f(-1.1f,-.8f,-0.5f).normalizeLocal()
        sun.color = ColorRGBA.White
        rootNode.addLight(sun)


        // Добавляем фоновое освещение, чтобы не было слишком темно
        val ambient = AmbientLight()
        ambient.color = ColorRGBA.White
        rootNode.addLight(ambient)


        // Настраиваем камеру, чтобы она была сбоку сверху и смотрела на сцену
//        cam.location = Vector3f(0f, 200f, 0f)
        cam.location = Vector3f(140f, 100f, 130f)
//        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y)
        cam.lookAt(Vector3f(40f, 0f, 20f), Vector3f.UNIT_Y)
        flyCam.moveSpeed = 25f

        addShadows(sun)

        // Устанавливаем фон сцены цвета неба
        viewPort.backgroundColor = ColorRGBA(0.5f, 0.7f, 1.0f, 0.5f)
    }

    private fun createBuildings(): List<Building> {
        val buildings = mutableListOf<Building>()
        buildings.add(Building(Vector3f(0f, 0f, 0f), Vector3f(2f, 5f, 2f)))
        buildings.add(Building(Vector3f(4f, 0f, 0f), Vector3f(3f, 5f, 1f)))
        buildings.add(Building(Vector3f(8f, 0f, 0f), Vector3f(1f, 4f, 3f)))
        buildings.add(Building(Vector3f(0f, 0f, 3f), Vector3f(3f, 3f, 1f)))
        buildings.add(Building(Vector3f(5f, 0f, 2f), Vector3f(2f, 3f, 3f)))
        buildings.add(Building(Vector3f(8f, 0f, 4f), Vector3f(1f, 2f, 1f)))
        buildings.add(Building(Vector3f(0f, 0f, 5f), Vector3f(4f, 1f, 1f)))

        return buildings
    }

    private fun createGraph(): Graph3D {
        val SD = 1f
        val HGT = 10f
        val graph = Graph3D()
        graph.add(Vector3f(-SD, HGT, -SD))
        graph.add(Vector3f(20 + SD, HGT, -SD))
        graph.add(Vector3f(40 - SD, HGT, -SD))
        graph.add(Vector3f(70 + SD, HGT, -SD))
        graph.add(Vector3f(80 - SD, HGT, -SD))
        graph.add(Vector3f(90 + SD, HGT, -SD))
        graph.add(Vector3f(90 + SD, HGT, 30 + SD))
        graph.add(Vector3f(90 + SD, HGT, 40 - SD))
        graph.add(Vector3f(90 + SD, HGT, 50 + SD))
        graph.add(Vector3f(80 - SD, HGT, 50 + SD))
        graph.add(Vector3f(70 + SD, HGT, 50 + SD))
        graph.add(Vector3f(50 - SD, HGT, 50 + SD))
        graph.add(Vector3f(40 + SD, HGT, 50 + SD))
        graph.add(Vector3f(40 + SD, HGT, 60 + SD))
        graph.add(Vector3f(-SD, HGT, 60 + SD))
        graph.add(Vector3f(-SD, HGT, 50 - SD))
        graph.add(Vector3f(-SD, HGT, 40 + SD))
        graph.add(Vector3f(-SD, HGT, 30 - SD))
        graph.add(Vector3f(-SD, HGT, 20 + SD))
//        graph.add(Vector3f(f, HGT, f))

        for (i in 0 until graph.vertices.size - 1) {
            graph.add(Edge(graph.vertices[i], graph.vertices[i + 1]))
        }
        graph.add(Edge(graph.vertices.last(), graph.vertices.first()))


        graph.add(Vector3f(20 + SD, HGT, 20 + SD)) // 19
        graph.add(Vector3f(40 - SD, HGT, 10 + SD)) // 20
        graph.add(Vector3f(70 + SD, HGT, 10 + SD)) // 21
        graph.add(Vector3f(70 + SD, HGT, 20 - SD)) // 22
        graph.add(Vector3f(80 - SD, HGT, 30 + SD)) // 23
        graph.add(Vector3f(80 - SD, HGT, 40 - SD)) // 24
        graph.add(Vector3f(50 - SD, HGT, 20 - SD)) // 25
        graph.add(Vector3f(30 + SD, HGT, 30 - SD)) // 26
        graph.add(Vector3f(30 + SD, HGT, 40 + SD)) // 27
        graph.add(Vector3f(40 + SD, HGT, 50 - SD)) // 28
        graph.add(Vector3f(40 - SD, HGT, 50 - SD)) // 29
//        graph.add(Vector3f(, HGT, )) // 3

        graph.add(Edge(graph.vertices[1], graph.vertices[19]))
        graph.add(Edge(graph.vertices[1], graph.vertices[20]))
        graph.add(Edge(graph.vertices[2], graph.vertices[20]))
        graph.add(Edge(graph.vertices[3], graph.vertices[21]))
        graph.add(Edge(graph.vertices[3], graph.vertices[23]))
        graph.add(Edge(graph.vertices[6], graph.vertices[23]))
        graph.add(Edge(graph.vertices[6], graph.vertices[24]))
        graph.add(Edge(graph.vertices[7], graph.vertices[24]))
        graph.add(Edge(graph.vertices[7], graph.vertices[23]))
        graph.add(Edge(graph.vertices[9], graph.vertices[24]))
        graph.add(Edge(graph.vertices[10], graph.vertices[22]))
        graph.add(Edge(graph.vertices[11], graph.vertices[25]))
        graph.add(Edge(graph.vertices[12], graph.vertices[28]))
        graph.add(Edge(graph.vertices[15], graph.vertices[29]))
        graph.add(Edge(graph.vertices[16], graph.vertices[27]))
        graph.add(Edge(graph.vertices[17], graph.vertices[26]))
        graph.add(Edge(graph.vertices[18], graph.vertices[19]))
        graph.add(Edge(graph.vertices[19], graph.vertices[25]))
        graph.add(Edge(graph.vertices[19], graph.vertices[26]))
        graph.add(Edge(graph.vertices[20], graph.vertices[19]))
        graph.add(Edge(graph.vertices[20], graph.vertices[25]))
        graph.add(Edge(graph.vertices[20], graph.vertices[26]))
        graph.add(Edge(graph.vertices[20], graph.vertices[21]))
        graph.add(Edge(graph.vertices[20], graph.vertices[29]))
        graph.add(Edge(graph.vertices[21], graph.vertices[22]))
        graph.add(Edge(graph.vertices[22], graph.vertices[25]))
        graph.add(Edge(graph.vertices[23], graph.vertices[24]))
        graph.add(Edge(graph.vertices[25], graph.vertices[26]))
        graph.add(Edge(graph.vertices[25], graph.vertices[28]))
        graph.add(Edge(graph.vertices[26], graph.vertices[27]))
        graph.add(Edge(graph.vertices[27], graph.vertices[29]))
        graph.add(Edge(graph.vertices[28], graph.vertices[29]))
//        graph.add(Edge(graph.vertices[], graph.vertices[]))


        return graph
    }


    private fun displayBuilding(b: Building) {
        val baseSize = 10f//m
        val box = Box(
            b.size.x / 2 * baseSize,
            b.size.y / 2 * baseSize,
            b.size.z / 2 * baseSize
        )
        val building = Geometry("Building", box)
        val mat = Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
        mat.setBoolean("UseMaterialColors", true)
        mat.setColor("Diffuse", ColorRGBA(0.5f, 0.7f, 1.0f, 0.1f)) // Основной цвет с легкой
        mat.setColor("Specular", ColorRGBA(0.5f, 0.7f, 1.0f, 0.1f)) // Белый цвет для отраженных бликов
        mat.setFloat("Shininess", 10f) // Значение блеска поверхности
        building.material = mat
        building.shadowMode = RenderQueue.ShadowMode.CastAndReceive
        building.localTranslation = b.position
            .multLocal(baseSize)
            .addLocal(Vector3f(b.size.x / 2 * baseSize, b.size.y / 2 * baseSize, b.size.z / 2 * baseSize))
        building.queueBucket = RenderQueue.Bucket.Transparent
        rootNode.attachChild(building)
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

    private fun addShadows(sun: DirectionalLight) {
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
            app.start()
        }
    }
}
