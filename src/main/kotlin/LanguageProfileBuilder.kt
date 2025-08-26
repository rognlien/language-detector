package com.github.rognlien

class LanguageProfileBuilder(private val language: String) {
    private val ngrams = mutableMapOf<String, Int>()

    val nRange: IntRange = 2..3

    fun append(text: String) {
        NgramExtractor.extract(text, nRange).forEach {
            ngrams.merge(it, 1, Int::plus)
        }
    }

    fun build(): LanguageProfile {
        val total = ngrams.values.sum().toDouble()
        val normalized = ngrams.mapValues { it.value / total }
        return LanguageProfile(language, normalized)
    }
}
