plugins {
    kotlin("jvm")
    application
}

group = "io.github.optijava.plugins.crystal.advanced_calculator"
version = "0.0.1"


dependencies {
    compileOnly("icu.takeneko:omms-crystal:0.5.0")

}

kotlin {
    jvmToolchain(17)
}