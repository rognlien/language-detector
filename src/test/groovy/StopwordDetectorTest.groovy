import com.github.rognlien.StopwordDetector
import spock.lang.Specification

class StopwordDetectorTest extends Specification {

    def "Detect languages from short book titles"() {
        expect:
            StopwordDetector.detect(text) == expected

        where:
            expected | text
            "eng"    | "The Art of War"
            "fre"    | "Le Petit Prince"
            "ger"    | "Die Verwandlung"
            "spa"    | "El amor en los tiempos"
            "ita"    | "Il nome della rosa"
            "por"    | "O Alquimista"
            "dut"    | "Het achterhuis"
            "nob"    | "Jeg er ikke redd"
            "nno"    | "Eg ikkje veit"
            "swe"    | "Och detta att vara"
            "dan"    | "Jeg blev ikke af kun meget"
            "cat"    | "Els pilars de la terra"
            "pol"    | "Nie jest tylko"
            "rum"    | "Nu este pentru"
            "tur"    | "Bu bir ve"
            "fin"    | "Ei ollut vain ja"
            "hun"    | "Az egy nem volt"
            "cze"    | "Jsou jako jeho"
            "slo"    | "Bol ako alebo"
            "hrv"    | "Bio kao ali"
            "lit"    | "Buvo kaip arba"
            "ind"    | "Yang ini tidak untuk"
            "afr"    | "Die vir ons nie"
            "isl"    | "Ekki og hann"
            "lat"    | "Est quod atque enim"
            "rus"    | "\u041d\u0435 \u0431\u044b\u043b\u043e \u0435\u0449\u0435 \u0442\u043e\u043b\u044c\u043a\u043e"
            "ukr"    | "\u0429\u043e \u0431\u0443\u043b\u043e \u0442\u0456\u043b\u044c\u043a\u0438 \u0432\u0436\u0435"
            "srp"    | "\u0411\u0438\u043e \u043a\u0430\u043e \u0430\u043b\u0438 \u0458\u043e\u0448"
            "ara"    | "\u0641\u064a \u0647\u0630\u0627 \u0639\u0644\u0649 \u0623\u0646"
            "arm"    | "\u0587 \u0561\u0575\u0564 \u0574\u0565\u056f \u0562\u0561\u0575\u0581"
            "vie"    | "C\u1ee7a v\u00e0 l\u00e0 kh\u00f4ng"
            "ceb"    | "Ang mga sa nga"
            "sme"    | "Lea leat ahte mii"
            "yor"    | "N\u00ed s\u00ed t\u00ed f\u00fan"
    }

    def "Detect returns null for empty input"() {
        expect:
            StopwordDetector.detect("") == null
    }

    def "Detect returns null for whitespace-only input"() {
        expect:
            StopwordDetector.detect("   ") == null
    }

    def "Detect returns null for no matching stopwords"() {
        expect:
            StopwordDetector.detect("xyz qqq zzz") == null
    }

    def "detectAll returns ranked results"() {
        when:
            def results = StopwordDetector.detectAll("This is not a test but the real deal")

        then:
            !results.isEmpty()
            results[0].language == "eng"
            results.size() >= 1
            (results.size() == 1) || (results[0].score >= results[1].score)
    }

    def "detectAll returns empty list for empty input"() {
        expect:
            StopwordDetector.detectAll("").isEmpty()
    }

    def "Detection is case insensitive"() {
        expect:
            StopwordDetector.detect("THE ART OF WAR") == "eng"
            StopwordDetector.detect("LE PETIT PRINCE") == "fre"
    }
}
