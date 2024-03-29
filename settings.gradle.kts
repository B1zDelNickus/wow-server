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
    "jdbc-core",

    "model",
    "repository",
    "service",

    "network-core",
    "network-client",

    "login-server",
    "game-server",
    "chat-server",

    "login-server-app"
)

project(":jdbc-core").projectDir = file("common/jdbc-core")
project(":model").projectDir = file("common/model")
project(":repository").projectDir = file("common/repository")
project(":service").projectDir = file("common/service")
project(":network-core").projectDir = file("common/network-core")
project(":network-client").projectDir = file("common/network-client")

project(":login-server-app").projectDir = file("dockers/login-server-app")