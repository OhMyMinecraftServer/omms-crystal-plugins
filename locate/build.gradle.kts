plugins{
    kotlin("jvm")
    application
}

//repositories {
//    mavenLocal()
//    mavenCentral()
//    maven {
//        url = uri("https://maven.takeneko.icu/releases")
//    }
//    maven("https://libraries.minecraft.net")
//}

dependencies {
    compileOnly("icu.takeneko:omms-crystal:0.5.0")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}