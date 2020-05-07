val commons_codec_version: String by project

dependencies {

    api(project(":model"))
    api("commons-codec:commons-codec:$commons_codec_version")

}