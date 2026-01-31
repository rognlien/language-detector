package com.github.rognlien

import kotlin.math.ln

object StopwordDetector {
    private val stopwords: Map<String, Set<String>> by lazy { buildStopwords() }
    private val idfWeights: Map<String, Double> by lazy { computeIdf(stopwords) }
    private val charHints: Map<Char, Set<String>> by lazy { buildCharHints() }
    private val charIdfWeights: Map<Char, Double> by lazy { computeCharIdf(charHints) }

    @JvmStatic
    fun detect(text: String): String? {
        return detectAll(text).firstOrNull()?.language
    }

    @JvmStatic
    fun detectAll(text: String): List<DetectionResult> {
        val words = tokenize(text)
        val chars = text.lowercase().toSet()
        if (words.isEmpty() && chars.none { it in charHints }) return emptyList()

        val scoreMap = mutableMapOf<String, Double>()

        for ((language, wordSet) in stopwords) {
            val score = words.sumOf { word -> if (word in wordSet) idfWeights[word] ?: 0.0 else 0.0 }
            if (score > 0.0) scoreMap[language] = score
        }

        for (ch in chars) {
            val languages = charHints[ch] ?: continue
            val weight = charIdfWeights[ch] ?: continue
            for (language in languages) {
                scoreMap.merge(language, weight, Double::plus)
            }
        }

        return scoreMap.map { (language, score) -> DetectionResult(language, score) }
            .sortedByDescending { it.score }
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

    private fun computeCharIdf(charHints: Map<Char, Set<String>>): Map<Char, Double> {
        val n = stopwords.size.toDouble()
        return charHints.mapValues { (_, langs) ->
            val df = langs.size
            if (df >= n.toInt()) 0.0 else ln(n / df)
        }
    }

    private fun buildCharHints(): Map<Char, Set<String>> =
        mapOf(
            // Nordic
            'ø' to setOf("nob", "nno", "dan"),
            'å' to setOf("nob", "nno", "dan", "swe"),
            'æ' to setOf("nob", "nno", "dan", "isl"),
            'þ' to setOf("isl"),
            'ð' to setOf("isl"),
            // Germanic
            'ä' to setOf("swe", "ger", "fin"),
            'ö' to setOf("swe", "ger", "fin", "tur", "hun", "isl"),
            'ü' to setOf("ger", "tur", "hun"),
            'ß' to setOf("ger"),
            // Romance
            'ñ' to setOf("spa"),
            'ã' to setOf("por"),
            'õ' to setOf("por"),
            'ç' to setOf("fre", "por", "tur", "cat"),
            // Slavic/Baltic
            'ł' to setOf("pol"),
            'ř' to setOf("cze"),
            'ů' to setOf("cze"),
            'ě' to setOf("cze"),
            'ľ' to setOf("slo"),
            'ė' to setOf("lit"),
            // Other
            'ő' to setOf("hun"),
            'ű' to setOf("hun"),
            'ğ' to setOf("tur"),
            'ı' to setOf("tur"),
            'ă' to setOf("rum", "vie"),
            'ș' to setOf("rum"),
            'ț' to setOf("rum"),
            'đ' to setOf("vie", "hrv"),
        )

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
            "rus" to
                setOf(
                    "\u0438", "\u0432", "\u043d\u0435", "\u043d\u0430", "\u043e\u043d",
                    "\u0447\u0442\u043e", "\u0441", "\u043a\u0430\u043a", "\u044d\u0442\u043e",
                    "\u043f\u043e", "\u043d\u043e", "\u0437\u0430", "\u0435\u0433\u043e",
                    "\u043e\u043d\u0430", "\u043e\u043d\u0438", "\u043c\u044b",
                    "\u0442\u0430\u043a", "\u0434\u0430", "\u0436\u0435",
                    "\u043e\u0442", "\u043a", "\u043e", "\u0438\u0437", "\u0443",
                    "\u0431\u044b\u043b", "\u0431\u044b\u043b\u0430",
                    "\u0431\u044b\u043b\u043e", "\u0435\u0441\u0442\u044c",
                    "\u0435\u0449\u0435", "\u0443\u0436\u0435",
                    "\u043c\u043e\u0436\u043d\u043e", "\u043c\u043e\u0436\u0435\u0442",
                    "\u0442\u043e\u043b\u044c\u043a\u043e", "\u043e\u0447\u0435\u043d\u044c",
                ),
            "ukr" to
                setOf(
                    "\u0456", "\u043d\u0430", "\u0432", "\u0443", "\u0437",
                    "\u0442\u0430", "\u0449\u043e", "\u043d\u0435", "\u044f\u043a",
                    "\u0430\u043b\u0435", "\u0446\u0435", "\u0432\u0456\u043d",
                    "\u0432\u043e\u043d\u0430", "\u0432\u043e\u043d\u0438",
                    "\u043c\u0438", "\u0442\u0430\u043a", "\u0434\u043e",
                    "\u043f\u043e", "\u0437\u0430", "\u043f\u0440\u043e",
                    "\u0431\u0443\u0432", "\u0431\u0443\u043b\u0430",
                    "\u0431\u0443\u043b\u043e", "\u0454",
                    "\u0449\u0435", "\u0432\u0436\u0435",
                    "\u043c\u043e\u0436\u043d\u0430", "\u043c\u043e\u0436\u0435",
                    "\u0442\u0456\u043b\u044c\u043a\u0438", "\u0434\u0443\u0436\u0435",
                    "\u0439\u043e\u0433\u043e", "\u0457\u0457",
                ),
            "srp" to
                setOf(
                    "\u0438", "\u0458\u0435", "\u0443", "\u0434\u0430", "\u0441\u0435",
                    "\u043d\u0430", "\u0437\u0430", "\u043d\u0435", "\u043a\u0430\u043e",
                    "\u0442\u043e", "\u0441\u0430", "\u0430\u043b\u0438",
                    "\u043e\u0434", "\u043f\u043e", "\u043e\u0432\u043e",
                    "\u043e\u0432\u0430\u0458", "\u043a\u043e\u0458\u0438",
                    "\u043a\u043e\u0458\u0430", "\u043a\u043e\u0458\u0435",
                    "\u0438\u043b\u0438", "\u0442\u0430\u043a\u043e",
                    "\u0431\u0438\u043e", "\u0431\u0438\u043b\u0430",
                    "\u045a\u0435\u0433\u043e\u0432",
                    "\u045a\u0438\u0445\u043e\u0432",
                    "\u0441\u0430\u043c\u043e", "\u0438\u043f\u0430\u043a",
                    "\u0458\u043e\u0448", "\u0432\u0440\u043b\u043e",
                    "\u0438\u043c\u0430", "\u043d\u0438\u0458\u0435",
                    "\u0441\u043c\u043e",
                ),
            "ara" to
                setOf(
                    "\u0641\u064a", "\u0645\u0646", "\u0639\u0644\u0649",
                    "\u0625\u0644\u0649", "\u0647\u0630\u0627", "\u0647\u0630\u0647",
                    "\u0627\u0644\u0630\u064a", "\u0627\u0644\u062a\u064a",
                    "\u0644\u0627", "\u0648", "\u0623\u0646", "\u0643\u0627\u0646",
                    "\u0643\u0627\u0646\u062a", "\u0642\u062f", "\u0639\u0646",
                    "\u0623\u0648", "\u0644\u0643\u0646", "\u062b\u0645",
                    "\u0628\u0639\u062f", "\u0642\u0628\u0644",
                    "\u0628\u064a\u0646", "\u0643\u0644",
                    "\u0630\u0644\u0643", "\u0647\u0646\u0627\u0643",
                    "\u0623\u064a\u0636\u0627", "\u0641\u0642\u0637",
                    "\u062d\u062a\u0649", "\u0645\u0639", "\u0644\u0647",
                    "\u0644\u0647\u0627", "\u0644\u0647\u0645",
                    "\u0643\u0627\u0646\u0648\u0627",
                ),
            "arm" to
                setOf(
                    "\u0587", "\u0567", "\u0561\u0575\u0564",
                    "\u056B\u0576", "\u0578\u0580", "\u0565\u0576",
                    "\u0561\u0575\u0576", "\u0574\u0565\u056F",
                    "\u0561\u0575\u057D", "\u0576\u0561\u0587",
                    "\u0562\u0561\u0575\u0581", "\u0561\u0575\u056C",
                    "\u056F\u0561\u0574", "\u0565\u0574",
                    "\u0565\u057D", "\u0565\u0576\u0584",
                    "\u0576\u0580\u0561\u0576\u0584",
                    "\u0576\u0580\u0561\u0576\u0581",
                    "\u056B\u0576\u0579", "\u0564\u0565\u057A\u056B",
                    "\u0564\u0565\u057A", "\u0574\u0565\u057B",
                    "\u057E\u0580\u0561", "\u0570\u0565\u057F",
                    "\u0574\u056B\u0561\u0575\u0576",
                    "\u0574\u056B\u0561\u056F",
                    "\u0561\u0575\u0564\u057A\u0565\u057D",
                    "\u056B\u0576\u0579\u057A\u0565\u057D",
                    "\u0565\u0569\u0565", "\u057D\u0561\u056F\u0561\u0575\u0576",
                    "\u0574\u056B\u0577\u057F",
                ),
            "vie" to
                setOf(
                    "c\u1ee7a", "v\u00e0", "l\u00e0", "c\u00f3",
                    "\u0111\u01b0\u1ee3c", "nh\u01b0ng", "cho",
                    "kh\u00f4ng", "c\u00e1c", "m\u1ed9t",
                    "nh\u1eefng", "\u0111\u00e3", "c\u0169ng",
                    "v\u1edbi", "trong", "tr\u00ean",
                    "b\u1edfi", "r\u1ea5t", "n\u00e0y",
                    "\u0111\u00f3", "t\u1ea1i", "t\u1eeb",
                    "theo", "khi", "n\u0103m",
                    "\u0111\u1ebfn", "ho\u1eb7c", "c\u00f2n",
                    "\u0111ang", "ra", "l\u1ea1i",
                    "v\u1ec1", "th\u00ec", "do",
                ),
            "ceb" to
                setOf(
                    "ang", "sa", "nga", "ug", "og", "ni", "mga", "kay",
                    "kini", "niya", "iya", "na", "kung", "kang",
                    "dili", "wala", "aduna", "mao", "kaniya",
                    "nila", "sila", "kami", "kita",
                    "pinaagi", "usab", "lang", "gikan",
                    "tungod", "alang", "uban", "apan",
                    "sulod", "taas", "ubos",
                ),
            "sme" to
                setOf(
                    "ja", "lea", "leat", "ahte", "go", "mii", "dat",
                    "son", "sii", "ii", "eai", "eat", "dien",
                    "dan", "maid", "ain", "nu", "vel",
                    "dego", "muhto", "dahje", "vai",
                    "buot", "ollu", "hui",
                    "gos", "goas", "das", "dasa",
                    "oidnot", "oaidnit", "lohkat",
                    "bargat", "boahtit",
                ),
            "yor" to
                setOf(
                    "n\u00ed", "s\u00ed", "t\u00ed", "\u00e0ti",
                    "f\u00fan", "p\u1eb9\u0300l\u00fa",
                    "k\u00ed", "k\u00f3", "j\u1eb9\u0301",
                    "l\u1eb9\u0301", "n\u00e1\u00e0",
                    "gb\u1ecdg\u1ecdb\u1ecdo", "s\u00e8", "fi",
                    "w\u00e0", "t\u00e0b\u00ed",
                    "b\u00ed", "r\u1eb9\u0300",
                    "ti", "\u00e0w\u1ecdn",
                    "\u1eb9ni", "l\u00e1ti", "\u1ecdr\u1ecd\u0300",
                    "nkan", "p\u00e0t\u00e0k\u00ec",
                    "b\u00e1k\u00e0nn\u00e0",
                    "s\u00edw\u00e1j\u00fa",
                    "\u00edb\u00e1t\u00e0n",
                    "l\u00f3r\u00ed", "n\u00edpa",
                ),
        )
}
