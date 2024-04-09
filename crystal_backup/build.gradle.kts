plugins{
    kotlin("jvm")
    application
}
version = "0.0.1"
dependencies {
    compileOnly("icu.takeneko:omms-crystal:0.5.0")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}