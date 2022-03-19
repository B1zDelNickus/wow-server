val ktorVersion: String by project

group = "com.avp.wow.network-core"

dependencies {

    api("io.ktor:ktor-server-core:$ktorVersion")
    api("io.ktor:ktor-network-tls:$ktorVersion")
    api("javolution:javolution:5.5.1")
    api("com.fasterxml.uuid:java-uuid-generator:3.1.0")

}