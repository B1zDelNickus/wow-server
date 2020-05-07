group = "com.avp.wow.network-ktor"

dependencies {
    implementation(project(":network-core"))
    implementation(project(":service"))
    testImplementation(project(":network-client"))
}