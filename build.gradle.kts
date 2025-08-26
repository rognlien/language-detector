
import org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask
import org.jlleitschuh.gradle.ktlint.tasks.KtLintFormatTask

plugins {
    id("groovy")
    id("maven-publish")

    kotlin("jvm") version "2.1.20"

    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

group = "com.github.rognlien"
version = "1.0-SNAPSHOT"
val spockVersion = "2.4-M2-groovy-4.0"
val groovyVersion = "4.0.26"

/*
subprojects {
    plugins.withId("groovy") {
        dependencies {
            testImplementation(platform("org.spockframework:spock-bom:$spockVersion"))
            testImplementation(platform("org.apache.groovy:groovy-bom:$groovyVersion"))
            testImplementation("org.apache.groovy:groovy")
            testImplementation("org.apache.groovy:groovy-json")
            testImplementation("org.apache.groovy:groovy-xml")

            testImplementation("org.spockframework:spock-core")
        }

        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }

    plugins.withId("org.jetbrains.kotlin.jvm") {
        repositories {
            mavenCentral()
        }
    }
}
*/

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
kotlin {
    jvmToolchain(21)
}

tasks.withType<KtLintFormatTask>().configureEach {
    dependsOn(tasks.compileKotlin)
}

tasks.withType<KtLintCheckTask>().configureEach {
    dependsOn(tasks.compileKotlin)
}
