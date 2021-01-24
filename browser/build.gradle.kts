plugins {
    kotlin("js") version "1.4.21"
}

group = "com.jaqxues"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
}

dependencies {
    implementation(project(":core"))
    testImplementation(kotlin("test-js"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.4.2")
}

kotlin {
    js(IR) {
        browser {
            binaries.executable()
            webpackTask {
                cssSupport.enabled = true
            }
            runTask {
                cssSupport.enabled = true
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                    webpackConfig.cssSupport.enabled = true
                }
            }
        }
    }
}