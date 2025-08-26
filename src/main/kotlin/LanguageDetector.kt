package com.github.rognlien

import java.io.File
import java.net.JarURLConnection
import java.nio.file.Files
import java.nio.file.Paths
import java.util.jar.JarFile

object LanguageDetector {
    private val nRange = 2..3
    private val profiles: List<LanguageProfile>

    init {
        println("Loading Profiles")
        profiles = loadProfiles()
        println("Profiles: ${profiles.joinToString { it.language }}")
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

    /*
    private fun loadProfiles(): List<LanguageProfile> {
        return this::class.java.getResource("/profiles")?.toURI()?.let {
            loadProfiles(File(it))
        } ?: emptyList()
    }

     */

    private fun loadProfiles(dir: File): List<LanguageProfile> {
        return dir.listFiles { file -> file.extension == "bin" }?.map { file ->
            loadProfile(file)
        } ?: emptyList()
    }

    private fun loadProfile(file: File): LanguageProfile {
        return file.inputStream().use { input ->
            LanguageProfileCodec.read(input)
        }
    }
}
