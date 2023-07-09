repositories {
    maven {
        url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
        name = "ktor-eap"
    }
}
private val nexus_url = System.getenv().getOrDefault("NEXUS_URL", "https://nexus.spectrum.codes")

group = "com.avp.wow.login"

dependencies {
    implementation(project(":jdbc-core"))
    implementation(project(":network-core"))
    implementation(project(":service"))
    testImplementation(project(":network-client"))
    testImplementation(project(":game-server"))
}