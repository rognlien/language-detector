import com.github.rognlien.NgramExtractor
import kotlin.ranges.IntRange
import spock.lang.Specification

class NgramExtractorTest extends Specification {
    
    def "Extract" () {
        given:
            def text = "The quick brown fox"
        
        when:
            def ngrams = NgramExtractor.extract(text, new IntRange(2,3))
        
        then:
            println(ngrams)
    }
}
