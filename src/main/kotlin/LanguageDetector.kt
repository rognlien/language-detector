package com.github.rognlien

import java.net.JarURLConnection
import java.nio.file.Files
import java.nio.file.Paths
import java.util.jar.JarFile

object LanguageDetector {
    private val profiles: List<LanguageProfile> by lazy { loadProfiles() }
    private val idfWeights: Map<String, Double> by lazy { computeIdf(profiles) }

    data class DetectionResult(val language: String, val score: Double)

    @JvmStatic
    fun detect(text: String): String? {
        return detectAll(text).firstOrNull()?.language
    }

    @JvmStatic
    fun detectAll(text: String): List<DetectionResult> {
        val ngrams = NgramExtractor.extract(text)
        if (ngrams.isEmpty()) return emptyList()

        val inputFreq = ngrams.groupingBy { it }.eachCount()
        val inputTotal = inputFreq.values.sum().toDouble()
        val inputNorm = inputFreq.mapValues { it.value / inputTotal }

        return profiles.map { profile ->
            DetectionResult(profile.language, score(inputNorm, profile.ngrams))
        }.sortedByDescending { it.score }
    }

    private fun score(
        input: Map<String, Double>,
        profile: Map<String, Double>,
    ): Double {
        return input.entries.sumOf { (ng, freq) ->
            freq * (profile[ng] ?: 0.0) * (idfWeights[ng] ?: 1.0)
        }
    }

    private fun computeIdf(profiles: List<LanguageProfile>): Map<String, Double> {
        val n = profiles.size.toDouble()
        val docFreq = mutableMapOf<String, Int>()
        for (profile in profiles) {
            for (ng in profile.ngrams.keys) {
                docFreq.merge(ng, 1, Int::plus)
            }
        }
        return docFreq.mapValues { (_, df) -> kotlin.math.ln(n / df) }
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
