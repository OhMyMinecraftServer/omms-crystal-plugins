plugins{
    kotlin("jvm") version "1.8.21"
    application
}
version = "0.0.1"
repositories {
    mavenCentral()
}

dependencies{
    implementation(fileTree("../libs"))
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}