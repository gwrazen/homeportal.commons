# commons — baza wiedzy (single file)

**homeportal.commons** — współdzielona biblioteka (`pl.homeportal:homeportal-commons:7.0`, Java 17)
narzędzi dla platformy Homeportal; **biblioteka, nie aplikacja** (brak `main`) — budowana, publikowana
do GitHub Packages i konsumowana przez pozostałe repa homixa.

⚠️ **To repo prowadzi dwie linie na dwóch gałęziach i ten plik opisuje gałąź `jdk17`:**

| gałąź | wersja | JDK | build | konsumenci |
|---|---|---|---|---|
| `jdk17` (ta) | **7.0** | 17 | **Gradle** | portal, hac, importer |
| `master` | 6.0 | 8 | Maven | hop |

Merge `jdk17` → `master` jest **zakazany** (decyzja z ticketu `commons-jdk17-migration`): konsument
na Javie 8 nie odczyta bajtkodu 17. Poprawki do linii 6.x robi się na `masterze`. Wersja jednej linii
nie mówi nic o drugiej.

Ten plik jest jedynym źródłem wiedzy o repo — obowiązuje też agentów AI pracujących w tym katalogu.

## Build & test

```bash
./gradlew build                                      # wszystkie moduły + testy
./gradlew :homeportal-commons-data:build             # jeden moduł wraz z zależnościami
./gradlew :homeportal-commons-java:test --tests StringUtilsTest            # pojedyncza klasa testowa
./gradlew :homeportal-commons-java:test --tests 'StringUtilsTest.someCase' # pojedynczy test
./gradlew publishAllPublicationsToStagingRepository -PpublishRepoUrl=file:///tmp/commons-staging
```

Build wymaga JDK 17: `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew build`.
Zawsze przez **wrapper** (`./gradlew`) — systemowego `gradle` nie ma i nie jest potrzebny, wersja
jest przypięta w `gradle/wrapper/`. Skille: `gb` (build), `gd` (publish), `gbd` (build + publish).
Mavenowe `mb`/`md`/`mbd` **nie działają w tym repo od migracji** — pomów tu nie ma.
Testy: JUnit 4 + Hamcrest (`useJUnit()` w buildzie).

⚠️ **`BUILD SUCCESSFUL` nie dowodzi, że testy się wykonały.** Gradle kończy się zielono także przy
zerze uruchomionych testów — przy JUnit 4 wystarczy brak `useJUnit()`. Liczbę czytaj z raportów
(`build/test-results/test/TEST-*.xml`); ma wynosić **139**, w rozkładzie 59 / 15 / 59 / 4 / 2 / 0
(java / domain / data / logging / mail / test). CI ma na to twardą bramkę.

CI: `.github/workflows/build.yml` uruchamia `./gradlew build` na każdy push i pull request (JDK 17),
waliduje wrapper i **przerywa build, gdy liczba testów ≠ 139**. Publikacja jest osobnym, ręcznym
workflow (`publish.yml`) — GitHub Packages nie pozwala nadpisać wydanej wersji, więc **każda zmiana
wymaga podbicia `version` w `build.gradle.kts` przed publikacją**. Poświadczenia idą przez zmienne
środowiskowe; Gradle nie czyta `~/.m2/settings.xml`.

## Układ modułów i kolejność zależności

Do buildu należy tych sześć modułów (deklaracja w `settings.gradle.kts`); buduj i edytuj w tej kolejności:

1. **homeportal-commons-java** — fundament, bez zależności wewnętrznych. Narzędzia w `pl.homeportal.commons.*`:
   text, datetime, file, image, zip, json, security, validation, reflection, i18n, exceptions, scheduler,
   helpery MVC, aspekty AOP.
2. **homeportal-commons-domain** — model domenowy **bez ORM, Lucene i Springa**: `Product`, `Market`,
   `Activity`, system cech (`Feature`, `FeatureType`, `FeatureConverter`, `FeatureTypeProvider`),
   interfejsy znacznikowe typów nieruchomości i kontrakt `Identifiable`. Jedyna zależność: commons-lang3.
   Konsument potrzebujący samych typów domenowych (np. `hac` na stosie jakarta) bierze ten moduł zamiast `-data`.
   **Nie dokładaj tu zależności** — to jedyny powód istnienia modułu.
3. **homeportal-commons-data** — zależy od `-java` i `-domain`. Persystencja JPA/Hibernate Search,
   warstwa full-text search, paginacja.
4. **homeportal-commons-logging** — zależy od `-java` i `-domain` (celowo nie od `-data`). Ustandaryzowane
   helpery logowania encji.
5. **homeportal-commons-mail** — zależy od `-java` i `-logging`. Maile szablonowane Velocity.
6. **homeportal-commons-test** — helpery testowe Spring MVC.

> Katalogi `homeportal-commons-geo-api` i `homeportal-commons-location-api` **nie** są w `settings.gradle.kts`
> i mają własne, mavenowe pomy z obcym parentem (`pl.homeportal-platform`). Build ich nie dotyka —
> pomijaj je, chyba że pracujesz nad nimi wprost.

## Konwencje

- **Nie usuwaj zależności na podstawie samego skanu importów.** Część z nich jest potrzebna wyłącznie
  w runtime i nie ma ani jednego `import`: `javax.el-api` + `glassfish javax.el` (interpolacja komunikatów
  Hibernate Validatora), `hibernate-entitymanager` (provider JPA), H2 w testach (ładowany przez
  `persistence.xml`). `mvn dependency:analyze` zgłasza je jako nieużywane — to fałszywy alarm.
