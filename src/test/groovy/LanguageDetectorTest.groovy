import com.github.rognlien.LanguageDetector
import com.github.rognlien.LanguageProfile
import com.github.rognlien.LanguageProfileCodec
import com.github.rognlien.NgramExtractor
import spock.lang.Specification

class LanguageDetectorTest extends Specification {

    def "Detect languages"() {
        expect:
            LanguageDetector.detect(text) == expected

        where:
            expected | text
            "eng"    | "The quick brown fox jumps over the lazy dog late on a friday afternoon"
            "fre"    | "Le petit chat dort tranquillement sur le canapé pendant que la pluie tombe doucement dehors."
            "ger"    | "Der schnelle braune Fuchs springt über den faulen Hund an einem späten Freitagnachmittag."
            "spa"    | "El rápido zorro marrón salta sobre el perro perezoso en una tarde tranquila de viernes."
            "ita"    | "La volpe marrone veloce salta sopra il cane pigro in un tranquillo pomeriggio di venerdì."
            "nob"    | "Jeg husker ikke nøyaktig når det skjedde, men det var en gang jeg gikk gjennom skogen og plutselig hørte noe bak meg. Det var ingenting der, bare stille trær og mye løv på bakken. Jeg fortsatte å gå videre uten å tenke mer over det."
            "nno"    | "Eg veit ikkje kvifor det vart slik, men det hende noko underleg den kvelden. Nokon hadde eigenleg sagt at det ikkje skulle vere mogleg, men eg såg det sjølv."
            "rum"    | "Vulpea maro rapidă sare peste câinele leneș într-o după-amiază liniștită de vineri. Este o zi frumoasă pentru a te plimba prin parc și a te bucura de natură."
            "dut"    | "De snelle bruine vos springt over de luie hond op een rustige vrijdagmiddag. Het was een prachtige dag om buiten te zijn en te genieten van het mooie weer."
            "por"    | "O gato pequeno dorme tranquilamente no sofá enquanto a chuva cai suavemente lá fora. Era uma noite perfeita para ficar em casa e ler um bom livro."
            "cat"    | "El gat petit dorm tranquil·lament al sofà mentre la pluja cau suaument a fora. Era una nit perfecta per quedar-se a casa i llegir un bon llibre."
            "pol"    | "Szybki brązowy lis przeskakuje przez płot na podwórku późnym piątkowym popołudniem. To był wspaniały dzień na spacer po parku i cieszenie się przyrodą."
            "tur"    | "Hızlı kahverengi tilki tembel köpeğin üzerinden atlıyor. Bugün hava çok güzel ve parkta yürüyüş yapmak için harika bir gün."
            "fin"    | "Nopea ruskea kettu hyppää laiskan koiran yli myöhään perjantai-iltapäivänä. Oli ihana päivä olla ulkona ja nauttia kauniista säästä. Aurinko paistoi kirkkaasti ja tuuli puhalsi kevyesti puiden latvoja pitkin. Tällaisia päiviä ei tule kovin usein tähän aikaan vuodesta."
            "hun"    | "A fejlesztések érdemben segítik az országos lefedettség elérését, ezzel teszik kényelmesebben használhatóvá és vonzóbbá a tiszta és csendes villanyautókat. A fejlesztések eredményeképpen a felvonók fülkéit, aknai acélszerkezeteit, a tornyokat immár kizárólag a saját gyártósorukon készítik el."
            "swe"    | "Även Moderaterna, med finansminister Elisabeth Svantesson i spetsen, har börjat öppna upp för en möjlighet att ta på sig spenderarbyxorna. Det handlar om att Sveriges ekonomi behöver stärkas och att arbetsmarknaden måste förbättras avsevärt under de kommande åren."
            "dan"    | "Den hurtige brune ræv springer over den dovne hund sent fredag eftermiddag. Det var en dejlig dag at gå en tur i parken og nyde det gode vejr. Solen skinnede klart og vinden blæste let gennem trætoppene. Sådanne dage kommer ikke særlig ofte på denne tid af året."
            "cze"    | "Rychlá hnědá liška přeskočí líného psa pozdě v pátek odpoledne. Byl to nádherný den na procházku v parku a užívání si přírody."
            "slo"    | "Rýchla hnedá líška preskočí lenivého psa neskoro v piatok popoludní. Bol to nádherný deň na prechádzku v parku a užívanie si prírody."
            "hrv"    | "Brza smeđa lisica preskače lijenog psa kasno u petak poslijepodne. Bio je to divan dan za šetnju kroz park i uživanje u prirodi."
            "lit"    | "Greita ruda lapė peršoka per tingų šunį vėlyvą penktadienio popietę. Tai buvo nuostabi diena pasivaikščioti parke ir mėgautis gamta. Saulė švietė ryškiai ir vėjas lengvai pūtė per medžių viršūnes. Tokios dienos retai pasitaiko šiuo metų laiku."
            "ind"    | "Rubah cokelat cepat melompati anjing malas pada suatu sore hari Jumat yang tenang. Hari itu sangat indah untuk berjalan-jalan di taman."
            "afr"    | "Die vinnige bruin jakkals spring oor die lui hond op 'n rustige Vrydagmiddag. Dit was 'n pragtige dag om buite te wees en die mooi weer te geniet."
            "rus"    | "Быстрая коричневая лиса перепрыгнула через ленивую собаку поздним пятничным вечером. Это был прекрасный день для прогулки по парку и наслаждения природой."
            "ukr"    | "Швидка коричнева лисиця перестрибнула через лінивого собаку пізно ввечері у п'ятницю. Це був чудовий день для прогулянки парком та насолоди природою. Сонце яскраво світило і вітер легенько дув крізь верхівки дерев. Такі дні трапляються нечасто у цю пору року."
            "srp"    | "Брза смеђа лисица прескочи преко лењог пса касно у петак поподне. Био је то диван дан за шетњу кроз парк и уживање у природи."
            "ara"    | "القطة الصغيرة تنام بهدوء على الأريكة بينما المطر يتساقط بلطف في الخارج. كانت ليلة هادئة وجميلة في هذا الوقت من السنة."
            "arm"    | "Բացի բևեռացումից, որը բնութագրում է քիմիական կապը ստատիկ վիճակում, յուրաքանչյուր կապ կարող է օժտվել նաև բևեռայնությամբ, այսինքն՝ արտաքին էլեկտրամագնիսական դաշտի ազդեցության տակ բևեռացումը փոփոխելու հատկությամբ։"
            "chi"    | "小猫安静地睡在沙发上，外面的雨轻轻地下着。这是一个美丽的夜晚，适合在家里读一本好书。窗外的世界安静而祥和。"
            "jpn"    | "今日は天気がとても良いです。公園に散歩に行きたいと思います。桜の花が美しく咲いています。"
            "vie"    | "Con mèo nhỏ ngủ yên trên ghế sofa trong khi mưa rơi nhẹ nhàng bên ngoài. Đó là một đêm yên tĩnh và đẹp đẽ vào thời điểm này trong năm."
            "isl"    | "Þá hefði komið fram að ríkisstjórnin áformaði að hefjast handa við fyrningu aflaheimilda. Flóðið ruddi sér leið úr lóninu við Gígjökul og niður farveg Markarfljóts."
            "ceb"    | "Abe Laguna, nga nailhan pinaagi sa iyang stage ngalan Ookay, mao ang usa ka American nga mga electronic dance music producer, DJ ug mga musikero."
            "sme"    | "Abderalaš Demokritos dahjege Demokrit riegádii sullii Abderas ja jámii sullii. Son lei Greikka filosofa ja gulai atomismiii."
            "yor"    | "Aami aisan ti o wọpọ fun ọna-ọfun to ndun ni ti kokoro ti a ko le f'oju lasan ri nfa ni ọna-ọfun ti o ndun ni."
            "lat"    | "Ab Aevo antiquo super lacum navigatio ad commeatum onerarium, piscationem et peregrinationem adhibita est. Servi manumissi officia praesidiorum plerumque faciebant."
    }

