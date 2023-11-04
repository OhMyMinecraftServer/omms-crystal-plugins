plugins {
    kotlin("jvm") version "1.8.21"
    application
}

group = "io.github.optijava.plugins.crystal.advanced_calculator"
version = "0.0.1"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")
    maven("https://libraries.minecraft.net")
}

dependencies {
    implementation("com.github.ZhuRuoLing:omms-crystal:master-SNAPSHOT")

    implementation("org.slf4j:slf4j-api:2.0.3")
    implementation("ch.qos.logback:logback-core:1.4.4")
    implementation("ch.qos.logback:logback-classic:1.4.4")
    implementation("com.mojang:brigadier:1.0.18")

    implementation("net.kyori:adventure-api:4.13.1")
}


kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}
