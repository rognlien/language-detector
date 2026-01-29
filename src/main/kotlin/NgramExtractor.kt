package com.github.rognlien

object NgramExtractor {
    private val nonLetterPattern = Regex("[^\\p{L}]+")
    private val whitespacePattern = Regex("\\s+")

    val DEFAULT_NGRAM_RANGE: IntRange = 2..5

    private fun preprocess(text: String): String =
        text.lowercase()
            .replace(nonLetterPattern, " ")
            .replace(whitespacePattern, " ")
            .trim()

    @JvmStatic
    fun extract(
        text: String,
        nRange: IntRange = DEFAULT_NGRAM_RANGE,
    ): List<String> {
        val cleaned = preprocess(text)
        if (cleaned.isEmpty()) return emptyList()
        val padded = " $cleaned "
        val result = mutableListOf<String>()
        for (n in nRange) {
            if (padded.length >= n) {
                for (i in 0..padded.length - n) {
                    result += padded.substring(i, i + n)
                }
            }
        }
        return result
    }
}
