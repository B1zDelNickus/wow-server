val ktorVersion: String by project

//group = "com.avp.wow.network-core"

plugins {
    `maven-publish`
}

dependencies {

    api("javolution:javolution:5.5.1")
    api("com.fasterxml.uuid:java-uuid-generator:4.2.0")
    api("io.ktor:ktor-server-core-jvm:$ktorVersion")
    api("io.ktor:ktor-network-tls-jvm:$ktorVersion")

}

repositories {
    maven {
        url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
        name = "ktor-eap"
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            //groupId = "org.gradle.sample"
            artifactId = "network-core"
            //version = "1.1"

            from(components["kotlin"])
        }
    }
}