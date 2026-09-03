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
        val result = mutableListOf<String>()
        forEachNgram(text, nRange) { result += it }
        return result
    }

    /** The n-grams of [text] with their counts: what [extract] yields, without the list in between. */
    @JvmStatic
    fun count(
        text: String,
        nRange: IntRange = DEFAULT_NGRAM_RANGE,
    ): Map<String, Int> {
        val counts = HashMap<String, Int>()
        forEachNgram(text, nRange) { counts.merge(it, 1, Int::plus) }
        return counts
    }

    private inline fun forEachNgram(
        text: String,
        nRange: IntRange,
        consume: (String) -> Unit,
    ) {
        val cleaned = preprocess(text)
        if (cleaned.isEmpty()) return
        val padded = " $cleaned "
        for (n in nRange) {
            if (padded.length >= n) {
                for (i in 0..padded.length - n) {
                    consume(padded.substring(i, i + n))
                }
            }
        }
    }
}
