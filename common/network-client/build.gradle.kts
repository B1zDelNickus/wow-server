//group = "com.avp.wow.network-client"

plugins {
    `maven-publish`
}

dependencies {

    api(project(":network-core"))

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
            artifactId = "network-client"
            //version = "1.1"

            from(components["kotlin"])
        }
    }
}
