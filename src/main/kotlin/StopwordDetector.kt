package com.github.rognlien

import kotlin.math.ln

object StopwordDetector {
    private val stopwords: Map<String, Set<String>> by lazy { buildStopwords() }
    private val idfWeights: Map<String, Double> by lazy { computeIdf(stopwords) }

    @JvmStatic
    fun detect(text: String): String? {
        return detectAll(text).firstOrNull()?.language
    }

    @JvmStatic
    fun detectAll(text: String): List<DetectionResult> {
        val words = tokenize(text)
        if (words.isEmpty()) return emptyList()

        val scores =
            stopwords.map { (language, wordSet) ->
                val score =
                    words.sumOf { word ->
                        if (word in wordSet) idfWeights[word] ?: 0.0 else 0.0
                    }
                DetectionResult(language, score)
            }.filter { it.score > 0.0 }
                .sortedByDescending { it.score }

        return scores
    }

    private fun tokenize(text: String): List<String> {
        return text.split(Regex("[^\\p{L}]+"))
            .filter { it.isNotEmpty() }
            .map { it.lowercase() }
    }

    private fun computeIdf(stopwords: Map<String, Set<String>>): Map<String, Double> {
        val n = stopwords.size.toDouble()
        val docFreq = mutableMapOf<String, Int>()
        for ((_, words) in stopwords) {
            for (word in words) {
                docFreq.merge(word, 1, Int::plus)
            }
        }
        return docFreq.mapValues { (_, df) ->
            if (df >= n.toInt()) 0.0 else ln(n / df)
        }
    }

