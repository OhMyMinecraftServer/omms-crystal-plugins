import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins{
    java
    application
    kotlin("jvm")
}
group = "com.github.ZhuRuoLing"
version = "0.0.1"

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
    implementation(kotlin("stdlib-jdk8"))
}
repositories {
    mavenCentral()
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "17"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "17"
}