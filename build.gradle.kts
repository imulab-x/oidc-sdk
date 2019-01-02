import groovy.lang.GroovyObject
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    java
    `java-library`
    `maven-publish`
    kotlin("jvm") version "1.3.10"
    id("com.jfrog.artifactory") version "4.8.1"
    id("com.gradle.build-scan") version "2.1"
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
    publishAlways()
}

group = "io.imulab.x"
version = "0.1.1"

repositories {
    maven(url = "https://artifactory.imulab.io/artifactory/gradle-dev-local/")
    jcenter()
    mavenCentral()
}

tasks {
    compileKotlin {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
        kotlinOptions {
            jvmTarget = "1.8"
            suppressWarnings = true
            freeCompilerArgs = listOf()
        }
    }
    compileTestKotlin {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
        kotlinOptions {
            jvmTarget = "1.8"
            suppressWarnings = true
            freeCompilerArgs = listOf()
        }
    }
    test {
        useJUnitPlatform()
    }
}

publishing {
    publications {
        create<MavenPublication>("oidc-sdk") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(components["java"])
            pom {
                name.set(artifactId)
                developers {
                    developer {
                        id.set("imulab")
                        name.set("Weinan Qiu")
                        email.set("davidiamyou@gmail.com")
                    }
                }
            }
        }
    }
}

artifactory {
    setContextUrl(System.getenv("ARTIFACTORY_CONTEXT_URL") ?: "https://artifactory.imulab.io/artifactory")
    publish(delegateClosureOf<PublisherConfig> {
        repository(delegateClosureOf<GroovyObject> {
            setProperty("repoKey", System.getenv("ARTIFACTORY_REPO") ?: "gradle-dev-local")
            setProperty("username", System.getenv("ARTIFACTORY_USERNAME") ?: "imulab")
            setProperty("password", System.getenv("ARTIFACTORY_PASSWORD") ?: "changeme")
            setProperty("maven", true)
        })
        defaults(delegateClosureOf<GroovyObject> {
            invokeMethod("publications", "oidc-sdk")
        })
    })
}

dependencies {
    implementation(platform("io.imulab.x:astrea-dependencies:3"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    api("io.imulab.x:oauth-sdk:0.1.1")

    testImplementation("io.kotlintest:kotlintest-runner-junit5")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin")
    testRuntime("org.slf4j:slf4j-api")
    testRuntime("org.apache.logging.log4j:log4j-api")
    testRuntime("org.apache.logging.log4j:log4j-core")
    testRuntime("org.apache.logging.log4j:log4j-slf4j-impl")
}