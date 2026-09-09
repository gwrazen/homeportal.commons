---
change_id: commons-gradle-migration
title: Przejście commons z Mavena na Gradle'a — biblioteka publikowana do GitHub Packages, nie wdrażana
status: implementing
created: 2026-08-29
updated: 2026-09-09
archived_at: null
---

## Notes

przejscie na gradle'a

Zgłoszone przez usera 2026-08-29 dla całego homixa, prowadzone jako osobny ticket w każdym repo.
Bliźniaki: `hp-gradle-migration`, `hac-gradle-migration`, `hop-gradle-migration`,
`importer-gradle-migration`, `spy-gradle-migration`.

## Stan wyjściowy (zmierzony 2026-08-29)

6 modułów (`java`, `domain`, `data`, `mail`, `logging`, `test`), **JDK 8**, wersja **6.0**.

## ⚠️ Tu problem jest inny niż w pozostałych pięciu

Tamte repozytoria mają `wagon-maven-plugin` i wysyłają jara po FTP na serwer. **Commons nie ma
wagona w ogóle** — to biblioteka, nie aplikacja. Zamiast tego ma `distributionManagement`
wskazujące na **GitHub Packages** (`https://maven.pkg.github.com/gwrazen/homeportal.commons`).

Czyli migracja nie musi rozwiązywać problemu deployu po FTP, ale musi rozwiązać **publikowanie
z uwierzytelnieniem do GitHub Packages** — w Gradle to blok `publishing` plus poświadczenia,
których dziś Maven bierze z `settings.xml`.

## ⚠️ Commons jest zależnością wszystkich pięciu pozostałych repozytoriów

Każde z nich ciągnie `pl.homeportal:homeportal-commons-*` **przy stałej wersji**. Skutek: to repo
migruje się **pierwsze albo ostatnie, nigdy w środku** — a jeśli zmieni się sposób publikowania
artefaktu, pięć innych buildów przestanie go znajdować. Gradle konsumuje artefakty Mavena bez
problemu i odwrotnie, więc stan mieszany jest technicznie w porządku; ryzyko siedzi wyłącznie
w tym, **skąd i pod jaką współrzędną** artefakt jest pobierany.

## Dlaczego nie od razu plan

Zgłoszenie podaje rozwiązanie, nie problem. Zanim powstanie plan, trzeba odpowiedzieć, **co Gradle
ma naprawić** — tu tym bardziej, bo to najmniejszy build w homixie i bez kroków nietypowych.
Stąd `/10x-frame` przed `/10x-plan`.

## Uwaga o nazewnictwie

Prefiks `commons-` jest tu konwencją: tak nazywa się zarchiwizowany `commons-refactoring`
i tak nazywają się bliźniaki w pozostałych repach. Otwarty ticket migracyjny, który stał bez
prefiksu, został **przemianowany 2026-08-29 na `commons-jdk17-migration`**.

## Baseline fazy 1 (zmierzony 2026-09-09)

Pomiar wykonany **przed** jakąkolwiek zmianą w buildzie. Pliki referencyjne leżą w
`context/changes/commons-gradle-migration/baseline/`.

| co | wynik |
|---|---|
| `deps-portal.txt` | 1123 linie, 30 wpisów `homeportal-commons-*:7.0`, BUILD SUCCESS |
| `deps-hac.txt` | 993 linie, 18 wpisów, BUILD SUCCESS |
| `deps-importer.txt` | 1082 linie, 34 wpisy, BUILD SUCCESS |
| `jar-paths.txt` | sześć modułów; pliki 34 / 19 / 25 / 21 / 3 / 3, z tego `.class` **32 / 17 / 23 / 8 / 1 / 1** |
| szablony `mail/*.vm` | **11** |
| `-sources.jar` | 6 z 6 |
| `mvn -B -o clean test` (jdk17, JDK 17.0.7) | **BUILD SUCCESS**, **139 testów**: java 59 / domain 15 / data 59 / logging 4 / mail 2 / test 0 |
| wyciszenia | zero (`@Ignore`, `skipTests`, `<excludes>` — brak trafień) |

