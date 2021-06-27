plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "com.gmail.johnokoroukwu"
version = "0.1.0"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1")

    implementation("org.snakeyaml:snakeyaml-engine:2.3")
    implementation("com.github.kittinunf.fuel:fuel:2.2.3")
    implementation("org.testng:testng:7.3.0")
    implementation("io.github.microutils:kotlin-logging:2.0.6")
    testImplementation("io.mockk:mockk:1.10.6")
    testImplementation("org.assertj:assertj-core:3.19.0")
    testImplementation("com.github.tomakehurst:wiremock-jre8:2.27.2")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.2.3")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
tasks.compileKotlin {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

tasks.test {
    useTestNG()

    testLogging {
        showStandardStreams = true
    }
}
