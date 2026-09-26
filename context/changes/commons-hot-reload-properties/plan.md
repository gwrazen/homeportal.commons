# Przeładowanie wybranych properties bez restartu — plan implementacji

## Overview

Wspólny mechanizm w commons (`@Reloadable` + `PropertiesReloader`) sprawdza datę modyfikacji pliku
konfiguracyjnego i podmienia w działającej aplikacji wyłącznie wartości oznaczone jako
przeładowywalne. Pierwszy odbiorca to portal: wyłączniki schedulerów, limit dobiegu wyceny,
`css.version` i `geo.blocking.mode`. Hac, hop i importer dochodzą osobnymi fazami w przyszłości,
na tym samym mechanizmie.

## Current State Analysis

- Wszystkie cztery aplikacje czytają conf identycznie: `@PropertySource("file:${HOMEPORTAL_<APP>_HOME}/conf/homeportal.<app>.properties")`
  na `@Component @Getter ApplicationConfiguration`, każda wartość przez `@Value("${key:default}")` na prywatnym,
  niefinalnym polu (portal: `homeportal-portal-configuration/.../ApplicationConfiguration.java:28`, 161 pól).
- `@Value` poza klasami `ApplicationConfiguration` nie występuje nigdzie; wyjątkiem są `@Scheduled(cron = "${...}")`, których nie da się przeładować.
- Konsumenci w większości pytają getter przy każdym użyciu — podmiana pola wystarczy:
  `ValuationScheduler.java:110` (limit dobiegu), `isEnabled()` wszystkich schedulerów, `ResourceVersionInterceptor.java:41` (`css.version`).
- `GeoBlockService.initialize()` (`@PostConstruct`, `:127-139`) kopiuje kraje/wyjątki/domeny crawlerów
  do pól i **pomija `readAreas()` przy `mode = off`** — przełączenie off→block w locie zostawiłoby puste
  zakresy IP i nie blokowało nikogo. `readAreas()` przypisuje `starts`, `ends`, `countries` trzema
  osobnymi instrukcjami (`:359-361`).
- Commons nie ma żadnego pomocnika JMX ani przeładowania; najbliższy jest `reflection.ClassFieldReader`.
  Wersja 7.0 jest opublikowana (GitHub Packages nie nadpisze), konsumenci deklarują `...:7.0`
  w `build.gradle.kts`. CI commons (`.github/workflows/build.yml`) wymaga **dokładnie 139 testów**.
- Hac i importer nie skanują `pl.homeportal.commons` — klasa w commons musi być zwykłą klasą, nie `@Component`.

## Desired End State

Na produkcji portalu edycja pliku `conf/homeportal.portal.properties` (np. `scheduler.offer.valuations.partial.limit = 50 → 60`)
w ciągu ~5 min zmienia zachowanie aplikacji bez restartu, a w `application.log` stoi linia z kluczem, starą i nową wartością.
Zmiana klucza spoza białej listy daje w logu ostrzeżenie „wymaga restartu” zamiast cichego rozjazdu plik ↔ pamięć.
Operacja JMX `HomeportalPortalManagement:type=ConfigurationManager#reload` robi to samo natychmiast i zwraca raport.

### Key Discoveries:

- Wzorzec podmiany pól przez refleksję już jest w testach HAC-a (`ApplicationConfigurationLabelThresholdsTest`).
- MBeany: `@Component @ManagedResource(objectName = "HomeportalPortalManagement:type=<X>Manager")` z `@ManagedOperation`,
  w `homeportal-portal-management/.../management/mbean/<obszar>/`; eksport przez `@EnableMBeanExport` w `acontextspring`.
- Pułapka na przyszłość (hac): pola `valuation*` wchodzą do `ValuationAxes.signature()` liczonej raz na JVM — nie mogą dostać `@Reloadable`.

## What We're NOT Doing

- hac, hop, importer — osobne fazy/tickety po sprawdzeniu mechanizmu na portalu.
- `known-bots.txt` i inne pliki obok confa (`blocked-areas.txt`, `robots.txt`) — osobny krok.
- `cron`, pule wątków, TTL-e cache Guavy, `HtmlCacheFilter` — wartości zamrożone w konstruktorach, zostają na restart.
- Spring Cloud Config, `@RefreshScope`, przepisywanie `ApplicationConfiguration` na dynamiczne gettery.
- Unieważnianie `html_cache` po zmianie `css.version` — nowa wartość obowiązuje dla nowo renderowanych stron; stare wpisy jak dziś (`/invalid`, `/warmup`).

