private val nexus_url = System.getenv().getOrDefault("NEXUS_URL", "https://nexus.spectrum.codes")

group = "com.avp.wow.game"

dependencies {
    implementation(project(":jdbc-core"))
    implementation(project(":network-core"))
    implementation(project(":service"))
}