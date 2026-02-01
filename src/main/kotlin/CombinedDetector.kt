package com.github.rognlien

import kotlin.math.ln

object CombinedDetector {
    private const val SHORT_TEXT_NGRAM_SCALE = 0.3
    private const val LANGUAGE_COUNT = 34.0

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
        val originalStopwordResults = StopwordDetector.detectAll(text)
        val stopwordResults = originalStopwordResults.toMutableList()

        if (ngramResults.isEmpty() && stopwordResults.isEmpty()) return emptyList()

        if (isBasicLatinOnly(text) && originalStopwordResults.isEmpty()) {
            stopwordResults.add(DetectionResult("eng", ln(LANGUAGE_COUNT / 2.0)))
        }

        val wordCount = text.split(Regex("[^\\p{L}]+")).count { it.isNotEmpty() }
        val bonusInjected = stopwordResults.size > originalStopwordResults.size
        val ngramScale =
            when {
                wordCount >= 4 -> 1.0
                originalStopwordResults.isNotEmpty() -> 0.0
                bonusInjected -> SHORT_TEXT_NGRAM_SCALE
                else -> 1.0
            }

        val ngramMax = ngramResults.maxOfOrNull { it.score } ?: 0.0
        val stopwordMax = stopwordResults.maxOfOrNull { it.score } ?: 0.0

        val ngramNorm =
            if (ngramScale > 0.0 && ngramMax > 0.0) {
                ngramResults.associate { it.language to it.score / ngramMax * ngramScale }
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

    private fun isBasicLatinOnly(text: String): Boolean {
        return text.any { it.isLetter() } &&
            text.all { !it.isLetter() || it in 'A'..'Z' || it in 'a'..'z' }
    }
}
