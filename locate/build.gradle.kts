plugins{
    kotlin("jvm") version "1.8.21"
    application
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://libraries.minecraft.net")
}

dependencies {
//    //implementation(fileTree("../libs"))
    implementation("com.github.ZhuRuoLing:omms-crystal:master-SNAPSHOT")
    implementation("com.google.code.gson:gson:2.10")
    implementation("org.slf4j:slf4j-api:2.0.3")
    implementation("ch.qos.logback:logback-core:1.4.4")
    implementation("ch.qos.logback:logback-classic:1.4.4")
    implementation("com.mojang:brigadier:1.0.18")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.20")
    implementation("org.apache.groovy:groovy:4.0.2")
    implementation("org.jline:jline:3.21.0")
    implementation("cn.hutool:hutool-all:5.8.11")
    implementation("commons-io:commons-io:2.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("com.alibaba.fastjson2:fastjson2:2.0.20.graal")
    implementation("net.kyori:adventure-api:4.13.1")
    implementation("net.kyori:adventure-text-serializer-gson:4.13.1")
    implementation("nl.vv32.rcon:rcon:1.2.0")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}