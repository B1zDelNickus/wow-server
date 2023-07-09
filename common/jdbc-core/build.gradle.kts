val kotlinCoroutinesVersion: String by project
val hikaricpVersion: String by project
val postgresDriverVersion: String by project
val flywayVersion: String by project
val commonsDbcp2Version: String by project
val c3poVersion: String by project
val testcontainersVersion: String by project
val exposedVersion: String by project

dependencies {

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("com.zaxxer:HikariCP:$hikaricpVersion")
    implementation("org.postgresql:postgresql:$postgresDriverVersion")
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.apache.commons:commons-dbcp2:$commonsDbcp2Version")
    implementation("com.mchange:c3p0:$c3poVersion")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

}