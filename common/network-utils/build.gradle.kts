val ktor_version: String by project

group = "com.avp.wow.network"

dependencies {

    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-network-tls:$ktor_version")

}