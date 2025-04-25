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








//
//        val mesh = Mesh()
//        GeometryBatchFactory.mergeGeometries(buildingsGeometry, mesh)
//
//
//
//        val minBounds = RecastBuilder.calculateMinBounds(mesh)
//        val maxBounds = RecastBuilder.calculateMaxBounds(mesh)
//
//        val config = Config()
//
//        config.maxBounds = maxBounds
//        config.minBounds = minBounds
//        config.cellSize = 0.3f
//        config.cellHeight = 0.2f
//        config.walkableSlopeAngle = 45f
//        config.walkableClimb = 1
//        config.walkableHeight = 2
//        config.walkableRadius = 2
//        config.minRegionArea = 8
//        config.mergeRegionArea = 20
//        config.borderSize = 20
//        config.maxEdgeLength = 12
//        config.setMaxVerticesPerPoly(6)
//        config.detailSampleMaxError = 1f
//        config.detailSampleDistance = 6f
//
//        RecastBuilder.calculateGridWidth(config)
//        RecastBuilder.calculatesGridHeight(config)
//
//
//        // Step 2. Rasterize input polygon soup.
//
//        //context is needed for logging that is not yet fully supported in native library.
//        //It must NOT be null.
//        val context = Context()
//
//
//        // Allocate voxel heightfield where we rasterize our input data to.
//        val heightfield = Heightfield()
//        if (!RecastBuilder.createHeightfield(context, heightfield, config)) {
//            println("Could not create solid heightfield")
//            return
//        }
//
//
//        // Allocate array that can hold triangle area types.
//
//        // In Recast terminology, triangles are what indices in jME is. I left this,
//
//        // Find triangles which are walkable based on their slope and rasterize them.
//        val areas = RecastBuilder.markWalkableTriangles(context, config.getWalkableSlopeAngle(), mesh)
//        RecastBuilder.rasterizeTriangles(context, mesh, areas, heightfield, 20)
//
//
//        // Step 3. Filter walkables surfaces.
//        // Once all geometry is rasterized, we do initial pass of filtering to
//        // remove unwanted overhangs caused by the conservative rasterization
//        // as well as filter spans where the character cannot possibly stand.
//        RecastBuilder.filterLowHangingWalkableObstacles(context, config.walkableClimb, heightfield)
//        RecastBuilder.filterLedgeSpans(context, config, heightfield)
//        RecastBuilder.filterWalkableLowHeightSpans(context, config.walkableHeight, heightfield)
//
//
//        // Step 4. Partition walkable surface to simple regions.
//        // Compact the heightfield so that it is faster to handle from now on.
//        // This will result more cache coherent data as well as the neighbours
//        // between walkable cells will be calculated.
//        val compactHeightfield = CompactHeightfield()
//
//        if (!RecastBuilder.buildCompactHeightfield(context, config, heightfield, compactHeightfield)) {
//            println("Could not build compact data")
//            return
//        }
//
//        if (!RecastBuilder.erodeWalkableArea(context, config.walkableRadius, compactHeightfield)) {
//            println("Could not erode")
//            return
//        }
//
//
//        // Partition the heightfield so that we can use simple algorithm later to triangulate the walkable areas.
//        // There are 3 partitioning methods, each with some pros and cons:
//        // 1) Watershed partitioning
//        //   - the classic Recast partitioning
//        //   - creates the nicest tessellation
//        //   - usually slowest
//        //   - partitions the heightfield into nice regions without holes or overlaps
//        //   - the are some corner cases where this method creates produces holes and overlaps
//        //      - holes may appear when a small obstacles is close to large open area (triangulation can handle this)
//        //      - overlaps may occur if you have narrow spiral corridors (i.e stairs), this make triangulation to fail
//        //   * generally the best choice if you precompute the navmesh, use this if you have large open areas
//        // 2) Monotone partitioning
//        //   - fastest
//        //   - partitions the heightfield into regions without holes and overlaps (guaranteed)
//        //   - creates long thin polygons, which sometimes causes paths with detours
//        //   * use this if you want fast navmesh generation
//        val partitionType = "Sample partition watershed"
//
//        if (partitionType == "Sample partition watershed") {
//            if (!RecastBuilder.buildDistanceField(context, compactHeightfield)) {
//                println("Could not build distance field")
//                return
//            }
//            if (!RecastBuilder.buildRegions(context, compactHeightfield, config)) {
//                println("Could not build watershed regions")
//                return
//            }
//        }
//
//        if (partitionType == "Sample partition monotone") {
//            if (!RecastBuilder.buildRegionsMonotone(context, compactHeightfield, config)) {
//                println("Could not build monotone regions")
//                return
//            }
//        }
//
//
//        // Step 5. Trace and simplify region contours.
//        // Create contours.
//        val contourSet = ContourSet()
//
//        if (!RecastBuilder.buildContours(context, compactHeightfield, 2f, config.maxEdgeLength, contourSet)) {
//            println("Could not create contours")
//            return
//        }
//
//
//        // Step 6. Build polygons mesh from contours.
//        // Build polygon navmesh from the contours.
//        val polyMesh = PolyMesh()
//
//        if (!RecastBuilder.buildPolyMesh(context, contourSet, config.maxVertsPerPoly, polyMesh)) {
//            println("Could not triangulate contours")
//            return
//        }
//
//
//        // Step 7. Create detail mesh which allows to access approximate height on each polygon.
//        val polyMeshDetail = PolyMeshDetail()
//
//        if (!RecastBuilder.buildPolyMeshDetail(context, polyMesh, compactHeightfield, config, polyMeshDetail)) {
//            println("Could not build detail mesh.")
//            return
//        }
//
//
//        // (Optional) Step 8. Create Detour data from Recast poly mesh.
//        // The GUI may allow more max points per polygon than Detour can handle.
//        // Only build the detour navmesh if we do not exceed the limit.
//        if (config.maxVertsPerPoly > DetourBuilder.VERTS_PER_POLYGON()) {
//            return
//        }
//        val createParams = NavMeshCreateParams()
//        createParams.getData(polyMesh)
//        createParams.getData(polyMeshDetail)
//
//        //setting optional off-mesh connections (in my example there are none)
//        createParams.getData(config)
//        createParams.isBuildBvTree = true
//
//        val navData = DetourBuilder.createNavMeshData(createParams)
//
//        if (navData == null) {
//            println("Could not build Detour navmesh.")
//            return
//        }
//
//        val navMesh = NavMesh()
//
//        if (!navMesh.isAllocationSuccessful) {
//            println("Could not create Detour navmesh")
//            return
//        }
//        var status: Status = navMesh.init(navData, TileFlags.DT_TILE_FREE_DATA.value())
//        if (status.isFailed) {
//            println("Could not init Detour navmesh")
//            return
//        }
//
//        val query = NavMeshQuery()
//        status = query.init(navMesh, 2048)
//        if (status.isFailed) {
//            println("Could not init Detour navmesh query")
//            return
//        }
//
//
////        val path = query.findPath(Poly(), Poly(), Vector3f(-10f, 10f, -10f), Vector3f(50f, 30f, 50f), QueryFilter(), 9999999)
//        val path = query.findStraightPath(Vector3f(-10f, 10f, -10f), Vector3f(50f, 30f, 50f), emptyArray(), 9999999)
//
//
//
//        // Отображаем путь
//        displayPath(path.toList())
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
