plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "com.gmail.suneclips3"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.testng:testng:7.3.0")
    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    implementation("com.charleskorn.kaml:kaml:0.26.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")

}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
tasks.compileKotlin {
    kotlinOptions {
        jvmTarget = "11"
    }
}

tasks.test {
    filter {
        includeTestsMatching("com.gmail.suneclips3.GetTestCaseTest*")
    }
    testLogging {
        showStandardStreams = true
    }
    useTestNG()
}
