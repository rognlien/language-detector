package com.github.rognlien

import java.net.JarURLConnection
import java.nio.file.Files
import java.nio.file.Paths
import java.util.jar.JarFile
import kotlin.math.ln

/**
 * N-gram language detection over the profiles shipped in the jar.
 *
 * The profiles are held as one index from n-gram to the languages that know it, each weight
 * already multiplied by the n-gram's IDF. A detection is then one lookup per input n-gram, not
 * one per input n-gram per language, and the index holds each n-gram once rather than once per
 * profile: 385k entries for 720k profile rows, about half the heap of a map per profile.
 *
 * Loading and indexing the profiles takes well over half a second. It happens on first use
 * unless [preload] is called first, which a service should do while starting up.
 */
object LanguageDetector {
    private class Known(val languages: ByteArray, val weights: DoubleArray)

    private class Index(val languages: Array<String>, val known: Map<String, Known>)

    private val index: Index by lazy { buildIndex(loadProfiles()) }

    /** Loads and indexes the profiles now, so the first detection does not pay for it. */
    @JvmStatic
    fun preload() {
        index
    }

    @JvmStatic
    fun detect(text: String): String? {
        return detectAll(text).firstOrNull()?.language
    }

    @JvmStatic
    fun detectAll(text: String): List<DetectionResult> {
        val counts = NgramExtractor.count(text)
        if (counts.isEmpty()) return emptyList()

        val total = counts.values.sum().toDouble()
        val scores = DoubleArray(index.languages.size)
        for ((ngram, count) in counts) {
            val known = index.known[ngram] ?: continue
            val frequency = count / total
            for (k in known.languages.indices) {
                scores[known.languages[k].toInt()] += frequency * known.weights[k]
            }
        }

        return index.languages.indices
            .map { DetectionResult(index.languages[it], scores[it]) }
            .sortedByDescending { it.score }
    }

    private fun buildIndex(profiles: List<LanguageProfile>): Index {
        require(profiles.size <= Byte.MAX_VALUE) { "${profiles.size} profiles; the index addresses languages by byte" }

        val rows = HashMap<String, Pair<MutableList<Byte>, MutableList<Double>>>()
        profiles.forEachIndexed { language, profile ->
            for ((ngram, frequency) in profile.ngrams) {
                val (languages, frequencies) = rows.getOrPut(ngram) { ArrayList<Byte>(2) to ArrayList<Double>(2) }
                languages += language.toByte()
                frequencies += frequency
            }
        }

        val languageCount = profiles.size.toDouble()
        val known = HashMap<String, Known>(rows.size * 4 / 3 + 1)
        for ((ngram, row) in rows) {
            val (languages, frequencies) = row
            val idf = ln(languageCount / languages.size)
            known[ngram] = Known(languages.toByteArray(), DoubleArray(frequencies.size) { frequencies[it] * idf })
        }
        return Index(profiles.map { it.language }.toTypedArray(), known)
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

        // Decoded in parallel, one gzip stream per profile; the order of the names is kept.
        return names.parallelStream()
            .map { path -> cl.getResourceAsStream(path)?.use { LanguageProfileCodec.read(it) } }
            .toList()
            .filterNotNull()
    }
}
