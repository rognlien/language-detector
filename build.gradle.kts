
import org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask
import org.jlleitschuh.gradle.ktlint.tasks.KtLintFormatTask

plugins {
    id("groovy")
    id("java-library")
    id("maven-publish")

    kotlin("jvm") version "2.1.20"

    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

group = "com.github.rognlien"
version = "0.1-SNAPSHOT"

val baseVersion = property("baseVersion")
val buildNumber = System.getenv("BUILD_NUMBER")
version = buildNumber?.let { "$baseVersion.$buildNumber" } ?: ("$baseVersion.local")

val spockVersion = "2.4-M2-groovy-4.0"
val groovyVersion = "4.0.26"

java {
    withSourcesJar()
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

repositories {
    mavenCentral()
}

dependencies {

    testImplementation(platform("org.spockframework:spock-bom:$spockVersion"))
    testImplementation(platform("org.apache.groovy:groovy-bom:$groovyVersion"))
    testImplementation("org.apache.groovy:groovy")
    testImplementation("org.apache.groovy:groovy-json")
    testImplementation("org.apache.groovy:groovy-xml")

    testImplementation("org.spockframework:spock-core")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenKotlin") {
            from(components["java"])
            pom {
                name.set("language-detector")
                description.set("Simple language detection")
                url.set("https://github.com/rognlien/language-detector")
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/rognlien/language-detector")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<KtLintFormatTask>().configureEach {
    dependsOn(tasks.compileKotlin)
}

tasks.withType<KtLintCheckTask>().configureEach {
    dependsOn(tasks.compileKotlin)
}
