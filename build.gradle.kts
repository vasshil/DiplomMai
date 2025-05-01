import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")

//    kotlin("plugin.serialization") version "1.4.21"
//    id("application")
}


group = "com.app"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()

//    flatDir {
//        dirs("libs")
//    }

}

//tasks {
//    applicationDefaultJvmArgs = listOf("-Xmx2048m")
//}
//
//application {
//    applicationDefaultJvmArgs
//}


dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation(compose.materialIconsExtended)


    // 3d graphics
    val jmonkeyengineVersion = "3.6.1-stable"
    implementation("org.jmonkeyengine:jme3-core:$jmonkeyengineVersion")
    implementation("org.jmonkeyengine:jme3-desktop:$jmonkeyengineVersion")
    runtimeOnly("org.jmonkeyengine:jme3-jogg:$jmonkeyengineVersion")
    runtimeOnly("org.jmonkeyengine:jme3-plugins:$jmonkeyengineVersion")

    implementation("org.jmonkeyengine:jme3-jbullet:$jmonkeyengineVersion")
    runtimeOnly("org.jmonkeyengine:jme3-lwjgl3:$jmonkeyengineVersion")

//    implementation(fileTree("libs" to "*.jar"))
//    implementation(kotlin("jme3-ai"))
//    implementation(fileTree(mapOf("include" to listOf(".jar"), "dir" to "libs")))
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))




    // recast4j
    val recast4jVersion = "1.2.8"
    implementation ("org.recast4j:parent:$recast4jVersion")
    implementation ("org.recast4j:detour-tile-cache:$recast4jVersion")
    implementation ("org.recast4j:detour-crowd:$recast4jVersion")
    implementation ("org.recast4j:detour-extras:$recast4jVersion")
    implementation ("org.recast4j:recast:$recast4jVersion")
    implementation ("org.recast4j:detour:$recast4jVersion")



    // geometry
    implementation("org.locationtech.jts:jts-core:1.20.0")

//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
}

compose.desktop {
    application {
        mainClass = "ui/CityCreator.kt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "DiplomMai"
            packageVersion = "1.0.0"

        }
    }
}