Zliczenie z kodu zgadza się z surefire co do modułu (27 klas / 139 metod).

⚠️ **Pułapka pomiarowa potwierdzona empirycznie**: w module `-java` `grep -rc '@Test'` daje **53**,
a `grep -rac` — **59**. `StringUtilsTest.java` ma znaki spoza ASCII, więc BSD grep uznaje go za plik
binarny i cicho pomija. Każde zliczanie testów w tym tickecie idzie przez `grep -a`.

### Stan repozytoriów w chwili pomiaru

| repo | gałąź | HEAD | drzewo |
|---|---|---|---|
| `portal` | `jdk17` | `7f50f9786` | czyste |
| `hac` | `commons-7-upgrade` | `0c71a1d` | czyste |
| `importer` | `jdk17` | `399fa0e` | **4 zmienione pliki `.class` w `target/`** (śledzone przez gita, sprzed tej sesji) |

Cztery pliki `.class` w importerze nie wpływają na `dependency:tree` — to artefakty builda, nie źródła
ani pomy. Odnotowane, bo baseline ma mówić prawdę o warunkach pomiaru.

### Uwaga o gałęzi

Robota idzie na `jdk17` (decyzja usera 2026-09-09). Dokumenty ticketu przyjechały tu z `mastera`
przez `cherry-pick` commita `d66bdda` (tutaj `47fbb6f`), więc **istnieją na obu gałęziach** —
na `masterze` już wypchnięte, tu żyją dalej razem z robotą. Pomiar testów wykonany na `jdk17`
w tymczasowym worktree, `mvn clean test` bez `install`, więc `~/.m2` pozostało nietknięte.

## Faza 2 — build Gradle bez publikacji (2026-09-09)

Gradle **9.7.1** (bieżąca stabilna), wrapper przypięty w repo, toolchain 17. Maven zostaje nietknięty
obok — oba buildy przechodzą.

| bramka | wynik |
|---|---|
| `./gradlew build` | BUILD SUCCESSFUL |
| testy | **139**: java 59 / domain 15 / data 59 / mail 2 / logging 4 / test 0 — zgodne z baseline co do modułu |
| wyciszenia | zero |
| bajtkod | `major version: 61` |
| klasy per moduł | 32 / 17 / 23 / 8 / 1 / 1 — zgodne |
| `mail/*.vm` | 11, na tych samych ścieżkach |
| `-sources.jar` | 6 z 6 (`withSourcesJar()`) |
| `mvn -B -o clean test` obok | BUILD SUCCESS, 139 testów |

### ⚠️ Lombok działa też w źródłach testowych

Pierwszy build padł na `PageItemsTest` (`package lombok does not exist`). W Mavenie scope `provided`
obejmuje **test classpath razem z przetwarzaniem adnotacji**; w Gradle `compileOnly` +
`annotationProcessor` dotyczą wyłącznie `main`. Potrzebna jest osobna para
`testCompileOnly` + `testAnnotationProcessor` — czego nie dało się wyczytać z pomów, bo tam
ta zależność jest niewidoczna.

### Odstępstwo przyjęte świadomie: brak `META-INF/maven/**`

Zbiory ścieżek w sześciu jarach różnią się od baseline'u **dokładnie o dwa wpisy na moduł**:
`META-INF/maven/pl.homeportal/<artifactId>/pom.xml` i `pom.properties`. Maven wkłada je
automatycznie, Gradle nie generuje ich w ogóle. Poza nimi zbiory są identyczne, zero wpisów
nadmiarowych po stronie Gradle'a.

