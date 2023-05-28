plugins{
    kotlin("jvm") version "1.8.21"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(fileTree("../libs"))
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}