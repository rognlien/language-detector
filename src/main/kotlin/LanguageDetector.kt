package com.github.rognlien

import java.io.File

object LanguageDetector {
    private val nRange = 2..3
    private val profiles: List<LanguageProfile>

    init {
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
        return Thread.currentThread().contextClassLoader?.getResource("profiles")?.toURI()?.let {
            loadProfiles(File(it))
        } ?: emptyList()
    }

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
