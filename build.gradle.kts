import org.gradle.api.tasks.testing.logging.TestLogEvent

val server_version: String by project
val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val koin_version: String by project
val hikaricp_version: String by project
val mu_logging_version: String by project
val postgres_driver_version: String by project
val testcontainers_version: String by project
val kluent_version: String by project
val junit_version: String by project
val kotlin_test_version: String by project
val assertj_version: String by project
val mockkk_version: String by project
val pipeline_processor_version: String by project
val apache_server_version: String by project

plugins {
    base
    kotlin("jvm") version "1.3.71"
}

allprojects {

    group = "com.avp.wow"
    version = server_version

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
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
        implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")

        /**
         * Log utils
         */
        implementation("ch.qos.logback:logback-classic:$logback_version")
        implementation("io.github.microutils:kotlin-logging:$mu_logging_version")

        /**
         * Tests deps
         */
        testImplementation("org.assertj:assertj-core:$assertj_version")
        testImplementation("io.kotlintest:kotlintest-runner-junit5:$kotlin_test_version")
        testImplementation("io.kotlintest:kotlintest-assertions:$kotlin_test_version")
        testImplementation("io.mockk:mockk:$mockkk_version")
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junit_version")
        testImplementation("org.junit.jupiter:junit-jupiter-params:$junit_version")
        testImplementation("org.testcontainers:testcontainers:$testcontainers_version")
        testImplementation("org.testcontainers:junit-jupiter:$testcontainers_version")
        testImplementation("org.testcontainers:postgresql:$testcontainers_version")
        testImplementation("org.testcontainers:kafka:$testcontainers_version")

        testRuntime("org.junit.jupiter:junit-jupiter-engine:$junit_version")

    }

    tasks.withType<Test> {
        useJUnitPlatform()
        failFast = true
        testLogging {
            events = setOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
        }
    }

}

