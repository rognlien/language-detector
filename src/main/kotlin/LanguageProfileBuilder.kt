package com.github.rognlien

class LanguageProfileBuilder(private val language: String) {
    private val ngrams = mutableMapOf<String, Long>()

    fun append(text: String) {
        append(text, 1L)
    }

    fun append(
        text: String,
        weight: Long,
    ) {
        require(weight > 0) { "Weight must be positive, was $weight" }
        NgramExtractor.count(text).forEach { (ngram, count) ->
            ngrams.merge(ngram, count * weight, Long::plus)
        }
    }

    fun build(maxNgrams: Int = DEFAULT_MAX_NGRAMS): LanguageProfile {
        val top =
            ngrams.entries
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
