version = "0.0.1"
repositories {
    mavenLocal()
    mavenCentral()
}


tasks.register("buildAllPlugins", Copy::class.java){
    subprojects {
        this@register.dependsOn(tasks.getByPath(":${name}:jar"))
    }
    subprojects {
        from("${this.buildDir}/libs")
    }
    into(buildDir.resolve("libs"))
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