## Implementation Approach

Deklaratywnie: pole z `@Reloadable` obok `@Value` jest jedynym źródłem decyzji „wolno w locie”.
`PropertiesReloader` czyta klucz i wartość domyślną z samej adnotacji `@Value` (ten sam napis, który
wstrzyknął wartość przy starcie), więc nie powstaje drugi spis kluczy. Wyzwalacze są dwa i wołają to samo:
okresowe sprawdzenie daty pliku (co 5 min) i operacja JMX.

## Critical Implementation Details

- **Zapis w trakcie:** plik zmodyfikowany mniej niż ~2 s temu pomijamy w tym przebiegu (edytor/`scp` może pisać go jeszcze); datę zapamiętujemy dopiero po udanym odczycie.
- **Widoczność między wątkami:** pola z `@Reloadable` w portalu dostają `volatile`; `reload()` jest `synchronized` (timer i JMX mogą wejść naraz).
- **Geo — spójność tablic:** `starts`/`ends`/`countries` muszą być podmieniane jednym przypisaniem (jeden obiekt z trzema tablicami w polu `volatile`), inaczej żądanie w trakcie przeładowania trafi binarnym wyszukiwaniem w tablice różnej długości.
- **Placeholdery:** wartość w pliku może zawierać `${...}` — w pierwszej wersji takie klucze odrzucamy z powodem w raporcie, zamiast rozwiązywać je inaczej niż Spring.

## Phase 1: commons 7.0 — `@Reloadable` i `PropertiesReloader`

### Overview

Mechanizm bez zależności od aplikacji, z testami i publikacją w wersji 7.0 (bez podbijania — decyzja usera 26.09).

### Changes Required:

#### 1. Adnotacja

**File**: `homeportal-commons-java/src/main/java/pl/homeportal/commons/configuration/Reloadable.java`

**Intent**: znacznik pola konfiguracji, które wolno podmienić w działającej aplikacji.

**Contract**: `@Retention(RUNTIME) @Target(FIELD) public @interface Reloadable`.

#### 2. Przeładowanie

**File**: `homeportal-commons-java/src/main/java/pl/homeportal/commons/configuration/PropertiesReloader.java` (+ `ReloadReport.java`)

**Intent**: czyta plik ponownie, porównuje z bieżącymi polami i podmienia tylko te z `@Reloadable`; raportuje, co zrobił.

**Contract**:
- `new PropertiesReloader(Object configuration, Path file)` — przy tworzeniu zapamiętuje datę modyfikacji i migawkę wszystkich kluczy pliku.
- `ReloadReport reloadIfModified()` — nic nie robi (pusty raport), gdy data się nie zmieniła albo zmiana jest młodsza niż ~2 s.
- `ReloadReport reload()` — przeładowanie bezwarunkowe (dla JMX).
- `void addListener(Consumer<ReloadReport> listener)` — wołany po przeładowaniu, które cokolwiek zmieniło; wyjątek słuchacza logowany, nie przerywa pozostałych.
- Klucz i wartość domyślna z `@Value("${key:default}")` pola; brak klucza w pliku → wartość domyślna.
- Konwersja: `String`, `boolean/Boolean`, `int/Integer`, `long/Long`, `double/Double`; błąd konwersji → klucz w `rejected`, pole bez zmian.
- `ReloadReport`: `changed` (klucz, stara, nowa), `rejected` (klucz, wartość, powód), `requiresRestart` (klucze bez `@Reloadable`, których wartość w pliku różni się od migawki startowej); `toString()` czytelny dla operatora JMX.
- Logowanie: każda zmiana `INFO`, każde odrzucenie i każdy klucz „wymaga restartu” `WARN` (raz na zmianę, nie co przebieg).

#### 3. Testy, CI, wersja

**File**: `homeportal-commons-java/src/test/java/pl/homeportal/commons/configuration/PropertiesReloaderTest.java`, `.github/workflows/build.yml`, `build.gradle.kts`

