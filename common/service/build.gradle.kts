val commonsCodecVersion: String by project

dependencies {

    api(project(":model"))
    api("commons-codec:commons-codec:$commonsCodecVersion")

}