package com.github.rognlien

import java.io.File

fun main() {
    // Look for language specific directories
    val root = File("/Users/bendik/language-detection/data")
    require(root.isDirectory) { "Not a directory: ${root.absolutePath}" }

    val outputDir = File("/Users/bendik/language-detection/profiles/")

    root.listFiles { it.isDirectory && it.name.matches("[a-z]{3}".toRegex()) }?.forEach {
        val language = it.name
        val builder = LanguageProfileBuilder(language)

        it.walkTopDown()
            .filter { it.isFile && it.extension.lowercase() == "txt" }
            .forEach { file ->
                println("Processing ${file.name}")
                file.useLines { lines ->
                    lines.forEach { builder.append(it) }
                }
            }

        val profile = builder.build()
        val file = File(outputDir, "${profile.language}.bin")
        file.outputStream().use { out ->
            LanguageProfileCodec.write(profile, out)
        }

        println("Created profile: ${file.absolutePath}")
    }
}