**Intent**: pokryć zachowanie, podbić bramkę liczby testów i wersję.

**Contract**: wersja zostaje `7.0`; bramka CI = 139 + liczba nowych testów.

### Success Criteria:

#### Automated Verification:

- `./gradlew build` w commons przechodzi, nowe testy zielone
- bramka liczby testów w CI zgodna z nową liczbą
- `./gradlew publishToMavenLocal` publikuje 7.0

#### Manual Verification:

- raport `toString()` czytelny na przykładowym przeładowaniu z testu

**Implementation Note**: po fazie 1 zatrzymaj się na potwierdzenie przed zmianami w portalu.

---

## Phase 2: portal — proste pokrętła, timer i JMX

### Overview

Portal na commons 7.0 (z mechanizmem), białe pola oznaczone, sprawdzanie co 5 min i operacja JMX.

### Changes Required:

#### 1. Wersja commons

**File**: wszystkie `homeportal.portal/**/build.gradle.kts` z `pl.homeportal:homeportal-commons-*:7.0`

**Intent**: wersja zostaje 7.0 — portal bierze nowy jar z `mavenLocal()`; nic do podbijania w `build.gradle.kts`.

#### 2. Oznaczenie pól

**File**: `homeportal.portal/homeportal-portal-configuration/.../ApplicationConfiguration.java`

**Intent**: `@Reloadable` + `volatile` na: `scheduler.generic.enabled`, wszystkich `scheduler.*.enabled`
(w tym `scheduler.indexer.valuation.enabled`), `scheduler.offer.valuations.partial.limit`, `css.version`.
Przed oznaczeniem każdego sprawdzić, że konsument czyta getter przy użyciu, a nie kopiuje wartości.

**Contract**: nowe, **nieprzeładowywalne** klucze `configuration.reload.enabled` (domyślnie `true`)
i `configuration.reload.interval.seconds` (domyślnie `300`); getter ścieżki pliku confa wyliczonej jak w `checkConfigurationLoaded()`.

#### 3. MBean i timer

**File**: `homeportal.portal/homeportal-portal-management/.../management/mbean/configuration/ConfigurationManager.java`

**Intent**: trzyma `PropertiesReloader`, co interwał woła `reloadIfModified()`, wystawia operację ręczną.

**Contract**: `@ManagedResource(objectName = "HomeportalPortalManagement:type=ConfigurationManager")`;
`@ManagedOperation String reload()` zwraca `ReloadReport.toString()`; `@Scheduled(fixedDelayString = "${configuration.reload.interval.seconds:300}000")`
(albo równoważnie) respektujący `configuration.reload.enabled`. Reloader tworzony `@Bean`-em lub w konstruktorze — nie `@Component` w commons.
Nie dziedziczy po `AbstractScheduler` (1 440 linii started/ended na dobę bez wartości).

#### 4. Conf

**File**: `homeportal.portal/homeportal-portal-application/dist/conf/homeportal.portal.properties`

**Intent**: sekcja `configuration` z dwoma nowymi kluczami (zasada: każdy czytany klucz jest w pliku). Na prodzie dopisać przy wdrożeniu.

### Success Criteria:

#### Automated Verification:

- `./gradlew build` portalu przechodzi (testy slice bez zmian)
- test jednostkowy `ConfigurationManager`: zmiana pliku → zmiana gettera `ApplicationConfiguration`

#### Manual Verification:

- lokalnie: zmiana `scheduler.offer.valuations.partial.limit` w confie → po ≤ 5 min linia w logu i nowy limit w następnym przebiegu dobiegu
- lokalnie: zmiana klucza spoza listy (np. `gemini.model.name`) → `WARN` „wymaga restartu”
- JMX `reload()` zwraca raport

**Implementation Note**: po fazie 2 zatrzymaj się na potwierdzenie.

---

## Phase 3: portal — `geo.blocking.mode` w locie

### Overview

Słuchacz przeładowania w `GeoBlockService` odtwarza stan z `initialize()`.

### Changes Required:

#### 1. Geo

**File**: `homeportal.portal/homeportal-portal-service/.../service/GeoBlockService.java`

