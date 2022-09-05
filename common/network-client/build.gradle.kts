group = "com.avp.wow.network-client"

dependencies {

    api(project(":network-core"))

}
repositories {
    maven {
        url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
        name = "ktor-eap"
    }
}
