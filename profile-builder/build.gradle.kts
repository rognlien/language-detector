import org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask
import org.jlleitschuh.gradle.ktlint.tasks.KtLintFormatTask

plugins {
    id("application")
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
}

application {
    mainClass.set("com.github.rognlien.MainKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":"))
}

kotlin {
    jvmToolchain(21)
}

tasks.named<JavaExec>("run") {
    workingDir = rootDir
}

tasks.withType<KtLintFormatTask>().configureEach {
    dependsOn(tasks.compileKotlin)
}

tasks.withType<KtLintCheckTask>().configureEach {
    dependsOn(tasks.compileKotlin)
}
