private val nexus_url = System.getenv().getOrDefault("NEXUS_URL", "https://nexus.spectrum.codes")

group = "com.avp.wow.login"

dependencies {
    implementation(project(":jdbc-core"))
}