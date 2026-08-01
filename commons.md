# commons — baza wiedzy (single file)

**homeportal.commons** — współdzielona biblioteka Maven (`pl.homeportal:homeportal-commons:6.0`,
packaging `pom`, Java 8) narzędzi dla platformy Homeportal; **biblioteka, nie aplikacja** (brak `main`) —
budowana, instalowana do lokalnego repo i konsumowana przez pozostałe repa homixa. Uwaga: **buduje się
wyłącznie na JDK 8** (Lombok 1.16.14 pada na JDK 16+), a konsumenci budują na JDK 17 wobec gotowego jara.

Ten plik jest jedynym źródłem wiedzy o repo — obowiązuje też agentów AI pracujących w tym katalogu.

## Build & test

```bash
mvn clean install                                    # wszystkie moduły + testy
mvn -pl homeportal-commons-data -am clean install    # jeden moduł wraz z zależnościami
mvn -pl homeportal-commons-java test -Dtest=StringUtilsTest           # pojedyncza klasa testowa
mvn -pl homeportal-commons-java test -Dtest=StringUtilsTest#someCase  # pojedynczy test
mvn deploy -Dmaven.test.skip=true                    # deploy artefaktów (testy pominięte)
```

Lokalnie build wymaga JDK 8: `JAVA_HOME=$(/usr/libexec/java_home -v 1.8) mvn clean install`.
Skille: `mb` (build), `md` (deploy), `mbd` (build + deploy). Testy: JUnit 4 + Hamcrest.

CI: `.github/workflows/build.yml` uruchamia `mvn verify` na każdy push i pull request (JDK 8).
Publikacja jest osobnym, ręcznym workflow (`publish.yml`) — GitHub Packages nie pozwala nadpisać
wydanej wersji, więc **każda zmiana wymaga podbicia `<version>` przed publikacją**.

## Układ modułów i kolejność zależności

Do agregatora należy tych sześć modułów (deklaracja w root `pom.xml`); buduj i edytuj w tej kolejności:

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

> Katalogi `homeportal-commons-geo-api` i `homeportal-commons-location-api` **nie** są w root `<modules>`
> i mają innego parenta (`pl.homeportal-platform`). `mvn install` ich nie buduje — pomijaj je, chyba że
> pracujesz nad nimi wprost.

## Konwencje

- **Nie usuwaj zależności na podstawie samego skanu importów.** Część z nich jest potrzebna wyłącznie
  w runtime i nie ma ani jednego `import`: `javax.el-api` + `glassfish javax.el` (interpolacja komunikatów
  Hibernate Validatora), `hibernate-entitymanager` (provider JPA), H2 w testach (ładowany przez
  `persistence.xml`). `mvn dependency:analyze` zgłasza je jako nieużywane — to fałszywy alarm.
- **Wersje zależności są scentralizowane.** Wszystkie wersje żyją w root `pom.xml` w `<dependencyManagement>`.
  POM-y modułów deklarują zależności *bez* `<version>`. Wersje artefaktów `spring-*` i `spring-data-*`
  pochodzą z zaimportowanych BOM-ów — nie pinuj ich pojedynczo.
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
globalne `mb/mbd/md/itest` są tam symlinkowane do `~/.claude/skills`, a `install.js` je synchronizuje.
Pełny model scope/origin/wersji i lista repów homixa: `homeportal.hac/hac.md` §11.
