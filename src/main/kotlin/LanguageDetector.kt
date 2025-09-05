package com.github.rognlien

import java.net.JarURLConnection
import java.nio.file.Files
import java.nio.file.Paths
import java.util.jar.JarFile

object LanguageDetector {
    private val nRange = 2..3
    private val profiles: List<LanguageProfile>

    init {
        profiles = loadProfiles()
        profiles.forEach { println("Profile: ${it.language} ${it.ngrams.size}") }
    }

    @JvmStatic
    fun detect(text: String): String? {
        val ngrams = NgramExtractor.extract(text, nRange)
        if (ngrams.isEmpty()) return null

        val inputFreq = ngrams.groupingBy { it }.eachCount()
        val inputTotal = inputFreq.values.sum().toDouble()
        val inputNorm = inputFreq.mapValues { it.value / inputTotal }

        return profiles.maxByOrNull { profile ->
            score(inputNorm, profile.ngrams)
        }?.language
    }

    private fun score(
        input: Map<String, Double>,
        profile: Map<String, Double>,
    ): Double {
        return input.entries.sumOf { (ng, freq) ->
            freq * (profile[ng] ?: 0.0)
        }
    }

    private fun loadProfiles(): List<LanguageProfile> {
        val root = "profiles" // no leading slash for ClassLoader
        val cl = this::class.java.classLoader
        val urls = cl.getResources(root) // may be file: or jar:

        val names = mutableListOf<String>()

        while (urls.hasMoreElements()) {
            val url = urls.nextElement()
            when (url.protocol) {
                "file" -> {
                    val dir = Paths.get(url.toURI())
                    Files.list(dir).use { stream ->
                        stream.filter { it.fileName.toString().endsWith(".bin") }
                            .forEach { names += "$root/${it.fileName}" }
                    }
                }
                "jar" -> {
                    val conn = url.openConnection() as JarURLConnection
                    val jar: JarFile = conn.jarFile
                    val prefix = "$root/"
                    jar.entries().asIterator().forEachRemaining { entry ->
                        if (!entry.isDirectory && entry.name.startsWith(prefix) && entry.name.endsWith(".bin")) {
                            names += entry.name
                        }
                    }
                }
            }
        }

        return names.mapNotNull { path ->
            cl.getResourceAsStream(path)?.use { LanguageProfileCodec.read(it) }
        }
    }
}
