plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
    id("maven-publish")
    id("signing")
}

group = "io.github.jokoroukwu"
version = "0.1.1"

repositories {
    mavenCentral()
    mavenLocal()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib", "1.4.10"))
    implementation("org.testng:testng:7.3.0")
    implementation("io.github.microutils:kotlin-logging:2.0.6")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    api("io.github.jokoroukwu:zephyr-api:0.1.1")

    testImplementation("io.mockk:mockk:1.10.6")
    testImplementation("org.assertj:assertj-core:3.19.0")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.2.3")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    withSourcesJar()
    withJavadocJar()
}

tasks.compileKotlin {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

publishing {
    publications {
        create<MavenPublication>("zephyrng") {
            from(components["java"])
            pom {
                name.set("zephyrng")
                description.set("A simple library for publishing TestNG test results using Zephyr for JIRA Server API")
                url.set("https://github.com/jokoroukwu/zephyrng")
                licenses {
                    license {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                }
                developers {
                    developer {
                        id.set("jokoroukwu")
                        name.set("John Okoroukwu")
                        email.set("john.okoroukwu@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com//jokoroukwu/zephyrng.git")
                    developerConnection.set("scm:git:ssh://jokoroukwu/zephyrng.git")
                    url.set("https://github.com/jokoroukwu/zephyrng")
                }
            }
        }
    }
    repositories {
        maven {
            name = "sonatype"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = property("sonatypeUser") as String
                password = property("sonatypePassword") as String
            }
        }
    }
}

signing {
    sign(publishing.publications["zephyrng"])
}


tasks.test {
    useTestNG()
    testLogging {
        showStandardStreams = true
    }
}
