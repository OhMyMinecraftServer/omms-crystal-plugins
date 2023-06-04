plugins{
    kotlin("jvm") version "1.8.21"
    application
}
version = "0.0.1"
repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    //implementation(fileTree("../libs"))
    //implementation("com.github.ZhuRuoLing:omms-crystal:master-SNAPSHOT")
}
kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}


task("buildAllPlugins"){
    subprojects {
        this@task.dependsOn(tasks.getByPath(":${name}:jar"))
    }
}