package com.github.rognlien

import java.io.File

fun main(args: Array<String>) {
    require(args.size >= 2) { "Usage: <input-dir> <output-dir> [language-filter]" }

    val root = File(args[0])
    require(root.isDirectory) { "Not a directory: ${root.absolutePath}" }

    val outputDir = File(args[1])
    outputDir.mkdirs()

    val languageFilter = args.getOrNull(2)

    root.listFiles { it -> it.isDirectory && it.name.matches("[a-z]{3}".toRegex()) }
        ?.filter { languageFilter == null || it.name == languageFilter }
        ?.forEach { dir ->
            val language = dir.name
            val builder = LanguageProfileBuilder(language)

            dir.walkTopDown()
                .filter { it.isFile && it.extension.lowercase() == "txt" }
                .forEach { file ->
                    println("Processing ${file.name}")
                    file.useLines { lines ->
                        lines.take(40_000_000).forEachIndexed { i, line ->
                            if (i % 100_000 == 0) {
                                println(i)
                            }
                            builder.append(line)
                        }
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
