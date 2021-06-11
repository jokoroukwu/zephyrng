plugins {
    kotlin("jvm") version "1.5.0"
    kotlin("plugin.serialization") version "1.5.0"
}

//val kotlinVersion = "1.5.0"
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
    testImplementation(kotlin("test-testng", "1.5.10"))
    testImplementation("io.mockk:mockk:1.11.0")
    testImplementation("org.assertj:assertj-core:3.19.0")
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
