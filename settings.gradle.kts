
rootProject.name = "ProLogicis"


include(":core")
include(":browser")
include(":jvm")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
include("desktop")
