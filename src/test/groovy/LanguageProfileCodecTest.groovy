import com.github.rognlien.LanguageProfile
import com.github.rognlien.LanguageProfileCodec
import spock.lang.Specification

class LanguageProfileCodecTest extends Specification {

    def "Write and read"() {
        given:
            def profile = new LanguageProfile("nob", [
                "er": 0.0231f,
                "en": 0.0207f,
                "de": 0.0142f,
                "et": 0.0126f,
                "te": 0.0120f,
                "re": 0.0109f,
                "an": 0.0086f
            ])
            def output = new ByteArrayOutputStream()

        when:
            LanguageProfileCodec.write(profile, output)

        and:
            def read = LanguageProfileCodec.read(new ByteArrayInputStream(output.toByteArray()))

        then:
            with(read) {
                language == "nob"
                ngrams == profile.ngrams
            }

    }
}