    private fun buildStopwords(): Map<String, Set<String>> =
        mapOf(
            "eng" to
                setOf(
                    "the", "a", "an", "is", "are", "was", "were", "be", "been", "being",
                    "have", "has", "had", "do", "does", "did", "will", "would", "shall",
                    "should", "may", "might", "must", "can", "could", "this", "that",
                    "these", "those", "not", "and", "but", "or", "if", "then", "than",
                ),
            "fre" to
                setOf(
                    "le", "la", "les", "un", "une", "des", "du", "de", "est", "sont",
                    "suis", "nous", "vous", "ils", "elles", "ce", "cette", "ces", "mon",
                    "ton", "son", "notre", "votre", "leur", "que", "qui", "dans", "pour",
                    "pas", "avec", "sur", "mais", "ou", "et", "au", "aux",
                ),
            "ger" to
                setOf(
                    "der", "die", "das", "ein", "eine", "ist", "sind", "war", "waren",
                    "und", "oder", "aber", "nicht", "auch", "ich", "du", "er", "sie",
                    "wir", "ihr", "den", "dem", "des", "auf", "mit", "von", "zu",
                    "haben", "wird", "kann", "nach", "aus", "wie", "noch", "wenn",
                ),
            "spa" to
                setOf(
                    "el", "la", "los", "las", "un", "una", "unos", "unas", "es", "son",
                    "fue", "del", "de", "en", "que", "por", "con", "para", "como",
                    "pero", "este", "esta", "estos", "estas", "ese", "esa", "hay",
                    "muy", "todo", "esta", "sino", "desde", "hasta", "sobre", "entre",
                ),
            "ita" to
                setOf(
                    "il", "lo", "la", "gli", "le", "un", "uno", "una", "di", "del",
                    "della", "dei", "delle", "che", "non", "sono", "per", "con",
                    "questo", "questa", "questi", "quello", "quella", "anche", "come",
                    "suo", "sua", "loro", "fra", "tra", "tutto", "ogni", "molto", "sempre",
                ),
            "por" to
                setOf(
                    "o", "os", "um", "uma", "uns", "umas", "de", "do", "da", "dos",
                    "das", "em", "no", "na", "nos", "nas", "que", "por", "com", "para",
                    "como", "mas", "ou", "se", "este", "esta", "esse", "essa", "isso",
                    "muito", "mais", "foi", "ser", "ter", "seu", "sua",
                ),
            "dut" to
                setOf(
                    "de", "het", "een", "van", "en", "in", "is", "dat", "op", "te",
                    "aan", "met", "voor", "er", "zijn", "die", "dit", "niet", "ook",
                    "maar", "om", "bij", "nog", "wel", "dan", "naar", "uit", "wat",
                    "als", "werd", "meer", "hun", "waar", "kunnen", "heeft",
                ),
            "nob" to
                setOf(
                    "og", "i", "er", "det", "en", "et", "at", "den", "til", "av",
                    "som", "med", "har", "var", "men", "om", "vi", "kan", "han",
                    "hun", "jeg", "ikke", "seg", "skulle", "ville", "ble", "fra",
                    "denne", "dette", "eller", "etter", "over", "under", "bare",
                ),
            "nno" to
                setOf(
                    "og", "i", "er", "det", "ei", "eit", "den", "til", "av", "som",
                    "med", "har", "var", "men", "om", "vi", "kan", "han", "ho",
                    "eg", "ikkje", "seg", "skulle", "ville", "vart", "frå", "denne",
                    "dette", "eller", "etter", "kva", "nokon", "noko", "sjølv",
                ),
            "swe" to
                setOf(
                    "och", "i", "att", "en", "det", "som", "den", "av", "med", "har",
                    "till", "var", "jag", "kan", "inte", "men", "ett", "om", "hans",
                    "ska", "efter", "hade", "sedan", "mycket", "alla", "sina",
                    "detta", "dessa", "skulle", "bara", "utan", "eller", "vid", "varit",
                ),
            "dan" to
                setOf(
                    "og", "i", "er", "det", "en", "den", "til", "af", "som", "med",
                    "har", "var", "men", "om", "vi", "kan", "han", "hun", "jeg",
                    "ikke", "sig", "skulle", "ville", "blev", "fra", "denne", "dette",
                    "eller", "efter", "over", "under", "kun", "også", "meget",
                ),
            "cat" to
                setOf(
                    "el", "la", "els", "les", "un", "una", "uns", "unes", "de", "del",
                    "dels", "al", "als", "amb", "per", "que", "com", "tot", "aquesta",
                    "aquest", "aquestes", "aquell", "aquella", "molt", "seva", "seu",
                    "seva", "han", "hem", "heu", "han", "ser", "dins", "sobre",
                ),
            "pol" to
                setOf(
                    "nie", "to", "si\u0119", "jest", "na", "co", "tak", "za", "do",
                    "jak", "ale", "czy", "tym", "po", "te", "jej", "go", "tego",
                    "od", "ich", "dla", "przez", "tylko", "oraz", "jego", "jeszcze",
                    "kt\u00f3ry", "kt\u00f3ra", "kt\u00f3re", "tego", "tym", "bardzo",
                    "ju\u017c", "tak\u017ce", "mo\u017ce", "tutaj",
                ),
            "rum" to
                setOf(
                    "de", "la", "al", "ale", "din", "pe", "cu", "este", "sunt", "fost",
                    "fi", "nu", "ce", "care", "mai", "dar", "sau", "ca", "lui", "ei",
                    "lor", "acest", "aceast\u0103", "prin", "pentru", "aici", "doar",
                    "dac\u0103", "foarte", "toate", "chiar", "avea", "putea", "trebui",
                ),
            "tur" to
                setOf(
                    "bir", "ve", "bu", "da", "de", "ile", "gibi", "i\u00e7in",
                    "ama", "var", "olan", "olarak", "daha", "en", "kadar",
                    "\u00e7ok", "baz\u0131", "sonra", "herhangi", "ya", "ayn\u0131",
                    "hem", "ise", "ancak", "bile", "taraf\u0131ndan", "oldu",
                    "olan", "\u015fey", "her", "olmu\u015ftur", "bunun", "eden",
                ),
            "fin" to
                setOf(
                    "ja", "on", "ei", "se", "ett\u00e4", "oli", "olla", "ole",
                    "h\u00e4n", "mutta", "tai", "niin", "kuin", "kun", "jo",
                    "vain", "mit\u00e4", "joka", "my\u00f6s", "ovat", "siit\u00e4",
                    "heid\u00e4n", "t\u00e4m\u00e4", "t\u00e4ss\u00e4",
                    "t\u00e4ll\u00e4", "sen", "sit\u00e4", "mik\u00e4",
                    "kanssa", "ennen", "kaikki", "paljon", "hyvin", "viel\u00e4",
                ),
            "hun" to
                setOf(
                    "a", "az", "egy", "is", "nem", "hogy", "volt", "van", "meg",
                    "ez", "aki", "ami", "ezt", "mint", "de", "csak", "m\u00e9g",
                    "vagy", "mind", "azt", "igen", "kell", "lesz", "lett",
                    "fel", "arra", "erre", "t\u00f6bb", "\u00e9s", "sem",
                    "m\u00e1r", "nagyon", "akkor", "mikor",
                ),
            "cze" to
                setOf(
                    "je", "to", "na", "se", "ne", "si", "ale", "jak", "tak", "pod",
                    "pro", "za", "po", "aby", "jsou", "jako", "bylo", "jeho",
                    "jej\u00ed", "kter\u00fd", "kter\u00e1", "kter\u00e9", "nebo",
                    "byl", "byla", "tento", "tato", "tedy", "v\u0161ak",
                    "jen", "ji\u017e", "velmi", "tak\u00e9", "jejich",
                ),
            "slo" to
                setOf(
                    "je", "to", "na", "sa", "ne", "si", "ale", "ako", "tak", "za",
                    "po", "aby", "bol", "bola", "boli", "jeho", "jej",
                    "ktor\u00fd", "ktor\u00e1", "ktor\u00e9", "alebo",
                    "tento", "t\u00e1to", "preto", "v\u0161ak",
                    "len", "ve\u013emi", "tie\u017e", "ich", "sme", "som", "ste",
                ),
            "hrv" to
                setOf(
                    "je", "to", "na", "se", "ne", "su", "ali", "kao", "tako", "za",
                    "po", "bio", "bila", "bili", "njegov", "njezin",
                    "koji", "koja", "koje", "ili", "ovaj", "ova",
                    "zato", "ipak", "samo", "nego", "ima", "biti", "vrlo",
                    "njihov", "tome", "toga", "nisu", "smo",
                ),
            "lit" to
                setOf(
                    "ir", "yra", "tai", "kad", "ne", "ar", "bet", "kaip", "taip",
                    "nuo", "buvo", "jo", "jos", "kuris", "kuri", "kurie",
                    "arba", "\u0161is", "\u0161i", "\u0161ie", "tod\u0117l",
                    "ta\u010diau", "tik", "labai", "dar", "jau",
                    "gali", "turi", "apie", "tarp", "pagal", "prie", "d\u0117l",
                ),
            "ind" to
                setOf(
                    "dan", "di", "yang", "ini", "itu", "dengan", "untuk", "dari",
                    "pada", "adalah", "ke", "tidak", "akan", "juga", "sudah",
                    "ada", "bisa", "oleh", "atau", "saya", "kami", "mereka",
                    "anda", "sangat", "telah", "hanya", "tetapi", "seperti",
                    "bahwa", "jika", "karena", "antara", "lebih", "semua",
                ),
            "afr" to
                setOf(
                    "die", "en", "van", "in", "is", "het", "wat", "vir", "op", "met",
                    "nie", "ek", "sy", "hy", "ons", "hulle", "daar", "kan", "sal",
                    "hul", "was", "ook", "maar", "aan", "dit", "nog", "tot",
                    "deur", "baie", "onder", "oor", "hierdie", "daardie", "elke",
                ),
            "isl" to
                setOf(
                    "og", "a\u00f0", "er", "\u00ed", "\u00e1", "\u00e9g",
                    "var", "en", "ekki", "um", "vi\u00f0", "til", "af",
                    "\u00fe\u00e1", "\u00feetta", "sem", "me\u00f0",
                    "hefur", "hef\u00f0i", "eru", "ver\u00f0ur", "hans",
                    "hennar", "\u00feess", "\u00feeirra", "h\u00fan", "hann",
                    "vera", "einn", "eitt", "einu", "eftir", "yfir", "undir",
                ),
            "lat" to
                setOf(
                    "et", "in", "est", "non", "ad", "cum", "sed", "quod", "qui",
                    "quae", "quam", "sunt", "aut", "ab", "ex", "per", "hoc",
                    "haec", "enim", "atque", "eius", "esse", "ipse", "ipsa",
                    "etiam", "tamen", "autem", "ante", "post", "omnis", "omnia",
                    "inter", "erat", "fuit",
                ),
        )
}
