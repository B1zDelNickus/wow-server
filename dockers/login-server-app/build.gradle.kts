val ktor_version: String by project
val koin_version: String by project
val logback_version: String by project
val mu_logging_version: String by project

plugins {
    application
    id("com.github.johnrengelman.shadow") version "4.0.1"
    id("com.bmuschko.docker-java-application") version "6.1.3"
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

docker {
    javaApplication {
        baseImage.set("adoptopenjdk/openjdk11:latest")
        ports.set(listOf(8080))
        jvmArgs.set(listOf("-Xms256m", "-Xmx2048m"))
        mainClassName.set("io.ktor.server.netty.EngineMain")
    }
}

dependencies {

    implementation(project(":login-server"))

    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")

    implementation("org.koin:koin-ktor:$koin_version")
    implementation("org.koin:koin-logger-slf4j:$koin_version")
    implementation("org.koin:koin-ktor:$koin_version")

    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.github.microutils:kotlin-logging:$mu_logging_version")

}