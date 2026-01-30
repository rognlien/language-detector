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
