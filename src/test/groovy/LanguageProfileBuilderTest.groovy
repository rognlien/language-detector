import com.github.rognlien.LanguageProfileBuilder
import spock.lang.Specification

class LanguageProfileBuilderTest extends Specification {

    def "Appending a text with a weight equals appending it that many times"() {
        given:
            def weighted = new LanguageProfileBuilder("nob")
            def repeated = new LanguageProfileBuilder("nob")

        when:
            weighted.append("det er en god dag", 3)
            weighted.append("hei")
            3.times { repeated.append("det er en god dag") }
            repeated.append("hei")

        then:
            weighted.build(LanguageProfileBuilder.DEFAULT_MAX_NGRAMS).ngrams == repeated.build(LanguageProfileBuilder.DEFAULT_MAX_NGRAMS).ngrams
    }

    def "Normalised frequencies sum to one"() {
        given:
            def builder = new LanguageProfileBuilder("eng")
            builder.append("the quick brown fox", 7)
            builder.append("jumps over the lazy dog")

        expect:
            Math.abs(builder.build(LanguageProfileBuilder.DEFAULT_MAX_NGRAMS).ngrams.values().sum() - 1.0d) < 1e-9
    }

    def "Counts beyond the range of an int do not overflow"() {
        given:
            def builder = new LanguageProfileBuilder("nob")
            builder.append("og", 3_000_000_000L)
            builder.append("ikke", 1_000_000_000L)

        when:
            def ngrams = builder.build(LanguageProfileBuilder.DEFAULT_MAX_NGRAMS).ngrams

        then:
            ngrams.values().every { it > 0.0d }
            ngrams[" o"] > ngrams[" i"]
    }

    def "The language is kept"() {
        expect:
            new LanguageProfileBuilder("nno").build(LanguageProfileBuilder.DEFAULT_MAX_NGRAMS).language == "nno"
    }

    def "A weight that is not positive is rejected"() {
        when:
            new LanguageProfileBuilder("nob").append("hei", weight)

        then:
            thrown(IllegalArgumentException)

        where:
            weight << [0L, -1L]
    }
}