    def "Detect returns null for empty input"() {
        expect:
            LanguageDetector.detect("") == null
    }

    def "Detect returns null for whitespace-only input"() {
        expect:
            LanguageDetector.detect("   ") == null
    }

    def "Detect returns null for non-letter input"() {
        expect:
            LanguageDetector.detect("12345 !@#\$%") == null
    }

    def "detectAll returns ranked results"() {
        when:
            def results = LanguageDetector.detectAll("The quick brown fox jumps over the lazy dog")

        then:
            !results.isEmpty()
            results[0].language == "eng"
            results.size() > 1
            results[0].score >= results[1].score
    }

    def "detectAll returns empty list for empty input"() {
        expect:
            LanguageDetector.detectAll("").isEmpty()
    }

    def "Scores are the profile frequencies weighted by IDF, exactly as the profile files state them"() {
        given: "the profiles read straight from the files"
            List<LanguageProfile> profiles = profileFiles().collect { LanguageProfileCodec.read(it.newInputStream()) }
            Map<String, Integer> documentFrequency = [:]
            profiles.each { profile -> profile.ngrams.keySet().each { documentFrequency[it] = (documentFrequency[it] ?: 0) + 1 } }

        and: "the input n-gram frequencies, as the extractor yields them"
            def text = "Jeg husker ikke nøyaktig når det skjedde, men det var en gang jeg gikk gjennom skogen"
            Map<String, Integer> counts = [:]
            NgramExtractor.extract(text, NgramExtractor.DEFAULT_NGRAM_RANGE).each { counts[it] = (counts[it] ?: 0) + 1 }
            double total = counts.values().sum()

        when:
            Map<String, Double> scores = LanguageDetector.detectAll(text).collectEntries { [it.language, it.score] }

        then: "every language scores what the plain formula gives"
            scores.size() == profiles.size()
            profiles.every { profile -> Math.abs(scores[profile.language] - plainScore(profile, counts, total, documentFrequency, profiles.size())) < 1e-12 }
    }

    private static double plainScore(LanguageProfile profile, Map<String, Integer> counts, double total,
                                     Map<String, Integer> documentFrequency, int languages) {
        double score = 0.0d
        counts.each { ngram, count ->
            Integer df = documentFrequency[ngram]
            Double weight = profile.ngrams[ngram]
            if (df != null && weight != null) {
                score += (count / total) * weight * Math.log(languages / (double) df)
            }
        }
        score
    }

    def "Preload leaves detection as it was"() {
        given:
            def before = LanguageDetector.detectAll("The quick brown fox jumps over the lazy dog")

        when:
            LanguageDetector.preload()

        then:
            LanguageDetector.detectAll("The quick brown fox jumps over the lazy dog") == before
    }

    private static List<File> profileFiles() {
        def dir = new File(LanguageDetectorTest.classLoader.getResource("profiles").toURI())
        dir.listFiles().findAll { it.name.endsWith(".bin") }.sort { it.name }
    }
}