Sprawdzone przed decyzją: `grep` po źródłach wszystkich pięciu repozytoriów daje **zero** trafień
na `META-INF/maven` i `pom.properties`, zero na `getImplementationVersion` i `Implementation-Version`.
Nikt w homixie tych metadanych nie czyta, a po fazie 6 pomy znikają z gałęzi, więc odtwarzanie ich
w jarze byłoby atrapą. **Kryterium 2.5 obowiązuje z pominięciem `META-INF/maven/**`** — decyzja
usera 2026-09-09.

### Wersje zależności

Moduły deklarują zależności **bez wersji**, tak jak w pomach. Wersje daje 30 `constraints`
w `build.gradle.kts` (lustro dzisiejszego `dependencyManagement`) plus dwie platformy:
`spring-framework-bom:5.2.9.RELEASE` i `spring-data-releasetrain:Moore-SR10`. Trzy nadpisania
tranzytywne (`javassist:3.29.2-GA`, `commons-io:2.6`, `commons-compress:1.0`) siedzą w tych samych
`constraints` — ich skutek u konsumenta weryfikuje faza 4.

## Faza 3 — metadane i publikacja do izolowanego stagingu (2026-09-09)

Publikacja do `/tmp/commons-staging` (parametr `-PpublishRepoUrl`), rejestr nietknięty,
`~/.m2` bez ani jednego pliku 7.1.

| bramka | wynik |
|---|---|
| komplet artefaktów | 6 × jar + `-sources.jar` + pom, wersja 7.1 |
| pliki `.module` | **0** (`GenerateModuleMetadata` wyłączone) |
| zbiór zależności i scope'y | zgodne z baseline'em we wszystkich sześciu modułach |
| wersje | zgodne co do numeru: Spring 5.2.9.RELEASE, Spring Data Moore-SR10, Hibernate 5.0.10.Final, Search 5.5.4.Final, Lucene 5.3.1, JAXB 2.3.1, xerces 2.12.2 |
| nadpisania tranzytywne | `javassist:3.29.2-GA`, `commons-io:2.6`, `commons-compress:1.0` — w DM **każdego** modułu |
| importy BOM | oba (`spring-framework-bom`, `spring-data-releasetrain`) w DM każdego modułu |
| wpisy `provided` | `-java`: servlet-api, lombok · `-data`: lombok · `-mail`: commons-email, velocity, lombok · `-test`: spring-webmvc, spring-test |

### Pom wyszedł wierniejszy, niż zakładał plan

Plan mówił o „płaskich pomach z `constraints`". Gradle zapisał **całe `dependencyManagement`** —
30 wpisów wersji plus **oba importy BOM ze `scope=import`** — do pomu każdego modułu. Mediacja wersji
jest więc odtworzona co do mechanizmu, tylko samodzielnie: bez `<parent>`, bez potrzeby ściągania
agregatora. Zależności w `<dependencies>` stoją bez wersji, dokładnie jak dziś.

### ⚠️ `provided` musi być jawne, inaczej znaczy `compile`

W opublikowanych pomach 7.0 `<scope>provided</scope>` widnieje **tylko** w `-mail` i `-test`.
Lombok i `servlet-api` nie mają tam scope'u wcale — ich „provided" pochodzi z `dependencyManagement`
rodzica. W pomie bez rodzica brak scope'u znaczy `compile`, więc lombok wjechałby konsumentom
na runtime classpath. Rozwiązane osobną konfiguracją `provided` w `build.gradle.kts`, która
wchodzi w `compileOnly` i `testImplementation`, a do pomu trafia przez `pom.withXml` z jawnym
scope'em i wersją rozwiązaną z compile classpath.

### Odstępstwo przyjęte świadomie: brak zależności testowych w pomie

Gradle nie publikuje `testImplementation` w ogóle, więc w nowych pomach nie ma `junit`,
`hamcrest-all`, `h2` ani `slf4j-simple` w `<dependencies>` (w `<dependencyManagement>` są).
Maven **nie propaguje scope'u `test` tranzytywnie**, więc do drzewa portalu, haca ani importera
te wpisy nigdy nie trafiały — skutek dla konsumenta zerowy. **Kryterium 3.3 obowiązuje dla
zależności widocznych dla konsumenta** (`compile` / `runtime` / `provided`) — decyzja usera 2026-09-09.

