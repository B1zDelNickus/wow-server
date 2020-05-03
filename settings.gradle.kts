pluginManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        jcenter()
        gradlePluginPortal()
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
        maven { url = uri("https://kotlin.bintray.com/ktor") }
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://dl.bintray.com/spekframework/spek-dev") }
        maven { url = uri("https://kotlin.bintray.com/kotlin-eap") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
}

rootProject.name = rootProject.projectDir.name

include(
    "jdbc-utils",

    "network-core",
    "network-ktor",
    "network-ktx",

    "login-server",
    "game-server",
    "chat-server",

    "login-server-app"
)

project(":jdbc-utils").projectDir = file("common/jdbc-utils")
project(":network-core").projectDir = file("common/network-core")
project(":network-ktor").projectDir = file("common/network-ktor")
project(":network-ktx").projectDir = file("common/network-ktx")

project(":login-server-app").projectDir = file("dockers/login-server-app")