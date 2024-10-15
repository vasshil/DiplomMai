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
import com.jme3.shadow.DirectionalLightShadowFilter
import com.jme3.shadow.DirectionalLightShadowRenderer


/**
 * This is the Main Class of your Game. It should boot up your game and do initial initialisation
 * Move your Logic into AppStates or Controls or other java classes
 */
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
        val groundBox = Box(50f, 0.1f, 50f) // Плоскость размером 100x100
        val ground = Geometry("Ground", groundBox)
        val groundMat = Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
        groundMat.setBoolean("UseMaterialColors", true)
        groundMat.setColor("Diffuse", ColorRGBA.LightGray)
        groundMat.setColor("Specular", ColorRGBA.White)
        groundMat.setFloat("Shininess", 5f) // Отражающая способность
        ground.material = groundMat
        ground.setShadowMode(RenderQueue.ShadowMode.Receive)
        ground.setLocalTranslation(0f, -0.1f, 0f) // Чуть ниже оси XZ
        rootNode.attachChild(ground)


        // Создаем несколько параллелепипедов (зданий) полупрозрачного голубого цвета
        addBuilding(5, 10, 5, Vector3f(0f, 5f, 0f)) // Здание в центре
        addBuilding(4, 8, 4, Vector3f(7f, 4f, 7f)) // Здание справа
        addBuilding(3, 20, 3, Vector3f(-7f, 3f, -7f)) // Здание слева
        addBuilding(6, 12, 6, Vector3f(-10f, 6f, 10f)) // Здание позади слева
        addBuilding(7, 9, 4, Vector3f(1f, 4.5f, -10f)) // Здание спереди справа


        // Добавляем направленный свет для освещения сцены
        val sun = DirectionalLight()
        sun.direction = Vector3f(-0.5f,-.7f,0.5f).normalizeLocal()
        sun.color = ColorRGBA.White
        rootNode.addLight(sun)


        // Добавляем фоновое освещение, чтобы не было слишком темно
        val ambient = AmbientLight()
        ambient.color = ColorRGBA.Gray
        rootNode.addLight(ambient)


        // Настраиваем камеру, чтобы она была сбоку сверху и смотрела на сцену
        cam.location = Vector3f(30f, 40f, 30f)
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y)

        addShadows(sun)

        // Устанавливаем фон сцены цвета неба
        viewPort.backgroundColor = ColorRGBA(0.5f, 0.7f, 1.0f, 0.5f)
    }

    private fun addBuilding(width: Number, height: Number, depth: Number, position: Vector3f) {
        val box = Box(width.toFloat() / 2, height.toFloat() / 2, depth.toFloat() / 2)
        val building = Geometry("Building", box)
        val mat = Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
        mat.setBoolean("UseMaterialColors", true)
        mat.setColor("Diffuse", ColorRGBA(0.5f, 0.7f, 1.0f, 0.5f)) // Основной цвет с легкой голубизной
        mat.setColor("Specular", ColorRGBA.White) // Белый цвет для отраженных бликов
        mat.setFloat("Shininess", 36f) // Значение блеска поверхности
        building.material = mat
        building.setShadowMode(RenderQueue.ShadowMode.CastAndReceive)
        building.localTranslation = position
//        building.queueBucket = RenderQueue.Bucket.Transparent
        rootNode.attachChild(building)
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
