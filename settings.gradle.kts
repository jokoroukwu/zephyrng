rootProject.name = "zephyrng"

pluginManagement {
    plugins {
        kotlin("jvm") version "1.4.10"
        kotlin("plugin.serialization") version "1.4.10"
        id("maven-publish")
        id("signing")
    }
}