**Intent**: wydzielić z `initialize()` metodę odtwarzającą kraje, wyjątki, domeny i obszary; wołać ją ze słuchacza,
gdy raport zawiera klucz `geo.*`; zakresy IP podmieniać jednym obiektem.

**Contract**: `@Reloadable` na `geo.blocking.mode`, `geo.blocked.countries`, `geo.exempt.ips`, `geo.crawler.domains`;
`ConfigurationManager` rejestruje słuchacza (albo `GeoBlockService` rejestruje się sam przez wstrzyknięty reloader — wybrać to,
co nie tworzy zależności service → management).

### Success Criteria:

#### Automated Verification:

- test: przełączenie `off → block` przez przeładowanie wczytuje obszary (`countryOf()` zwraca kraj)
- `./gradlew build` portalu przechodzi

#### Manual Verification:

- lokalnie: `geo.blocking.mode = off → log` w pliku, po ≤ 5 min linie `[GEO]` w logu bez restartu

## Testing Strategy

### Unit Tests:

- reloader: brak zmiany daty → pusty raport; zmiana pola `@Reloadable` → podmiana i wpis w `changed`
- zmiana klucza bez `@Reloadable` → `requiresRestart`, pole nietknięte
- błędna liczba → `rejected`, stara wartość zostaje
- klucz usunięty z pliku → wartość domyślna z `@Value`
- plik świeżo zmodyfikowany (< 2 s) → pominięty w tym przebiegu
- wyjątek słuchacza nie przerywa pozostałych

### Manual Testing Steps:

1. Lokalnie `/launch`, zmienić limit dobiegu w confie, obserwować log.
2. `jconsole`/`jmxterm` → `ConfigurationManager.reload()`.
3. Po wdrożeniu na prod: zmiana limitu 50 → 51 i powrót, sprawdzić `/scheduls` (licznik dobiegu) i log.

## Migration Notes

Wdrożenie: najpierw commons 7.0 z mechanizmem (`publishToMavenLocal`), potem portal. Na prodzie dopisać dwa klucze `configuration.*`
przed restartem. Wycofanie: `configuration.reload.enabled = false` + restart albo powrót do poprzedniego jara.

## References

- Ticket: `context/changes/commons-hot-reload-properties/change.md`
- Wzorzec MBean: `homeportal.portal/homeportal-portal-management/.../mbean/promotion/PromotionManager.java:148-157`
- Geo: `homeportal.portal/homeportal-portal-service/.../GeoBlockService.java:127-139, 314-365`

## Progress

> Convention: `- [ ]` pending, `- [x]` done. Append ` — <commit sha>` when a step lands. Do not rename step titles.

### Phase 1: commons 7.0 — `@Reloadable` i `PropertiesReloader`

#### Automated

- [x] 1.1 `./gradlew build` w commons przechodzi, nowe testy zielone — f58640f
- [x] 1.2 bramka liczby testów w CI zgodna z nową liczbą — f58640f
- [x] 1.3 `./gradlew publishToMavenLocal` publikuje 7.0 — f58640f

#### Manual

- [x] 1.4 raport `toString()` czytelny na przykładowym przeładowaniu z testu — f58640f

### Phase 2: portal — proste pokrętła, timer i JMX

#### Automated

- [x] 2.1 `./gradlew build` portalu przechodzi (testy slice bez zmian) — portal 25c304566
- [x] 2.2 test jednostkowy `ConfigurationManager`: zmiana pliku → zmiana gettera `ApplicationConfiguration` — portal 25c304566

#### Manual

- [x] 2.3 lokalnie: zmiana limitu dobiegu → po ≤ 5 min linia w logu i nowy limit w następnym przebiegu — portal 25c304566
- [x] 2.4 lokalnie: zmiana klucza spoza listy → `WARN` „wymaga restartu” — portal 25c304566
- [x] 2.5 JMX `reload()` zwraca raport — portal 25c304566

### Phase 3: portal — `geo.blocking.mode` w locie

#### Automated

- [ ] 3.1 test: przełączenie `off → block` przez przeładowanie wczytuje obszary
- [ ] 3.2 `./gradlew build` portalu przechodzi

#### Manual

- [ ] 3.3 lokalnie: `geo.blocking.mode = off → log`, po ≤ 5 min linie `[GEO]` bez restartu
