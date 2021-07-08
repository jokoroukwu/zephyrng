plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("maven-publish")
    id("signing")
}

group = "com.github.jokoroukwu"
version = "0.1.0"

repositories {
    mavenCentral()
    jcenter()
    maven {
        name = "sonatype"
        url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
        credentials {
            username = property("sonatypeUser") as String
            password = property("sonatypePassword") as String
        }
    }
    maven {
        name = "my-repo"
        url = uri("""C:\Users\JOkoroukwu\IdeaProjects\zephyrng\my-repo""")
    }
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

    withSourcesJar()
    withJavadocJar()

}

tasks.compileKotlin {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
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
