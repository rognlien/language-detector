import com.github.rognlien.LanguageDetector
import spock.lang.Specification

class LanguageDetectorTest extends Specification {
    
    def "Detect languages" () {
        expect:
            def detected = LanguageDetector.detect(text)
            
            detected == expected

            println("Detected language: $detected")
        
        where:
            expected    | text
            "nno"       | "Eg bråvaknar av ein underleg lyd rett utanfor huset. Kva i alle dagar var det der? Eg gnir litt søvn ut av auga og kikar meg rundt. Ein høg melodi tutar i veg med korte støytar der ute. Kva skjer? Så kjem eg på at det er noko med denne dagen som eg skulle hugse. Men kva? Har eg bursdag?"
            "eng"       | "The quick brown fox jumps over the lazy dog late on a friday afternoon"
            "nob"       | "Dette er en historie om de tre små fisk, som endte sine dager i en fiskehandlers disk."
    }
}
