import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
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

    val jmonkeyengineVersion = "3.6.1-stable"
    implementation("org.jmonkeyengine:jme3-core:$jmonkeyengineVersion")
    implementation("org.jmonkeyengine:jme3-desktop:$jmonkeyengineVersion")
    runtimeOnly("org.jmonkeyengine:jme3-jogg:$jmonkeyengineVersion")
    runtimeOnly("org.jmonkeyengine:jme3-plugins:$jmonkeyengineVersion")

    implementation("org.jmonkeyengine:jme3-jbullet:$jmonkeyengineVersion")
    runtimeOnly("org.jmonkeyengine:jme3-lwjgl3:$jmonkeyengineVersion")

//    implementation("wf.frk:jme-igui:0.1.2")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "DiplomMai"
            packageVersion = "1.0.0"
        }
    }
}
