val ktorVersion: String by project

group = "com.avp.wow.network-core"

dependencies {

    api("javolution:javolution:5.5.1")
    api("com.fasterxml.uuid:java-uuid-generator:3.1.0")
    api("io.ktor:ktor-server-core-jvm:2.0.0-eap-256")
    api("io.ktor:ktor-network-tls-jvm:2.0.0-eap-256")

}
repositories {
    maven {
        url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
        name = "ktor-eap"
    }
}
