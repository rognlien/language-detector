import com.github.rognlien.NgramExtractor
import kotlin.ranges.IntRange
import spock.lang.Specification

class NgramExtractorTest extends Specification {

    def "Extract bigrams and trigrams from text"() {
        given:
            def text = "The quick brown fox"

        when:
            def ngrams = NgramExtractor.extract(text, new IntRange(2, 3))

        then:
            ngrams.contains("th")
            ngrams.contains("qu")
            ngrams.contains("the")
            ngrams.contains("qui")
            !ngrams.contains("The")
    }

    def "Extract includes cross-word boundary n-grams"() {
        given:
            def text = "ab cd"

        when:
            def ngrams = NgramExtractor.extract(text, new IntRange(2, 3))

        then:
            ngrams.contains(" a")
            ngrams.contains("b ")
            ngrams.contains(" c")
            ngrams.contains("d ")
            ngrams.contains(" ab")
            ngrams.contains("b c")
            ngrams.contains(" cd")
            ngrams.contains("cd ")
    }

    def "Extract returns empty list for empty input"() {
        expect:
            NgramExtractor.extract("", new IntRange(2, 3)).isEmpty()
    }

    def "Extract returns empty list for non-letter input"() {
        expect:
            NgramExtractor.extract("123 !@#", new IntRange(2, 3)).isEmpty()
    }

    def "Extract lowercases input"() {
        when:
            def ngrams = NgramExtractor.extract("ABC", new IntRange(2, 2))

        then:
            ngrams.contains("ab")
            ngrams.contains("bc")
            !ngrams.contains("AB")
    }

    def "Extract handles unicode letters"() {
        when:
            def ngrams = NgramExtractor.extract("café", new IntRange(2, 2))

        then:
            ngrams.contains("af")
            ngrams.contains("fé")
    }
}
