pluginManagement {
    plugins {
        kotlin("jvm") version "1.9.21"
    }
}
rootProject.name = "omms-crystal-plugins"

include("crystal_backup")
include("locate")
include("simple_op")
include("advanced_calculator")
