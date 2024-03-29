import org.gradle.api.tasks.testing.logging.TestLogEvent

val serverVersion: String by project
val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val koinVersion: String by project
val hikaricpVersion: String by project
val muLoggingVersion: String by project
val postgresDriverVersion: String by project
val testcontainersVersion: String by project
val junitVersion: String by project
val kotlinTestVersion: String by project
val assertjVersion: String by project
val mockkVersion: String by project
val pipelineProcessorVersion: String by project
val apacheServerVersion: String by project

plugins {
    base
    kotlin("jvm") version "1.7.10"
}

allprojects {

    group = "com.avp.wow"
    version = serverVersion

    apply(plugin = "kotlin")

    repositories {
        mavenLocal()
        mavenCentral()

        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
        maven { url = uri("https://kotlin.bintray.com/ktor") }
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://dl.bintray.com/spekframework/spek-dev") }
        maven { url = uri("https://dl.bintray.com/kotlin/kotlin-eap") }

        jcenter()
        google()
    }

    dependencies {

        /**
         * KT deps
         */
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
        implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

        /**
         * Log utils
         */
        implementation("ch.qos.logback:logback-classic:$logbackVersion")
        implementation("io.github.microutils:kotlin-logging:$muLoggingVersion")

        /**
         * Tests deps
         */
        testImplementation("org.assertj:assertj-core:$assertjVersion")
        testImplementation("io.kotlintest:kotlintest-runner-junit5:$kotlinTestVersion")
        testImplementation("io.kotlintest:kotlintest-assertions:$kotlinTestVersion")
        testImplementation("io.mockk:mockk:$mockkVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
        testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
        testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
        testImplementation("org.testcontainers:postgresql:$testcontainersVersion")

        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    }

    tasks.withType<Test> {
        useJUnitPlatform()
        failFast = true
        testLogging {
            events = setOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
        }
        maxHeapSize = "512m"
    }

}

