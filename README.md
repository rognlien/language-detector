# language-detector

A small, dependency-free language detector for the JVM, written in Kotlin.
It identifies 36 languages and is tuned for short texts such as book titles,
subtitles and descriptions, as well as ordinary prose.

```kotlin
CombinedDetector.detect("Jeg husker ikke nøyaktig når det skjedde") // "nob"
CombinedDetector.detect("Der Prozess und die Verwandlung")          // "ger"
```

## How it works

Two detectors run side by side, and `CombinedDetector` blends their scores.

- **`LanguageDetector`** compares the character n-grams of the text, from two
  to five characters long, with a profile for each language. Each n-gram is
  weighted by how few languages share it, so distinctive sequences count most.
- **`StopwordDetector`** looks for common function words such as "the", "und"
  and "og", and for characters typical of a few languages, such as "ø", "ß"
  and "ł".

N-grams work well on longer text, and stopwords carry short titles where there
are too few n-grams to be reliable. For short texts made only of basic Latin
letters and no known stopwords, `CombinedDetector` leans towards English.

## Installation

The library is published to GitHub Packages. GitHub requires authentication
even for public packages, so you need a personal access token with the
`read:packages` scope.

```kotlin
// build.gradle.kts
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/rognlien/language-detector")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("com.github.rognlien:language-detector:0.1.27")
}
```

Every push to `main` publishes a new version numbered `0.1.<build>`. The
library needs Java 21 or later.

## Usage

### Kotlin

```kotlin
import com.github.rognlien.CombinedDetector

fun main() {
    // Load the profiles at startup. Otherwise the first call pays for it.
    CombinedDetector.preload()

    val language: String? = CombinedDetector.detect("Le Petit Prince") // "fre"

    // All candidates, best first
    CombinedDetector.detectAll("Det er en god dag")
        .take(3)
        .forEach { println("${it.language} ${"%.2f".format(it.score)}") }
    // nob 1.00
    // dan 0.99
    // nno 0.66
}
```

### Java

```java
import com.github.rognlien.CombinedDetector;
import com.github.rognlien.DetectionResult;

import java.util.List;

public class Example {
    public static void main(String[] args) {
        CombinedDetector.preload();

        String language = CombinedDetector.detect("Il nome della rosa"); // "ita"

        List<DetectionResult> results = CombinedDetector.detectAll("Det er en god dag");
        DetectionResult best = results.get(0);
        System.out.println(best.getLanguage() + " " + best.getScore()); // nob 1.0
    }
}
```

### Results

- `detect` returns the best language code, or `null` when the text has no
  letters, for example `""` or `"12345"`.
- `detectAll` returns every candidate as a `DetectionResult` with a `language`
  and a `score`, sorted from best to worst, or an empty list.
- Scores are relative within one call and are not probabilities. A close
  second place, as with Bokmål and Danish above, means the text is ambiguous.

### Weighting the two detectors

`detectAll` takes optional weights for the n-gram and stopword scores. Both
default to 0.5.

```kotlin
CombinedDetector.detectAll("Le Petit Prince", ngramWeight = 0.3, stopwordWeight = 0.7)
```

From Java, pass the weights as the second and third arguments. The two
detectors can also be used on their own through `LanguageDetector` and
`StopwordDetector`, which have the same `detect` and `detectAll` methods.

### Threads and startup

All detectors are thread-safe singletons. Loading the profiles takes a few
hundred milliseconds. Call `CombinedDetector.preload()` while your
application starts, so the first request does not wait for it.

## Languages

Codes are ISO 639-2 bibliographic codes, the scheme used by ONIX code list 74
for book metadata. Icelandic is the exception and uses `isl`.

| Code | Language | Code | Language |
|---|---|---|---|
| afr | Afrikaans | jpn | Japanese |
| ara | Arabic | lat | Latin |
| arm | Armenian | lit | Lithuanian |
| cat | Catalan | nno | Norwegian Nynorsk |
| ceb | Cebuano | nob | Norwegian Bokmål |
| chi | Chinese | pol | Polish |
| cze | Czech | por | Portuguese |
| dan | Danish | rum | Romanian |
| dut | Dutch | rus | Russian |
| eng | English | slo | Slovak |
| fin | Finnish | sme | Northern Sami |
| fre | French | spa | Spanish |
| ger | German | srp | Serbian |
| hrv | Croatian | swe | Swedish |
| hun | Hungarian | tur | Turkish |
| ind | Indonesian | ukr | Ukrainian |
| isl | Icelandic | vie | Vietnamese |
| ita | Italian | yor | Yoruba |

## Limitations

- **Unsupported languages are not reported as unknown.** Text in a language
  outside the list gets the closest supported language, or an empty result
  for scripts no profile covers, such as Korean, Hebrew and Thai.
- **Any basic Latin text leans English** when it is short and has no
  stopwords. This includes names and random letters.
- **Mixed-language text** returns one ranking for the whole text, not one
  language per part.
- **Very short phrases in closely related languages** can be confused, for
  example Russian, Serbian and Ukrainian in Cyrillic, or Bokmål and Danish.

## Building language profiles

The profiles in `src/main/resources/profiles` are built by the
`profile-builder` subproject from a directory of plain text files, with one
subdirectory per language named by its ISO 639-3 code, such as `nob/` or
`fra/`.

```sh
./gradlew :profile-builder:run --args="training-data profiles-out [language]"
```

## Development

```sh
./gradlew build         # compile and run the tests
./gradlew ktlintFormat  # format before committing
```

## License

Apache License 2.0. See [LICENSE](LICENSE) and [NOTICE](NOTICE).
