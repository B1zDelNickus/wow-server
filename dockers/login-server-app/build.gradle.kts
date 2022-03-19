val ktorVersion: String by project
val koinVersion: String by project
val logbackVersion: String by project
val muLoggingVersion: String by project

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

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")

    implementation("org.koin:koin-ktor:$koinVersion")
    implementation("org.koin:koin-logger-slf4j:$koinVersion")
    implementation("org.koin:koin-ktor:$koinVersion")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.github.microutils:kotlin-logging:$muLoggingVersion")

}