### Drobiazg wyłapany po drodze

`provided("org.projectlombok:lombok")` bez wersji nie rozwiązywał się, bo w lustrze `constraints`
brakowało wpisu na lomboka — w mavenowym `dependencyManagement` on jest. Dopisany.

## Faza 4 — bramka u trzech konsumentów (2026-09-09)

### Zmiana zakresu: commons zostaje na 7.0, nic nie wydajemy

Decyzja usera 2026-09-09, w trakcie fazy 4. Build deklaruje `version = 7.0`, a ticket kończy się
na zielonej bramce i CI — **bez publikacji do rejestru**. Powód: migracja narzędzia nie zmienia
zawartości biblioteki, więc nie ma czego wydawać ani czego podbijać u konsumentów. Numer pójdzie
w górę przy pierwszej realnej zmianie kodu.

Skutki dla planu: faza 5 traci publikację wersji kontrolnej (ścieżka Gradle → GitHub Packages
zostaje **niesprawdzona w boju**, świadomie), faza 6 traci wydanie i tag.

### Bramka zrobiona lepiej, niż zakładał plan: bez dotykania pomów konsumentów

Plan przewidywał tymczasową podmianę `homeportal.commons.version` i wpisanie repozytorium
stagingowego do pomu każdego konsumenta. Zamiast tego repozytorium podane jest przez **własny
`settings.xml`** (`-s`), a wersja się nie zmienia — więc **żaden pom konsumenta nie został
tknięty**. Izolacja: `localRepository` wskazuje na klon `~/.m2` zrobiony przez APFS
copy-on-write (53 s, zero dodatkowego miejsca), z którego usunięto commons 7.0, żeby rozwiązanie
**musiało** pójść ze stagingu. Potwierdzone: `_remote.repositories` w klonie mówi `commons-staging`.

### Wynik

| repo | build | testy | diff drzewa zależności |
|---|---|---|---|
| `portal` | BUILD SUCCESS | 1444 w 174 klasach | **pusty** (982 = 982 wpisy) |
| `hac` | BUILD SUCCESS | 372 w 60 klasach | **pusty** (862 = 862) |
| `importer` | BUILD SUCCESS | 28 w 9 klasach | **pusty** (971 = 971) |

Diff jest pusty **dosłownie**, nie „po odfiltrowaniu numeru wersji" — artefakt zbudowany Gradle'em
stoi pod tą samą współrzędną co mavenowy i rozwiązuje się identycznie. 372 testy haca zgadzają się
co do jednego z liczbą zapisaną przy migracji na JDK 17.

⚠️ `git status` w portalu, hacu i importerze: **zero zmian** po całej fazie (w importerze zostają
cztery pliki `.class` w `target/` sprzed tej sesji).

### Nadpisanie tranzytywne — udowodnione, nie założone

`./gradlew :homeportal-commons-data:dependencies --configuration runtimeClasspath` pokazuje
`org.javassist:javassist:3.18.1-GA -> 3.29.2-GA` i wpis `(c)` od constraintu. Bez tego kontekst EMF
padłby na JDK 17 w runtime, a kompilacja niczego by nie zgłosiła.