- **Wersje zależności są scentralizowane.** Wszystkie żyją w root `build.gradle.kts` jako `constraints`;
  buildy modułów deklarują zależności *bez* wersji. Wersje `spring-*` i `spring-data-*` pochodzą
  z dwóch platform (`spring-framework-bom`, `spring-data-releasetrain`) — nie pinuj ich pojedynczo.
  Trzy wpisy (`javassist`, `commons-io`, `commons-compress`) nie są przez nikogo deklarowane wprost —
  **przykrywają wersje tranzytywne** i ich usunięcie wywala się dopiero w runtime u konsumenta
  (javassist 3.18 nie działa na JDK 17).
- **`api`, nie `implementation`.** Zależności widoczne dla konsumenta muszą iść przez `api`, bo
  `implementation` publikuje je w pomie ze scope'em `runtime` — a portal, hop i importer kompilują się
  przeciw klasom `-domain`, którego **nie deklarują wprost**.
- **`provided` ma osobną konfigurację.** Gradle nie ma odpowiednika mavenowego `provided`: `compileOnly`
  w ogóle nie trafia do metadanych. Konfiguracja `provided` w root buildzie wchodzi w `compileOnly`
  i `testImplementation`, a do pomu jest dopisywana przez `pom.withXml` z jawnym scope'em.
- **Lombok** (`@Getter`/`@Setter`/`@NoArgsConstructor` itd.) w encjach i DTO.
- Cały kod produkcyjny pod pakietem `pl.homeportal.commons`.
- Wyjątki: hierarchia `Homeportal*Exception` (`HomeportalServiceException`, `HomeportalValidationException`,
  `HomeportalSecurityException`) zamiast surowych `RuntimeException`.

## Architektura

**AOP przez adnotacje (`-java`).** Zachowania przekrojowe są sterowane własnymi adnotacjami sparowanymi
ze springowym `@Component @Aspect`. Żeby to działało w aplikacji konsumenta, klasa aspektu musi być beanem,
a proxy AspectJ włączone:
- `@ExecutionTime` → `ExecutionTimeAspect` loguje czas wykonania metody.
- `@ModelAttributeCondition` → `ModelAttributeConditionAspect` warunkowo pomija metody `@ModelAttribute`
  w zależności od URI bieżącego żądania.

**Full-text search (`-data`).** Kodowanie wartości ma **jedno źródło prawdy**: pakiet `search/encoding`
(`ValueEncoders.TEXT / FEATURE / NUMERIC / DATE`). Bridge'y Hibernate Search są cienkimi adapterami na te
encodery, a `SearchQuery` używa encodera zadeklarowanego przy `QueryParameter.encoder()`. Dzięki temu
strona indeksu i strona zapytania nie mogą się rozjechać — czego pilnuje `EncodingContractTest`.
Dodając nowy parametr wyszukiwania, zadeklaruj encoder odpowiadający bridge'owi użytemu na polu encji.

**Persystencja (`-data`).**
- `AbstractEntity<IDENTITY extends Number>` — bazowy `@MappedSuperclass` dla wszystkich encji
  (generowane `@Id`, plus `isPersisted`/`isTransient`).
- `FullTextRepository<T>` / `FullTextRepositoryImpl<T extends AbstractEntity>` opakowują
  **Hibernate Search + Lucene**. `SearchQuery` + `SearchQueryBuilder` + `QueryParameter` budują zapytania;
  pakiet `search/bridge` zawiera `FieldBridge`'e (`FeatureBridge`, `NumericBridge`, `DateBridge`,
  `PropertyTypeBridge`) mapujące wartości domenowe do indeksu Lucene.
- Pakiet `pageable` (`Page`, `PageItems`, `PageItem`) — własna abstrakcja paginacji. `Page` **implementuje**
  `Pageable` i jest **1-based** (nazwy jego pól to nazwy parametrów HTTP w publicznym API hop-a);
  konwersja na 0-based żyje wyłącznie w `Page.toPageable()`.
- Model domenowy (`model/`) mieszka w module `-domain`, ale zachowuje pakiety `pl.homeportal.commons.data.model.*`
  — importy konsumentów są niezmienione.

**Logowanie (`-logging`).** `LoggingSupport` to statyczny helper z ustandaryzowanymi szablonami komunikatów
(`INFORMATION_SAVE`, `ERROR_DELETE`, ...) do spójnego logowania operacji CRUD na encjach przez SLF4J.
Przy logowaniu operacji na encjach używaj tych helperów zamiast doraźnych stringów.

**Mail (`-mail`).** `Notifier`/`NotifierAdapter` wysyłają `VelocityEmail` renderowane z `EmailTemplate`
przez Apache Velocity + commons-email; `BaseDTO` jest bazą modelu szablonu. Nieudana wysyłka rzuca
`HomeportalServiceException` — nie jest logowana jako sukces.

## Workflow 10x

Katalog `context/` (`foundation/`, `changes/`, `archive/`) leży w korzeniu repo — commons nie ma modułu
aplikacyjnego. Bieżące zmiany: `context/changes/<change-id>/`.

## Skille AI (rejestr)

Skille AI homixa mają wspólne, wersjonowane źródło — sibling repo **`homeportal.ai.registry`**;
globalne `gb/gd/gbd` (Gradle), `mb/mbd/md/itest` (Maven) są tam symlinkowane do `~/.claude/skills`,
a `install.js` je synchronizuje. W tym repo działają wyłącznie gradle'owe.
Pełny model scope/origin/wersji i lista repów homixa: `homeportal.hac/hac.md` §11.
