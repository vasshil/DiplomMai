import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")

//    kotlin("plugin.serialization") version "1.4.21"
//    id("application")
//    id("io.github.0ffz.github-packages1.2.1")
}

group = "com.app"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
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