⚠️ Kryterium 4.4 („javassist u konsumenta to 3.29.2-GA") okazało się **bezprzedmiotowe**:
javassist nie występuje w drzewie żadnego z trzech konsumentów — ani przed podmianą, ani po.
U nich wygrywa własna linia Hibernate'a. Zweryfikowane więc na poziomie commons, gdzie ma znaczenie.

Kryterium 4.5 (velocity poza runtime classpath konsumenta): w portalu velocity jest w `compile`
i **przed, i po** — to jego własna zależność, nie tranzytywa z commons. Pusty diff jest tu mocniejszym
dowodem niż samo sprawdzenie scope'u.

### Pomy Mavena usunięte z gałęzi (przeniesione z fazy 6)

Siedem pomów i `gradlew.bat` usunięte na życzenie usera już teraz — skoro wydania nie ma, warunek
„po zielonym wydaniu" stracił sens. `./gradlew build` bez pomów: **139 testów**, zielono.
`master` nietknięty: dalej siedem pomów i wersja 6.0.

⚠️ Od tego commita `mb`, `md` i `mbd` **nie działają w tym repo**. Powstały odpowiedniki gradle'owe:
`/gb`, `/gd`, `/gbd` (rejestr `homeportal.ai.registry`).

### ⚠️ Uboczny skutek wyłapany i posprzątany: 7.1 w `~/.m2`

Przy pierwszym podejściu (jeszcze z podmienionym pomem portalu i wersją 7.1) w `~/.m2` wylądowały
**42 pliki commons 7.1**, mimo że buildy z konsoli szły do izolowanego klonu — `help:evaluate`
potwierdził `/tmp/m2-phase4`. Znacznik `_remote.repositories` datuje je na 22:55:34 i wskazuje
`commons-staging`, czyli najprawdopodobniej **IDE przeczytało w tle podmieniony pom i ściągnęło je
samo**. Sześć katalogów `7.1` usunięte, `7.0` nietknięte. To ten sam kształt awarii, który przy
migracji na JDK 17 położył lokalne buildy trzech repozytoriów — i argument za tym, żeby pomów
konsumentów nie ruszać w ogóle, co jest teraz stanem docelowym tej fazy.

## Faza 5 — CI na Gradle'a (2026-09-09)

Oba workflowy przepisane, przebieg **zielony** na gałęzi `jdk17`: `BUILD SUCCESSFUL in 54s`,
a bramka wypisała `Tests run: 139`. Przebieg `34405952456`, commit `9dce6e4`.

### Co się zmieniło w `build.yml`

- wrapper zamiast `mvn`, `cache: gradle`,
- **walidacja wrappera** (`gradle/actions/wrapper-validation`) — wrapper jar jest binarny i siedzi
  w repo, więc CI sprawdza, że to oryginalna dystrybucja Gradle'a,
- **twarda bramka liczby testów**: krok liczy `tests="…"` z raportów i przerywa build, gdy suma
  nie wynosi 139. To jest odpowiedź na pułapkę z fazy 2 — `BUILD SUCCESSFUL` nie dowodzi, że testy
  się wykonały,
- raporty testów wrzucane jako artefakt przebiegu.

### Co się zmieniło w `publish.yml`

`./gradlew publishAllPublicationsToGithubPackagesRepository`, poświadczenia ze **środowiska**
(`GITHUB_ACTOR` / `GITHUB_TOKEN`) — Gradle nie czyta `~/.m2/settings.xml`, więc `setup-java`
z `server-id` przestało cokolwiek dawać. Testy lecą przed publikacją, bo artefaktu z rejestru
nie da się cofnąć. Repozytorium `githubPackages` dodane w `build.gradle.kts` obok stagingowego.

### ⚠️ Ścieżka publikacji pozostaje niesprawdzona w boju

Plan przewidywał przećwiczenie uwierzytelnienia na wersji kontrolnej (`7.1-ci-check`). Po decyzji
„commons zostaje na 7.0, nic nie wydajemy" ten krok **wypadł** — kryteria 5.2 i 5.3 są
bezprzedmiotowe. Skutek: `publish.yml` jest napisany, ale **nigdy nie uruchomiony**; pierwsze
prawdziwe wydanie linii 7.x Gradle'em będzie zarazem pierwszym testem tej ścieżki. Świadomy koszt
decyzji o niewydawaniu — zapisany tutaj, żeby nikt się nie zdziwił.

Stan rejestru potwierdzony: `7.0`, `6.0`, `5.0` — nic pod `7.1`.

### Historia Actions

Ostatni przebieg zielony. Dwa czerwone w historii gałęzi są z 2026-09-08 i pochodzą z ticketu
`commons-jdk17-migration` (tag `v7.0` wskazywał commit sprzed poprawki CI) — nie z tej migracji.

## Faza 6 — domknięcie (2026-09-09)

Faza skurczyła się do dokumentacji: wydania nie ma (decyzja z fazy 4), a pomy i `gradlew.bat`
zniknęły już w fazie 4. Zostało uzgodnienie `commons.md` ze stanem faktycznym.

### `commons.md` przepisany na gałęzi `jdk17`

- **nagłówek**: 7.0 / Java 17 / Gradle, plus tabela dwóch linii i zapis, że merge do `mastera`
  jest zakazany — plik opisuje **tę** gałąź, `master` żyje inaczej (6.0 / JDK 8 / Maven),
- **Build & test**: komendy `./gradlew`, wymóg JDK 17, wrapper zamiast systemowego Gradle'a,
  skille `gb` / `gd` / `gbd` zamiast `mb` / `md` / `mbd` (te ostatnie **nie działają tu od migracji**),
- **ostrzeżenie o zielonym buildzie bez testów** wraz z liczbą bramkową 139 i rozkładem per moduł,
- **CI**: `./gradlew build`, walidacja wrappera, twarda bramka liczby testów,
- **Konwencje**: wersje w `constraints` zamiast `dependencyManagement`, trzy nadpisania tranzytywne
  i czym grozi ich usunięcie, `api` zamiast `implementation`, osobna konfiguracja `provided`,
- **moduły**: `settings.gradle.kts` zamiast root `<modules>`.

Komendy z dokumentu sprawdzone, nie przepisane z pamięci: `./gradlew :homeportal-commons-java:test
--tests StringUtilsTest` uruchamia 6 testów, `:homeportal-commons-data:build` przechodzi.

### Stan końcowy

| co | jdk17 | master |
|---|---|---|
| build | **Gradle 9.7.1** (wrapper) | Maven |
| wersja | 7.0 | 6.0 |
| JDK | 17 | 8 |
| pomy w buildzie | **0** | 7 |
| CI | `./gradlew build` + bramka 139 | `mvn verify` |

⚠️ Na `jdk17` zostają dwa pomy: `homeportal-commons-geo-api` i `homeportal-commons-location-api`.
To martwe moduły spoza buildu (obcy parent `pl.homeportal-platform`, ostatni commit 2020-06-29,
zero konsumentów) — świadomie poza zakresem tego ticketu, tak samo jak były poza reaktorem Mavena.

### Czego ten ticket NIE zrobił

- **Nie wydał niczego** — w rejestrze dalej 7.0 / 6.0 / 5.0, zbudowane Mavenem. Artefakt z Gradle'a
  istniał wyłącznie w katalogu stagingowym na dysku.
- **Nie sprawdził ścieżki publikacji w boju** — `publish.yml` jest napisany i nigdy nieuruchomiony.
  Pierwsze wydanie linii 7.x Gradle'em będzie zarazem pierwszym testem uwierzytelnienia
  do GitHub Packages.
- **Nie ruszył `mastera`** ani linii 6.0 — hop dalej konsumuje 6.0 zbudowane Mavenem.
- **Nie tknął pomów konsumentów** — portal, hac i importer mają je bit w bit takie jak przed fazą 4.
- **Nie naprawił skilli mavenowych** — `mb`/`md`/`mbd` przestały działać w tym repo; odpowiedniki
  `gb`/`gd`/`gbd` powstały w `homeportal.ai.registry`, ale poprawka samych mavenowych (żeby mówiły,
  że w commons ich nie ma) należy do osobnego ticketu.
