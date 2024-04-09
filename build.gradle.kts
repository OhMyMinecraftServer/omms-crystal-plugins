plugins{
    application
}
version = "0.0.1"
repositories {
    mavenLocal()
    mavenCentral()
}


task("buildAllPlugins"){
    subprojects {
        this@task.dependsOn(tasks.getByPath(":${name}:jar"))
    }
}

subprojects{
    this@subprojects.repositories {
        mavenLocal()
        mavenCentral()
        maven {
            url = uri("https://maven.takeneko.icu/releases")
        }
        maven("https://libraries.minecraft.net")
    }
}