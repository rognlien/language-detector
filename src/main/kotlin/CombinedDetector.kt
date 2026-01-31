package com.github.rognlien

object CombinedDetector {
    @JvmStatic
    fun detect(text: String): String? {
        return detectAll(text).firstOrNull()?.language
    }

    @JvmStatic
    fun detectAll(text: String): List<DetectionResult> {
        return detectAll(text, 0.5, 0.5)
    }

    @JvmStatic
    fun detectAll(
        text: String,
        ngramWeight: Double,
        stopwordWeight: Double,
    ): List<DetectionResult> {
        val ngramResults = LanguageDetector.detectAll(text)
        val stopwordResults = StopwordDetector.detectAll(text)

        if (ngramResults.isEmpty() && stopwordResults.isEmpty()) return emptyList()

        val wordCount = text.split(Regex("[^\\p{L}]+")).count { it.isNotEmpty() }
        val useNgrams = stopwordResults.isEmpty() || wordCount >= 4

        val ngramMax = ngramResults.maxOfOrNull { it.score } ?: 0.0
        val stopwordMax = stopwordResults.maxOfOrNull { it.score } ?: 0.0

        val ngramNorm =
            if (useNgrams && ngramMax > 0.0) {
                ngramResults.associate { it.language to it.score / ngramMax }
            } else {
                emptyMap()
            }

        val stopwordNorm =
            if (stopwordMax > 0.0) {
                stopwordResults.associate { it.language to it.score / stopwordMax }
            } else {
                emptyMap()
            }

        val allLanguages = ngramNorm.keys + stopwordNorm.keys

        return allLanguages.map { lang ->
            val ngramScore = ngramNorm[lang]
            val stopwordScore = stopwordNorm[lang]
            val blended =
                when {
                    ngramScore != null && stopwordScore != null ->
                        ngramScore * ngramWeight + stopwordScore * stopwordWeight
                    ngramScore != null -> ngramScore * ngramWeight
                    else -> stopwordScore!! * stopwordWeight
                }
            DetectionResult(lang, blended)
        }.sortedByDescending { it.score }
    }
}
