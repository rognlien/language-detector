package com.github.rognlien

class LanguageProfileBuilder(private val language: String) {
    private val ngrams = mutableMapOf<String, Int>()

    fun append(text: String) {
        NgramExtractor.extract(text).forEach {
            ngrams.merge(it, 1, Int::plus)
        }
    }

    fun build(maxNgrams: Int = DEFAULT_MAX_NGRAMS): LanguageProfile {
        val top = ngrams.entries
            .sortedByDescending { it.value }
            .take(maxNgrams)
        val total = top.sumOf { it.value }.toDouble()
        val normalized = top.associate { it.key to it.value / total }
        return LanguageProfile(language, normalized)
    }

    companion object {
        const val DEFAULT_MAX_NGRAMS = 20_000
    }
}
