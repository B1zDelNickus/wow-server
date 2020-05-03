val ktor_version: String by project

group = "com.avp.wow.network-core"

dependencies {

    api("io.ktor:ktor-server-core:$ktor_version")
    api("io.ktor:ktor-network-tls:$ktor_version")
    api("javolution:javolution:5.5.1")
    api("com.fasterxml.uuid:java-uuid-generator:3.1.0")

}