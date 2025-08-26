package com.github.rognlien

object NgramExtractor {
    private val allowedLetters = Regex("[^a-zæøåáéíóúýðþöčđŋŧšžń]+")

    private fun preprocess(text: String): String =
        text.lowercase()
            .replace(allowedLetters, " ")
            .replace(Regex("\\s+"), " ")
            .trim()

    @JvmStatic
    fun extract(
        text: String,
        nRange: IntRange = 2..3,
    ): List<String> {
        val cleaned = preprocess(text)
        val result = mutableListOf<String>()
        val words = cleaned.split(" ")
        for (word in words) {
            for (n in nRange) {
                if (word.length >= n) {
                    for (i in 0..word.length - n) {
                        result += word.substring(i, i + n)
                    }
                }
            }
        }
        return result
    }
}
