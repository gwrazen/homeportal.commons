# Commons 6.0 — refaktor modulow, bugfixy i naprawa full-text search

## Overview

Wydanie `pl.homeportal:homeportal-commons:6.0` obejmuje: naprawe kontraktu kodowania w warstwie full-text search (dzis czesc filtrow zwraca zero wynikow bez bledu), wszystkie potwierdzone bugfixy w piatce modulow — kazdy przypiety testem regresyjnym, wydzielenie bezzaleznosciowego modulu `commons-domain`, oraz fundament buildu i CI, bez ktorego reszta jest nieweryfikowalna. Zostajemy na Hibernate Search 5; migracja HS6/JDK17 to osobna zmiana `jdk17-migration`, dla ktorej ten plan przygotowuje grunt.

## Current State Analysis

Stan wejsciowy jest opisany w `context/changes/commons-refactoring/research.md`. Skrot tego, co ksztaltuje plan:

- **69 klas produkcyjnych, 10 testowych, ~14% pokrycia nominalnego (~12% realnego).** 5 z 28 metod testowych nie asertuje niczego (`DateFormatsTest.java:13-19`, `ObjectValidatorTest.java:25-30,40-45,55-62`, `LoggingSupportTest.java:12-18`). CI nie uruchamia testow w ogole — `.github/workflows/publish.yml:37` to `mvn -B -DskipTests deploy`.
- **Brak `project.build.sourceEncoding`** przy 24 plikach z polskimi znakami. Te same literaly (`Market.java:12` → `"wtórny"`) decyduja o dopasowaniu w Lucene — build na maszynie nie-UTF-8 cicho je psuje.
- **Trzy wersje Spring Framework w jednym buildzie**: `spring-context` 5.1.4 (`pom.xml:38`), `spring-test` 5.2.9 (`:47`), a `spring-data-jpa` 2.2.10 ciagnie `spring-core`/`beans`/`aop` w 5.2.9 na glebokosci 2, gdzie wygrywaja z pinowanym 5.1.4.
- **Kontrakt kodowania FTS nie istnieje jako artefakt.** Strona indeksu wybiera bridge adnotacja w encji downstream, strona zapytania koduje na sztywno (`search/SearchQuery.java:40,154-163`). Rozjazd daje ciche zero wynikow dla cech z akcentami i pol bez `PropertyTypeBridge`.
- **`FullTextRepository` wystawia `FullTextQuery` i `SortField[]`** (`repository/FullTextRepository.java:4,38`) i jest dziedziczony przez 17 repozytoriow downstream; `save`/`delete` koliduja sygnatura z `CrudRepository` i wygrywaja rozstrzygniecie fragmentu Spring Data.
- **`hac` uciekl z commons** — ma reczne kopie `OfferProduct`/`OfferActivity`/`OfferMarket`/`FeatureType` i wyklucza `commons-data` z drzewa `commons-logging` (`hac/pom.xml:87-98`), bo `-logging` ciagnie ORM+Lucene przez jeden import w `LoggingSupport.java:5`.

Wykonane juz w ramach tej zmiany: bump wersji 5.0 → 6.0 w root pom + 5 modulach, `homeportal-commons-mail/pom.xml:26` z zaszytego `5.0` na `${project.parent.version}`, aktualizacja `CLAUDE.md`. Praca idzie na branchu `commons-refactoring`.

## Desired End State

Po zakonczeniu planu:

1. `mvn clean install` przechodzi na czystym klonie, a GitHub Actions uruchamia `mvn verify` na kazdy push — testy przestaja byc opcjonalne.
2. Kazdy bug wymieniony w `research.md` jest naprawiony i przypiety testem, ktory bez poprawki nie przechodzi.
3. Zapytanie i indeks koduja wartosci przez **jedno** zrodlo prawdy zadeklarowane przy `QueryParameter`; test parametryzowany sprawdza zgodnosc dla kazdej stalej enuma.
4. Publiczne API `FullTextRepository` nie zawiera typow `org.apache.lucene.*` ani `org.hibernate.search.*`.
5. Istnieje modul `homeportal-commons-domain` bez zaleznosci od JPA/Lucene, z ktorego `hac` moze korzystac bezposrednio.
6. `homeportal-commons-logging` nie zalezy od `-data`.
7. Istnieje `context/changes/commons-refactoring/migration-6.0.md` z mapa zmian API i procedura reindeksu.

Weryfikacja: `mvn clean install` zielony, workflow CI zielony, `mvn dependency:tree -pl homeportal-commons-mail` bez `hibernate-search`/`lucene`, `grep -r "org.apache.lucene\|org.hibernate.search" homeportal-commons-data/src/main/java/pl/homeportal/commons/data/repository/FullTextRepository.java` pusty.

### Key Discoveries:

- Jedyne miejsce wykonania surowego Lucene to `repository/FullTextRepositoryImpl.java:202-236` — parser, analizator, sort i polityka wyjatkow w dwudziestu liniach. Naprawa tam promieniuje na wszystkich konsumentow.
- `PerFieldAnalyzerWrapper` z pusta mapa (`:229-236`) degeneruje sie do jednego analizatora dla wszystkich pol; wlasciwe zrodlo to `getSearchFactory().getAnalyzer(t)`.
- `org.hibernate.search.query.dsl` i `ProjectionConstants` **nie sa uzywane nigdzie** — powierzchnia przyszlej migracji HS6 to dwa pliki repozytorium i cztery bridge'y.
- Nazwa pakietu `pl.homeportal.commons.data.repository` jest wpisana stringiem w `@EnableJpaRepositories` w trzech aplikacjach (np. `portal/.../PortalRepositoryConfiguration.java:7`) — przeniesienie pakietu psuje sie w runtime, nie przy kompilacji.
- `FeatureType` jest zapisany w bazie portalu jako string (`portal/.../FeatureTranslation.java:31-32`, `@Enumerated(EnumType.STRING)`) — nazwy stalych sa danymi, nie tylko kodem.

## What We're NOT Doing

- **Nie migrujemy na Hibernate Search 6, jakarta ani JDK 17.** To zmiana `jdk17-migration`; tutaj tylko przygotowujemy grunt (ukrycie typow HS za wlasna abstrakcja, izolacja bridge'ow).
- **Nie aktualizujemy konsumentow.** `hop`, `portal`, `importer`, `hac` dostaja `migration-6.0.md`; przepiecie kazdego z nich to osobna zmiana 10x w jego repo.
- **Nie zmieniamy nazw pakietow uzywanych w `basePackages`** (`pl.homeportal.commons.data.repository`) — ryzyko cichej awarii w runtime przewyzsza zysk.
- **Nie zmieniamy nazw stalych `FeatureType`** — sa zapisane w bazie portalu.
- **Nie przepisujemy `LoggingSupport` na nowy interfejs logowania** poza tym, co wymusza rozciecie zaleznosci od `-data`.
- **Nie wprowadzamy dual-write ani wersjonowania pol indeksu** — przyjmujemy pelny reindeks przy wdrozeniu.
- **Nie ruszamy `homeportal-commons-geo-api` ani `-location-api`** (inny parent, poza aggregatorem).

## Implementation Approach

Kolejnosc jest podyktowana ryzykiem, nie wartoscia biznesowa: najpierw fundament, ktory pozwala cokolwiek udowodnic (encoding + CI z testami), potem poprawki niezalezne od siebie, potem zmiany strukturalne, na koncu zmiany lamiace API. Kazda faza zostawia repo w stanie zdatnym do wydania.

Zasada dla bugfixow: **test najpierw** — commit z testem, ktory nie przechodzi, potem poprawka. Dzieki temu wida.c w historii, ze test faktycznie lapie blad.

Zasada dla FTS: kodowanie wartosci staje sie deklaracja przy `QueryParameter` (kazdy parametr mowi, jakim encoderem sie posluguje), a nie zaszyta decyzja w `SearchQuery`. To jednoczesnie naprawia rozjazd i jest ksztaltem, ktorego oczekuje `ValueBridge` z HS6 — jedna zmiana obsluguje poprawnosc i przyszla migracje.

## Critical Implementation Details

**Kolejnosc: encoding przed czymkolwiek innym.** Faza 1 musi dodac `project.build.sourceEncoding=UTF-8` zanim ktokolwiek dotknie `FeatureBridge`, `PropertyTypeBridge` czy testow z polskimi znakami. Bez tego testy kodowania przechodza albo nie w zaleznosci od maszyny, a to jest dokladnie ten rodzaj falszywego sygnalu, ktory uniewaznia cala faze 4.

**Rozstrzyganie fragmentow Spring Data.** `FullTextRepositoryImpl.save`/`delete` maja te sama erased signature co `CrudRepository` i **wygrywaja** — `repository.save(x)` woła kod z commons, nie `SimpleJpaRepository`. Zmiana nazw w fazie 5 nie jest kosmetyka: przywraca konsumentom domyslne zachowanie Spring Data. Trzeba to wyroznic w `migration-6.0.md`, bo po migracji `save()` zacznie robic `persist` zamiast `merge` — czyli zmieni sie zachowanie kodu, ktorego nikt nie tknal.

**Reindeks jest czescia wdrozenia, nie opcja.** Zmiany w `NumericBridge` (usuniecie `intValue()`, kodowanie liczb ujemnych) i w normalizacji akcentow zmieniaja format zapisu w indeksie. Do momentu pelnego `indexAll` wyszukiwarka zwraca niepelne wyniki. Procedura musi trafic do `migration-6.0.md` wraz z kolejnoscia: wdroz kod → uruchom reindeks → dopiero potem wlacz ruch na nowe filtry.

**`IndexerMonitor.acquireLock` nie jest lockiem** (`index/IndexerMonitor.java:26-33`) — ustawia flage bezwarunkowo. Skoro reindeks staje sie krokiem wdrozeniowym uruchamianym z JMX i ze schedulera, ta poprawka (faza 5) jest warunkiem bezpieczenstwa procedury z poprzedniego akapitu, a nie drobiazgiem.

---

## Phase 1: Fundament — build i CI

### Overview

Ustawienie warunkow, w ktorych kolejne fazy sa w ogole weryfikowalne: deterministyczny build, spojne wersje Springa, dzialajace logowanie i CI, ktore uruchamia testy.

### Changes Required:

#### 1. Konfiguracja buildu

**File**: `pom.xml`

**Intent**: Uczynic build deterministycznym i niezaleznym od ustawien maszyny. Dzis brak encodingu psuje polskie literaly, a niewersjonowany `maven-source-plugin` daje inny wynik u kazdego.

**Contract**: `properties` zyskuje `project.build.sourceEncoding=UTF-8` i `project.reporting.outputEncoding=UTF-8`. Powstaje sekcja `<pluginManagement>` z przypietymi wersjami: compiler, surefire, source, jar, install, deploy. Usuniecie dwoch martwych, blednie nazwanych wlasciwosci `javax.persitence.api.version` i `hibernate.javax.persitence.api.version` (`pom.xml:30-31`, zero odwolan).

#### 2. Spojnosc wersji Springa

**File**: `pom.xml`

**Intent**: Usunac rozjazd trzech wersji Spring Framework, ktory jest klasyczna konfiguracja pod `NoSuchMethodError` w runtime u konsumenta.

**Contract**: `dependencyManagement` importuje `spring-framework-bom` i `spring-data-bom` (`<type>pom</type>`, `<scope>import</scope>`) zamiast pinowac pojedyncze artefakty `spring-*`. Wlasciwosci `spring.context.version` i `spring.test.version` znikaja na rzecz jednej wersji BOM-u. Docelowa linia: Spring Framework 5.2.x zgodna ze `spring-data-jpa` 2.2.10 (nie 5.3 — to nalezy do `jdk17-migration`).

#### 3. Logowanie

**File**: `pom.xml`, `homeportal-commons-java/pom.xml`

**Intent**: Zejsc z porzuconej bety `slf4j 1.8.0-beta2` na stabilna linie 1.7.x, zgodna z bindingami, ktore realnie maja konsumenci. Dzis logi z commons moga byc NOP-em.

**Contract**: `slf4j.version` → 1.7.36. Zadna zmiana w kodzie nie jest potrzebna (API 1.7 vs 1.8 jest zgodne w uzyciu). Weryfikacja: testowy log w dowolnym module faktycznie sie pojawia.

#### 4. CI uruchamiajace testy

**File**: `.github/workflows/publish.yml`, nowy `.github/workflows/build.yml`

**Intent**: Rozdzielic build od publikacji. Dzis jedyny workflow to reczny deploy z `-DskipTests`, wiec nic nigdy nie uruchamia testow automatycznie.

**Contract**: Nowy workflow `build.yml` na `push` i `pull_request`, JDK 8 (temurin), krok `mvn -B verify`. `publish.yml` bez zmian w mechanice deployu (GitHub Packages nie pozwala nadpisac wydanej wersji — bump wersji przy publikacji zostaje jako wymog).

#### 5. Naprawa testow, ktore nic nie asertuja

**File**: `homeportal-commons-java/src/test/java/pl/homeportal/commons/datetime/DateFormatsTest.java`, `.../validation/ObjectValidatorTest.java`, `homeportal-commons-logging/src/test/java/pl/homeportal/commons/service/LoggingSupportTest.java`

**Intent**: Testy bez asercji daja falszywy sygnal zielonego buildu; wlaczamy wlasnie CI, wiec ten sygnal zaczyna miec znaczenie.

**Contract**: `DateFormatsTest` — `System.out.println` zastapiony asercja na format wyjscia. `ObjectValidatorTest` — trzy metody bez asercji dostaja asercje; dwie metody deklarujace test `validateWithoutNull` faktycznie wolaja `validateWithNull` (`:40-45`, `:47-53`) — do poprawienia, bo `validateWithoutNull` nie ma dzis zadnego pokrycia. `LoggingSupportTest` — przeniesienie z pakietu `pl.homeportal.commons.service` do `pl.homeportal.commons.logging` (zgodnie z klasa pod testem) i dodanie asercji.

### Success Criteria:

#### Automated Verification:

- Build przechodzi: `mvn clean install`
- Build jest niezalezny od locale: `mvn clean install -Duser.language=en -Dfile.encoding=ISO-8859-1`
- Brak ostrzezenia o niewersjonowanym pluginie: `mvn -B clean install 2>&1 | grep -c "version.*not specified"` zwraca 0
- Jedna wersja Springa w drzewie: `mvn dependency:tree -Dincludes=org.springframework:*` — brak rozjazdu miedzy `spring-core`, `spring-beans`, `spring-context`, `spring-aop`
- Workflow CI zielony na branchu

#### Manual Verification:

- Log z dowolnej klasy commons faktycznie pojawia sie w konsoli aplikacji konsumenckiej (weryfikacja bindingu slf4j)
- Polskie znaki w `Market.java` i `FeatureBridgeTest.java` sa poprawne po buildzie na maszynie z innym domyslnym encodingiem

**Implementation Note**: Po zakonczeniu fazy i przejsciu weryfikacji automatycznej — pauza na potwierdzenie weryfikacji manualnej przed faza 2.

---

## Phase 2: Bugfixy poza FTS (`-java`, `-mail`, `-logging`)

### Overview

Poprawki niezalezne od siebie i od reszty planu, kazda z testem regresyjnym pisanym jako pierwszy. Faza jest rozbijalna na rownolegle commity.

### Changes Required:

#### 1. Refleksja i tekst

**File**: `homeportal-commons-java/src/main/java/pl/homeportal/commons/reflection/ClassFieldReader.java`, `.../text/StringUtils.java`

**Intent**: `readFieldValues` rzuca NPE dla dowolnego obiektu z polem `null` (`Collectors.toMap` odrzuca null); pomija tez pola dziedziczone i syntetyczne. `stripInvalidXmlCharacters` usuwa pierwszy znak kazdego wejscia i rzuca NPE na `null`.

**Contract**: `ClassFieldReader:15-16` — zbieranie do `HashMap` zamiast `Collectors.toMap`, przejscie po lancuchu nadklas, pominiecie `isSynthetic()` i pol statycznych. `StringUtils:41-65` — usuniecie specjalnego przypadku `i == 0` (petla ma sprawdzac `XMLChar.isValid` dla kazdego indeksu) i null-guard. Rozstrzygniecie kontraktu `normalize` (`:15,33`): regex `[^a-zA-Z]` usuwa cyfry mimo nazwy stalej `NOT_ALPHANUMERIC` — decyzja: zachowac zachowanie, przemianowac stala na `NOT_ALPHABETIC` (zmiana regexu zmienilaby generowane slugi/URL-e).

#### 2. Zip i pliki

**File**: `homeportal-commons-java/src/main/java/pl/homeportal/commons/zip/ZipEntryExtractor.java`, `.../file/Files.java`

**Intent**: `extract` nie zamyka `ZipFile` (wyczerpanie deskryptorow w petli importera) i rzuca NPE przy braku wpisu; `isAvailable` zwraca `true` dla dowolnego czytelnego pliku. `Files.deleteFiles` dopasowuje wzorzec do katalogow i pomija rekurencje, a wynik `delete()` jest ignorowany przy bezwarunkowym logu sukcesu.

**Contract**: `ZipEntryExtractor:29-35` — odczyt wpisu w `try-with-resources`, zwrot `ByteArrayInputStream`, nazwany wyjatek gdy `getEntry` zwroci `null`; `:37-47` — `isAvailable` przez `new ZipFile(path)` i sprawdzenie wpisow. Usuniecie martwych `TEXT_SUFFIXES`/`isTextFile` (`:20-27,49-60`). `Files:62-74` — sprawdzenie `isDirectory()` przed dopasowaniem wzorca i rekurencja; `:34-35,66-67` — log sukcesu tylko gdy `delete()` zwrocil `true`, w przeciwnym razie WARN; `:43-47` — przekazanie wyjatku do `log.error`.

#### 3. Bezpieczenstwo

**File**: `homeportal-commons-java/src/main/java/pl/homeportal/commons/security/MD5Encoder.java`, `.../security/PasswordGenerator.java`

**Intent**: Skrot liczony domyslnym charsetem platformy — haslo z polskim znakiem daje inny wynik po zmianie JVM/OS, czyli uzytkownik przestaje sie logowac bez zadnego bledu. `PasswordGenerator` uzywa `Math.random()` (LCG), wiec wygenerowane tokeny sa przewidywalne.

**Contract**: `MD5Encoder:22-23` — `getBytes(StandardCharsets.UTF_8)`; `:41-44` — `NoSuchAlgorithmException` opakowany w `HomeportalSecurityException` zamiast `return null`. `PasswordGenerator:19-30` — `SecureRandom` i indeksowanie w jeden jawny alfabet (dzis rozklad jest skrzywiony: cyfry maja te sama wage co litery). Petla hex w `MD5Encoder:25-38` jest poprawna — nie ruszamy. Migracja hasel na bcrypt/PBKDF2 jest **poza zakresem** (dotyka schematu bazy konsumenta).

#### 4. Data, czas, i18n

**File**: `homeportal-commons-java/src/main/java/pl/homeportal/commons/datetime/DateTimeUtils.java`, `.../datetime/Timer.java`, `.../datetime/DateFormats.java`, `.../i18n/Language.java`, `.../i18n/LanguageResolver.java`

**Intent**: `todayPlusMonths`/`todayPlusYears`/`todayMinusYears` licza na stalych `MONTH = DAY*31` i `YEAR = 372 dni`, wiec nie sa odwrotnoscia poprawnego `todayMinusMonths` i drifta ~7 dni na rok. `Language.UKRAINIAN` ma wartosc `"ua"`, gdy ISO 639-1 to `"uk"`. `LanguageResolver` zwraca `null` z metod, ktore obok maja poprawny fallback.

**Contract**: `DateTimeUtils:70-83` — wszystkie cztery metody przez `LocalDateTime.plusMonths/plusYears`; stale `MONTH`/`YEAR` (`:16-17`) usuniete. `Timer:19-29` — `summary()` przestaje nadpisywac wartosc zapisana przez `end()`. `DateFormats:50,55,60,74,79` — `Locale.ROOT` i jawna strefa. `Language:13` — `"uk"`, z aliasem dla `"ua"` w `getByValue`, bo stara wartosc moze byc utrwalona w bazie konsumenta (do wyroznienia w dokumencie migracji). `LanguageResolver:32,48,51-59` — fallback zamiast `return null`.

#### 5. Obrazy

**File**: `homeportal-commons-java/src/main/java/pl/homeportal/commons/image/ImageProcessor.java`, `.../image/ImageResizer.java`

**Intent**: Konstruktor zadania rzuca `StringIndexOutOfBoundsException` dla nazwy bez kropki; petla oczekiwania krec.i sie na nie-`volatile` fladze z pustym `catch`, co przy JIT moze nie skonczyc sie nigdy i jest nieprzerywalne; wynik `ImageIO.write` jest ignorowany, wiec nieobslugiwany format konczy sie "sukcesem" bez pliku.

**Contract**: `ImageProcessor:135-143` — guard na `lastIndexOf(".")` i odrzucenie nazw bez rozpoznanego rozszerzenia; `:44-61,89,118` — zastapienie recznego watku i spin-loopa `ExecutorService` + `invokeAll` (usuwa jednoczesnie problem `volatile`, nieprzerywalnosci i nieograniczonej liczby watkow OS z `:40`); `:105-116` — `try-with-resources` na strumieniu zrodlowym; `:131` i `ImageResizer:21` — sprawdzenie wyniku `ImageIO.write` i wyjatek gdy `false`.

#### 6. Wyjatki

**File**: `homeportal-commons-java/src/main/java/pl/homeportal/commons/exception/*.java`

**Intent**: `HomeportalServiceException.getMessage()` rzuca NPE gdy uzyto konstruktora bezargumentowego — i robi to wewnatrz handlera bledu, maskujac oryginalna przyczyne.

**Contract**: `HomeportalServiceException:39` — `String.valueOf(super.getMessage())`; analogicznie `HomeportalValidationException:46`. Wszystkie trzy wyjatki dostaja `serialVersionUID`, `HomeportalSecurityException` — konstruktor przyjmujacy przyczyne. Usuniecie osmiu nieuzywanych importow (`HomeportalServiceException:3-12`) i martwego `removeLastLine` (`HomeportalValidationException:68-73`) razem z zakomentowanym wywolaniem (`:55`).

#### 7. Mail

**File**: `homeportal-commons-mail/src/main/java/pl/homeportal/commons/mail/NotifierAdapter.java`, `.../mail/VelocityEmail.java`

**Intent**: `fork` jest stanem instancji ustawianym przez `notify(dto,false)` i nigdy nieprzywracanym — na beanie singletonowym jedno wywolanie synchroniczne trwale przelacza wszystkie kolejne. `VelocityEmail.send()` zwraca `null` przy dowolnym bledzie, po czym adapter loguje **"Email sent"** — nieudana wysylka wyglada w logach jak sukces.

**Contract**: `NotifierAdapter:28,49` — tryb przekazywany jako parametr sciezki wywolania zamiast pola instancji; `:85-87` — `ExecutorService` zamiast `new Thread` na kazdy mail. `VelocityEmail:227-231` — rozroznienie bledu od sukcesu (wyjatek `HomeportalServiceException` zamiast `null`), `:240-243` — usuniecie pustego `catch`, `:84,98` — `RuntimeException` → `HomeportalServiceException`. Cztery szablony `.vm` odwolujace sie do zmiennych, ktorych `model()` (`:211`) nigdy nie wklada (`question.vm`, `registerUser.vm`, `resetPasswordNewPassword.vm`, `resetPasswordReqLink.vm`) — usuniete jako martwe (weryfikacja: brak odwolan w `portal`/`importer`).

#### 8. Logging

**File**: `homeportal-commons-logging/src/main/java/pl/homeportal/commons/logging/LoggingSupport.java`

**Intent**: `logWithoutExceptionForSaveOrUpdate` i `logWithoutExceptionForDelete` przyjmuja `Exception` i go nie loguja — sygnatura klamie.

**Contract**: `:196-199` i `:207-210` — albo logowanie wyjatku, albo usuniecie parametru. Decyzja: logowac (usuniecie parametru lamie 343 wywolania downstream bez zysku). Eager `String.format` przy wylaczonym poziomie logu → guard `isXxxEnabled()`.

### Success Criteria:

#### Automated Verification:

- Wszystkie testy przechodza: `mvn clean install`
- Kazda poprawka ma test, ktory nie przechodzi bez niej (weryfikacja przez odwrocenie poprawki lokalnie przed commitem)
- Brak `printStackTrace` w kodzie produkcyjnym: `grep -rn "printStackTrace" --include=*.java */src/main` zwraca tylko `FullTextRepositoryImpl` (naprawiany w fazie 5)
- Test dla `ClassFieldReader` z obiektem majacym pole `null` przechodzi
- Test dla `StringUtils.stripInvalidXmlCharacters` zachowuje pierwszy znak

#### Manual Verification:

- Wysylka maila przy celowo blednym SMTP loguje blad, a nie sukces
- Generowanie miniatur dla pliku bez rozszerzenia zwraca czytelny blad zamiast `StringIndexOutOfBoundsException`
- Hasla utworzone przed zmiana `MD5Encoder` nadal weryfikuja sie poprawnie dla ASCII (zmiana dotyczy tylko znakow spoza ASCII — do wyroznienia w dokumencie migracji)

**Implementation Note**: Pauza na potwierdzenie weryfikacji manualnej przed faza 3.

---

## Phase 3: `commons-domain` i bugfixy domenowe

### Overview

Wydzielenie modelu domenowego do modulu bez zaleznosci ORM/Lucene, poprawki bledow copy-paste w tym modelu, oraz rozciecie zaleznosci `-logging` → `-data`.

### Changes Required:

#### 1. Nowy modul

**File**: nowy `homeportal-commons-domain/pom.xml`, root `pom.xml`

**Intent**: Dac konsumentom (`hac`, docelowo `spy`) dostep do stalych i enumow domenowych bez ciagniecia Hibernate, Hibernate Search i Lucene. Dzis `hac` utrzymuje reczne kopie `OfferProduct`/`OfferActivity`/`OfferMarket`/`FeatureType`, ktore beda sie rozjezdzac.

**Contract**: Nowy modul w `<modules>` przed `-data`, zaleznosc wylacznie od `-java`. Przenoszone typy: `model/Product`, `model/Market`, `model/Activity`, `model/feature/{Feature, FeatureType, FeatureConverter, FeatureConstants, FeatureTypeProvider}`, `model/interfaces/*`. Pakiety **zachowuja obecne nazwy** (`pl.homeportal.commons.data.model.*`) — split-package miedzy modulami Maven jest dopuszczalny w Javie 8, a zmiana nazw dotknelaby 1013 miejsc downstream. `-data` dostaje zaleznosc od `-domain`, wiec dla obecnych konsumentow nic sie nie zmienia w imporcie.

#### 2. Rozciecie `-logging` → `-data`

**File**: `homeportal-commons-logging/src/main/java/pl/homeportal/commons/logging/LoggingSupport.java`, `homeportal-commons-logging/pom.xml`

**Intent**: Jeden import `AbstractEntity` (`:5`) sprawia, ze `-mail` ciagnie caly stos ORM+Lucene, zeby wyslac maila, a `hac` musi robic `<exclusions>`.

**Contract**: Przeciazenia przyjmujace `AbstractEntity` przyjmuja zamiast tego identyfikator i nazwe typu (albo `Supplier<String>`). `homeportal-commons-logging/pom.xml` traci zaleznosc od `-data` i nieuzywany `hibernate-validator` (`:29-32`). Weryfikacja: `mvn dependency:tree -pl homeportal-commons-mail` bez `hibernate-*` i `lucene-*`. Zmiana sygnatur trafia do dokumentu migracji.

#### 3. `FeatureTypeProvider`

**File**: `homeportal-commons-domain/src/main/java/pl/homeportal/commons/data/model/feature/FeatureTypeProvider.java`

**Intent**: Cztery bledy copy-paste dzialajace dzis: `forRentObject()` i `forSaleObject()` zwracaja **puste listy** (bo inicjalizatory wypelniaja listy biurowe), `forRentLand()` zwraca cechy hal, a `FLOOR_QUANTITY` jest dodawany dwa razy do `forSaleHouse`.

**Contract**: `:152-174` i `:272-291` — zapis do wlasciwych list; `:313-316` — zwrot `forRentLand`; `:219-220` — usuniecie duplikatu. Struktura z ~250 linii copy-paste zastapiona mapa `(Activity, Product) → List<FeatureType>` budowana raz. Gettery (`:293-351`) zwracaja `unmodifiableList` zamiast zywej, mutowalnej statycznej `LinkedList`. **Uwaga**: poprawka zmienia to, co uzytkownik widzi na filtrach dla ofert typu "obiekt" i "grunt" — wymaga weryfikacji manualnej z konsumentem.

#### 4. `FeatureConverter`

**File**: `homeportal-commons-domain/src/main/java/pl/homeportal/commons/data/model/feature/FeatureConverter.java`

**Intent**: Wartosci wielokrotne sa gubione dwoma niezaleznymi bledami: `extractValues` (`:76-79`) robi `split("^")`, a `^` w regexie jest kotwica zerowej dlugosci — string nie jest dzielony wcale; niezaleznie `toFeatureMap` (`:39`) bierze `values[0]`. Kolejnosc wyjscia `toFeatures` zalezy od `HashMap`, wiec re-serializacja niezmienionej oferty brudzi wiersz i wymusza re-indeks.

**Contract**: `:78` — `split(Pattern.quote(VALUE_SEPARATOR))`; `:39` — mapa przechowuje wszystkie wartosci; `:26,53` — `LinkedHashMap`/sortowanie dla deterministycznego wyjscia; `:64-74` — `extractName` przestaje uzywac `catch (Exception)` jako sterowania. Test musi objac `MEDIA:prąd^woda^gaz` (round-trip zachowuje trzy wartosci) i determinizm kolejnosci.

### Success Criteria:

#### Automated Verification:

- Build przechodzi: `mvn clean install`
- `-mail` nie ciagnie ORM/Lucene: `mvn dependency:tree -pl homeportal-commons-mail | grep -c "hibernate-search\|lucene"` zwraca 0
- `-domain` nie ma zaleznosci ORM: `mvn dependency:tree -pl homeportal-commons-domain | grep -c "hibernate\|lucene\|spring-data"` zwraca 0
- Test: `forRentObject()`, `forSaleObject()` zwracaja niepuste listy; `forRentLand()` nie zwraca cech hal
- Test: round-trip `MEDIA:prąd^woda^gaz` zachowuje trzy wartosci
- Test: `toFeatures` daje ten sam wynik dla tego samego wejscia w 100 kolejnych wywolaniach

#### Manual Verification:

- Filtry cech dla ofert typu "obiekt" (najem i sprzedaz) pokazuja pozycje w aplikacji konsumenckiej
- Filtry dla gruntow pokazuja cechy gruntow, a nie hal
- `hac` po podmianie na `commons-domain` kompiluje sie bez `<exclusions>` (weryfikacja lokalna, bez commitowania w `hac`)

**Implementation Note**: Pauza na potwierdzenie weryfikacji manualnej przed faza 4.

---

## Phase 4: FTS — kontrakt kodowania

### Overview

Rdzen naprawy wyszukiwarki: jedno zrodlo prawdy o kodowaniu wartosci po stronie indeksu i zapytania. To faza, ktora zmienia format indeksu i wymusza reindeks.

### Changes Required:

#### 1. Encoder deklarowany przy parametrze

**File**: `homeportal-commons-data/src/main/java/pl/homeportal/commons/data/search/QueryParameter.java`, `.../search/SearchQuery.java`

**Intent**: Dzis `SearchQuery` koduje kazdy term przez `PropertyTypeBridge`, a zakresy przez `NumericBridge` — niezaleznie od tego, jakim bridgem pole zostalo zaindeksowane. Stad ciche zero wynikow dla cech z akcentami (`FeatureBridge` zachowuje `\p{L}`, zapytanie strippuje) i dla pol bez `PropertyTypeBridge` (ktory usuwa wszystkie spacje, wiec `nowysacz` nie trafia w tokeny `[nowy, sącz]`).

**Contract**: `QueryParameter` zyskuje deklaracje encodera (metoda zwracajaca strategie kodowania wartosci). `SearchQuery:37-92` — buildery przestaja wolac `normalize()`/`longToString()` na sztywno, uzywaja encodera z przekazanego parametru. Szesc niemal identycznych blokow `StringBuilder` (`:39-42,47-50,55-61,66-69,75-78`) zwija sie w jedna sciezke — to ta duplikacja jest powodem, dla ktorego `addPhraseParameter` (`:64-70`) jako jedyny nie normalizuje niczego. Enumy `QueryParameter` w `hop` i `portal` musza zadeklarowac encodery — to zmiana lamiaca, do dokumentu migracji.

#### 2. Escapowanie i zakresy otwarte

**File**: `homeportal-commons-data/src/main/java/pl/homeportal/commons/data/search/SearchQuery.java`

**Intent**: Wartosci trafiaja do zapytania bez escapowania — term `x OR product:1` sklei sie w `xORproduct:1`, co parser czyta jako klauzule na polu sterowanym przez uzytkownika; znaki `[ ~ ^ { }` po prostu rzucaja wyjatek konczacy sie bledem 500. Brak zakresu otwartego wymusil w `hop` sentinel `MAX = "9999999"`, przez ktory oferty powyzej ~10 mln PLN wypadaja z wynikow "cena od".

**Contract**: Escapowanie (`QueryParser.escape`) w jednym miejscu sciezki budowania fragmentu. Nowe metody zakresu jednostronnego emitujace `[x TO *]` i `[* TO x]`. Null/blank guard we wszystkich builderach (dzis `addRangeParameter` rzuca `NumberFormatException` z `Long.valueOf` na pustym polu formularza, a `normalize(null)` emituje literalne `(field:null)`). `addDateRangeParameter` (`:81-92`) dostaje nawiasy jak reszta klauzul i guard na `null`. `isSortEmpty()` (`:105-108`) zwraca dzis odwrotnosc swojej nazwy — do poprawienia (metoda jest nieuzywana, wiec zmiana jest bezpieczna).

#### 3. `NumericBridge`

**File**: `homeportal-commons-data/src/main/java/pl/homeportal/commons/data/search/bridge/NumericBridge.java`

**Intent**: `intValue()` (`:30`) przepelnia sie dla wartosci powyzej `Integer.MAX_VALUE` — cena 3 mld indeksuje sie jako `-1294967296`, bez paddingu, poza porzadkiem. Padding zerami lamie tez porzadek liczb ujemnych (`pad("-5")` → `"00000000-5"` sortuje sie przed `"00000000-9"`).

**Contract**: Kodowanie z `long`/`BigDecimal` bez zwezania, szerokosc paddingu obejmujaca pelen zakres, offset przesuwajacy wartosci ujemne do dodatnich przed paddingiem. Wartosci dluzsze niz szerokosc paddingu nie moga cicho pomijac paddingu (`:31`). To zmiana formatu indeksu — wymaga reindeksu.

#### 4. Analizator ze search factory

**File**: `homeportal-commons-data/src/main/java/pl/homeportal/commons/data/repository/FullTextRepositoryImpl.java`

**Intent**: `getAnalyzer` (`:229-236`) tworzy `PerFieldAnalyzerWrapper` z **pusta mapa**, wiec degeneruje sie do jednego analizatora dla wszystkich pol wszystkich encji; analizatory, ktorych Hibernate Search uzyl przy indeksowaniu, nie sa w ogole brane pod uwage. Do tego kazde wywolanie alokuje nowy `Analyzer` (`Closeable`) i nigdy go nie zamyka.

**Contract**: Analizator pobierany z `getSearchFactory().getAnalyzer(t)` — zarzadzany i wspoldzielony, wiec problem wycieku znika sam. Flaga `keywordAnalyser` (`SearchQuery:34-35`, `FullTextRepository:38`) przestaje sterowac wyborem analizatora; jej usuniecie z API nalezy do fazy 5.

#### 5. Test zgodnosci kodowania

**File**: nowe testy w `homeportal-commons-data/src/test/java/pl/homeportal/commons/data/search/`

**Intent**: Rozjazd indeks/zapytanie byl niewykrywalny, bo nic go nie sprawdzalo. Test ma zamienic konwencje w asercje.

**Contract**: Test parametryzowany po stalych `QueryParameter` sprawdzajacy, ze encoder zapytania i bridge indeksu daja ten sam wynik dla zestawu wartosci brzegowych: polskie znaki (`wtórny`, `wolnostojący`), nazwy wieloczlonowe (`Nowy Sącz`), wartosci ujemne, wartosci powyzej `Integer.MAX_VALUE`, `null`, pusty string, znaki specjalne Lucene. Plus testy jednostkowe `SearchQuery` (dzis zero) i `PropertyTypeBridge` (dzis zero).

### Success Criteria:

#### Automated Verification:

- Build przechodzi: `mvn clean install`
- Test zgodnosci kodowania przechodzi dla kazdej stalej `QueryParameter`
- Test: wartosc `3_000_000_000` koduje sie bez przepelnienia i zachowuje porzadek wzgledem `2_000_000_000`
- Test: wartosci ujemne sortuja sie zgodnie z porzadkiem liczbowym
- Test: term ze znakami specjalnymi Lucene nie powoduje wyjatku parsera
- Test: zakres jednostronny `[x TO *]` buduje sie poprawnie

#### Manual Verification:

- Po reindeksie na srodowisku testowym filtr po cesze z polskim znakiem (`wtórny`) zwraca wyniki
- Filtr po miescie o nazwie dwuczlonowej (`Nowy Sącz`) zwraca wyniki
- Filtr "cena od" zwraca oferty powyzej 10 mln PLN
- Czas pelnego reindeksu na srodowisku testowym zmierzony i zapisany (wejscie do procedury wdrozenia)

**Implementation Note**: Pauza na potwierdzenie weryfikacji manualnej przed faza 5. Weryfikacja manualna tej fazy **wymaga pelnego reindeksu** na srodowisku testowym.

---

## Phase 5: FTS — API repozytorium i paginacja

### Overview

Zmiany lamiace API: nazwy metod kolidujace ze Spring Data, granice transakcji, ukrycie typow Lucene/HS, rozplatanie `Page`/`PageItems`.

### Changes Required:

#### 1. Nazwy metod i granice transakcji

**File**: `homeportal-commons-data/src/main/java/pl/homeportal/commons/data/repository/FullTextRepository.java`, `.../repository/FullTextRepositoryImpl.java`

**Intent**: `save`/`delete`/`findAll` maja te sama erased signature co `CrudRepository` i **wygrywaja** rozstrzygniecie fragmentu — `repository.save(nowaOferta)` woła `merge` (argument zostaje transientny), `delete(detached)` rzuca `IllegalArgumentException`. Tylko `indexOne` ma `@Transactional`, a `flushToIndexes()` pisze do Lucene przed commitem, wiec rollback zostawia indeks opisujacy nieistniejace wiersze.

**Contract**: Rename na `indexedSave`/`indexedDelete`/`searchAll` — przestaja przeslaniac `CrudRepository`. `@Transactional` na poziomie klasy; usuniecie eager `flushToIndexes()` na rzecz synchronizacji transakcyjnej HS. `deleteAll(Class)` (`:64-74`) — bulk delete zamiast ladowania calej tabeli do pamieci. `countByIndex` (`:93-101`) — logger zamiast `printStackTrace` i wyjatek zamiast sentinela `-1`. `createQuery` (`:218-221`) — `HomeportalServiceException` zamiast `IllegalArgumentException("Probably parsing lucene query exception")`. Przywrocenie flagi przerwania w `catch (InterruptedException)` (`:175-178`).

#### 2. Ukrycie typow Lucene i Hibernate Search

**File**: `homeportal-commons-data/src/main/java/pl/homeportal/commons/data/repository/FullTextRepository.java`, `.../SortFieldAware.java`

**Intent**: `SortField[]` i `FullTextQuery` w sygnaturach interfejsu (`:4,38`) sprawiaja, ze 17 repozytoriow downstream kompiluje sie przeciw Lucene 5 i HS 5. To najwiekszy pojedynczy bloker przyszlej migracji HS6.

**Contract**: Wlasna abstrakcja sortowania (odpowiednik `SortSpec`) zamiast `SortField[]`; `createQuery` przestaje byc czescia publicznego interfejsu (widocznosc pakietowa), a metody wyszukujace zwracaja wlasny typ wyniku zamiast `FullTextQuery`. Usuniecie flagi `boolean keywordAnalyser` z API (po fazie 4 nie steruje juz niczym). `Class<T> t` znika z metod, ktore moga wywnioskowac typ z fragmentu — 10 z 13 metod przyjmuje go dzis tylko dlatego, ze fragment nie zna wlasnej klasy domenowej.

#### 3. Paginacja

**File**: `homeportal-commons-data/src/main/java/pl/homeportal/commons/data/pageable/Page.java`, `.../pageable/PageItems.java`, `.../repository/FullTextRepositoryImpl.java`

**Intent**: `Page extends PageRequest` i przeslania pola nadklasy, wiec `equals`/`hashCode`/`getOffset`/`next()` czytaja stan nadklasy — dwa `Page` roznione tylko `setPage` sa **rowne** (trucizna dla dowolnego cache'u kluczowanego pageable'em). `PageItems.getPageItems()` zwraca `null` przy zerowej liczbie wynikow, czyli kazda pusta lista wynikow to NPE w widoku. Konwersja 1-based/0-based jest powielona w trzech miejscach.

**Contract**: `Page` przestaje dziedziczyc po `PageRequest` — zostaje formularzem z metoda `toPageable()`; konwersja indeksu strony zyje odtad w jednym miejscu. `PageItems:55-87` — `pageItems` inicjalizowane pusta lista; `:194` — null-guard przed dereferencja; `:139-142` — bare `catch (Exception) → null` zastapione logowaniem i propagacja; `:72,84` — koniec mutowania formularza uzytkownika w petli. `FullTextRepositoryImpl:242-249` — brakujacy `else`, przez ktory kazde pole z `reverse` emituje `DESC` i zaraz potem `ASC`; `:253-260` — `getSort` przestaje zwracac tylko pierwszy order. **Uwaga**: `Page` jest bazowa klasa formularzy bindowanych jako `@ModelAttribute` w publicznym API hop-a, wiec nazwy pol (`page`, `size`, `sort`, `reverse`) **nie moga sie zmienic** — to nazwy parametrow HTTP.

#### 4. Lock reindeksu

**File**: `homeportal-commons-data/src/main/java/pl/homeportal/commons/data/index/IndexerMonitor.java`

**Intent**: `acquireLock` (`:26-33`) ustawia flage bezwarunkowo i nigdy nie sprawdza biezacego stanu — dwa reindeksy (scheduler + JMX) moga ruszyc rownolegle, a pierwszy `releaseLock` zwolni flage w trakcie pracy drugiego. Skoro reindeks staje sie krokiem wdrozeniowym, to warunek bezpieczenstwa procedury.

**Contract**: `compareAndSet(false, true)` zwracajacy wynik pozyskania; `synchronized (MONITOR)` wokol `AtomicBoolean` do usuniecia.

### Success Criteria:

#### Automated Verification:

- Build przechodzi: `mvn clean install`
- Brak typow Lucene/HS w API: `grep -c "org.apache.lucene\|org.hibernate.search" homeportal-commons-data/src/main/java/pl/homeportal/commons/data/repository/FullTextRepository.java` zwraca 0
- Test: `PageItems.getPageItems()` dla zera wynikow zwraca pusta liste, nie `null`
- Test: dwa `Page` roznione numerem strony nie sa rowne
- Test: sortowanie malejace emituje jeden order, nie `DESC` + `ASC`
- Test: `IndexerMonitor` odrzuca drugie pozyskanie locka
- Test integracyjny FTS (H2 + Lucene RAM): zapis → indeks → wyszukanie → paginacja

#### Manual Verification:

- Pusta lista wynikow wyszukiwania renderuje sie bez bledu w aplikacji konsumenckiej
- Reindeks uruchomiony z JMX w trakcie pracy schedulera jest odrzucany, a nie uruchamiany rownolegle
- Sortowanie malejace w aplikacji daje faktycznie odwrocona kolejnosc

**Implementation Note**: Pauza na potwierdzenie weryfikacji manualnej przed faza 6.

---

## Phase 6: Domkniecie — prune, martwy kod, dokument migracji

### Overview

Zmniejszenie powierzchni przed przyszla migracja HS6/JDK17 i przekazanie zmian konsumentom.

### Changes Required:

#### 1. Prune zaleznosci

**File**: `pom.xml`, `homeportal-commons-java/pom.xml`, `homeportal-commons-logging/pom.xml`, `homeportal-commons-test/pom.xml`, `homeportal-commons-mail/pom.xml`

**Intent**: Zaleznosci zadeklarowane i nieuzywane zwiekszaja powierzchnie migracji i wnosza ryzyko (`commons-compress` jest w wersji 1.0 z 2009); zaleznosci uzywane, ale niezadeklarowane, to `NoClassDefFoundError` czekajacy na konsumenta, ktory wezmie sam `-mail`.

**Contract**: Usuniecie z `-java`: `commons-io`, `commons-compress`, `commons-email`, oba `javax.el`; z `-logging`: `hibernate-validator`; z `-test`: `spring-webmvc`, `junit`. Zadeklarowanie brakujacych: `javax.mail` i `commons-collections` w `-mail` (albo zastapienie `CollectionUtils.isEmpty` odpowiednikiem z `commons-lang3`, ktory juz jest). Lombok z `compile` na `provided`. `-test`: `spring-test` z `compile` na `provided` — dzis testowy kod Springa lezy na produkcyjnym classpathie konsumentow. Weryfikacja: `mvn dependency:analyze` bez `Used undeclared` i bez `Unused declared` dla usunietych.

#### 2. Martwy kod

**File**: rozne

**Intent**: Mniej kodu do przeniesienia przy HS6/JDK17. Skan objal 5 repozytoriow (`hac`, `hop`, `portal`, `importer`, `spy`) i nie dowodzi, ze nic innego tego nie uzywa — stad ta faza jest ostatnia, po wszystkich zmianach zachowania.

**Contract**: Usuwane sa wylacznie pozycje z zerowym uzyciem downstream potwierdzonym skanem: `SessionMockProvider` (mock w produkcyjnym jarze `-mail`), `ImageResizer` jesli zostanie bez uzycia po fazie 2, `PageItem`, `SortFieldAware` (po fazie 5), `FeatureConstants`, martwe metody wyliczone w `research.md` sekcja 2.2, zakomentowany test `PageItemsTest.java:167-198` odwolujacy sie do nieistniejacej klasy. **Nie usuwamy** stalych-szablonow `LoggingSupport` mimo zerowego uzycia — sa czescia deklarowanego API biblioteki i tanie w utrzymaniu.

#### 3. Dokument migracji

**File**: nowy `context/changes/commons-refactoring/migration-6.0.md`

**Intent**: Konsumenci (`hop`, `portal`, `importer`, `hac`) migruja we wlasnych repozytoriach i wlasnym tempie — potrzebuja mapy zmian, nie archeologii w diffie.

**Contract**: Dokument zawiera: (a) tabele zmian API stara → nowa nazwa/sygnatura z uzasadnieniem, (b) wyroznione zmiany zachowania bez zmiany sygnatury — przede wszystkim `save()` robiacy odtad `persist` zamiast `merge`, alias `"ua"` → `"uk"` w `Language`, `MD5Encoder` dla znakow spoza ASCII, (c) obowiazek zadeklarowania encoderow w enumach `QueryParameter`, (d) procedure reindeksu z kolejnoscia krokow i zmierzonym czasem z fazy 4, (e) wymog bumpu wersji w konsumentach, ktore uzywaja `${project.parent.version}` (`hop`, `portal`, `importer`) lub wprowadzenia wlasciwosci `homeportal.commons.version` wzorem `hac`, (f) osobno wyroniona pulapke: `homeportal-hop-management/pom.xml:31` ma zaszyte `5.0` dla `commons-logging`.

#### 4. Aktualizacja dokumentacji repo

**File**: `commons.md`, `CLAUDE.md`

**Intent**: `commons.md:3` nadal podaje wersje 5.0 i nie zna modulu `-domain`.

**Contract**: Aktualizacja wersji, dodanie `homeportal-commons-domain` do opisu ukladu modulow i kolejnosci zaleznosci w obu plikach.

### Success Criteria:

#### Automated Verification:

- Build przechodzi: `mvn clean install`
- `mvn dependency:analyze` bez `Used undeclared dependencies`
- Testowy kod Springa nie jest na compile classpath: `mvn dependency:tree -pl homeportal-commons-test -Dscope=compile | grep -c spring-test` zwraca 0
- Workflow CI zielony
- `migration-6.0.md` istnieje i pokrywa kazda zmiane lamiaca z faz 3-5

#### Manual Verification:

- Probne podniesienie `hop` do commons 6.0 na lokalnej galezi kompiluje sie po wykonaniu krokow z `migration-6.0.md` (bez commitowania w `hop`)
- `hac` kompiluje sie z `commons-domain` bez bloku `<exclusions>`

---

## Testing Strategy

### Unit Tests:

- **Kodowanie wartosci** — test parametryzowany po stalych `QueryParameter`, zestaw brzegowy: polskie znaki, nazwy wieloczlonowe, ujemne, `> Integer.MAX_VALUE`, `null`, pusty string, znaki specjalne Lucene
- **`SearchQuery`** — kazdy builder osobno (dzis zero pokrycia), w tym escapowanie i zakresy jednostronne
- **Bugfixy fazy 2 i 3** — jeden test na poprawke, pisany przed poprawka
- **Paginacja** — `PageItems` przy zerze wynikow, `Page` kontrakt `equals`/`toPageable`, sortowanie malejace

### Integration Tests:

- **Harness FTS** (H2 + Lucene RAM directory) — zapis encji, indeksowanie, wyszukanie po kazdym typie klauzuli, paginacja, sortowanie. `portal` ma juz `OfferTestBase` (`portal/homeportal-portal-repository/src/test/.../offer/OfferTestBase.java`) — sciagnac wzorzec w dol zamiast pisac od zera
- **Transakcje** — rollback po `indexedSave` nie zostawia dokumentu w indeksie

### Manual Testing Steps:

1. Wdrozyc na srodowisko testowe, uruchomic pelny reindeks, zmierzyc czas
2. Sprawdzic filtr po cesze z polskim znakiem (`wtórny`) — przed zmiana zwracal zero
3. Sprawdzic filtr po miescie dwuczlonowym (`Nowy Sącz`)
4. Sprawdzic filtr "cena od" dla ofert powyzej 10 mln PLN
5. Sprawdzic filtry cech dla ofert typu "obiekt" (najem i sprzedaz) — przed zmiana lista byla pusta
6. Sprawdzic filtry dla gruntow — przed zmiana pokazywaly cechy hal
7. Otworzyc wyszukiwanie bez wynikow — przed zmiana NPE w widoku
8. Wywolac reindeks z JMX w trakcie pracy schedulera — powinien zostac odrzucony
9. Wyslac maila przy blednej konfiguracji SMTP — w logu ma byc blad, nie "Email sent"

## Performance Considerations

- **Reindeks** — czas mierzony w fazie 4 na srodowisku testowym wchodzi do procedury wdrozenia. Do jego zakonczenia wyszukiwarka zwraca niepelne wyniki.
- **Podwojne wykonanie zapytania** — dzis kazde wyszukanie wykonuje zapytanie Lucene dwa razy (`getResultSize` + okno wynikow), za kazdym razem budujac parser i analizator. Pobranie analizatora ze search factory (faza 4) usuwa alokacje; samo podwojne wykonanie zostaje — wejscie do przyszlej optymalizacji, nie do tego planu.
- **`deleteAll`** — dzis laduje cala tabele do pamieci; bulk delete w fazie 5 usuwa ryzyko `OutOfMemoryError` na duzych tabelach.
- **Watki** — `ImageProcessor` (faza 2) i `NotifierAdapter` (faza 2) tworza dzis nieograniczona liczbe watkow OS; pula ogranicza to do przewidywalnego poziomu.

## Migration Notes

Konsumenci pozostaja na 5.0 do momentu, w ktorym kazdy z nich wykona wlasna migracje. `hop`, `portal` i `importer` nie nazywaja dzis wersji commons — biora `${project.parent.version}`, czyli wlasna wersje 5.0. Zeby wziac 6.0, musza albo podbic sie do 6.0, albo wprowadzic wlasciwosc `homeportal.commons.version` wzorem `hac` (`hac/pom.xml:32`). Rekomendacja: wlasciwosc — odwiazuje cykl wydawniczy konsumenta od commons.

Kolejnosc wdrozenia dla pojedynczego konsumenta: podniesienie wersji → dostosowanie do zmian API z `migration-6.0.md` → wdrozenie kodu → pelny reindeks → weryfikacja filtrow.

## References

- Rozpoznanie: `context/changes/commons-refactoring/research.md`
- Zmiana powiazana: `context/changes/jdk17-migration/change.md` — mapa wywolan ginacych w HS6 jest w sekcji 9 rozpoznania
- Wzorzec harnessu testowego FTS: `homeportal.portal/homeportal-portal-repository/src/test/java/.../offer/OfferTestBase.java`
- Wzorzec wersjonowania commons u konsumenta: `homeportal.hac/pom.xml:32`

## Progress

> Convention: `- [ ]` pending, `- [x]` done. Append ` — <commit sha>` when a step lands. Do not rename step titles. See `references/progress-format.md`.

### Phase 1: Fundament — build i CI

#### Automated

- [x] 1.1 Build przechodzi: `mvn clean install` — 45f2c12
- [x] 1.2 Build niezalezny od locale i encodingu — 45f2c12
- [x] 1.3 Brak ostrzezen o niewersjonowanych pluginach — 45f2c12
- [x] 1.4 Jedna wersja Spring Framework w drzewie zaleznosci — 45f2c12
- [x] 1.5 Workflow CI zielony na branchu — 45f2c12

#### Manual

- [ ] 1.6 Log z commons pojawia sie w aplikacji konsumenckiej (binding slf4j)
- [x] 1.7 Polskie znaki poprawne po buildzie na innym domyslnym encodingu — zweryfikowane 2026-08-29: `mvn clean install -Dfile.encoding=ISO-8859-1 -Duser.language=en -Duser.country=US` zielony, `javap -c Market.class` pokazuje `String wtórny`

### Phase 2: Bugfixy poza FTS

#### Automated

- [x] 2.1 Wszystkie testy przechodza: `mvn clean install` — 930ca8d
- [x] 2.2 Kazda poprawka ma test czerwony bez niej — 930ca8d
- [x] 2.3 Brak `printStackTrace` poza `FullTextRepositoryImpl` — 930ca8d
- [x] 2.4 Test `ClassFieldReader` z polem `null` — 930ca8d
- [x] 2.5 Test `stripInvalidXmlCharacters` zachowuje pierwszy znak — 930ca8d

#### Manual

- [ ] 2.6 Bledna wysylka maila loguje blad, nie sukces
- [ ] 2.7 Plik bez rozszerzenia daje czytelny blad zamiast wyjatku indeksu
- [ ] 2.8 Hasla ASCII weryfikuja sie po zmianie `MD5Encoder`

### Phase 3: `commons-domain` i bugfixy domenowe

#### Automated

- [x] 3.1 Build przechodzi: `mvn clean install` — c2451c7
- [x] 3.2 `-mail` nie ciagnie ORM/Lucene — c2451c7
- [x] 3.3 `-domain` bez zaleznosci ORM/Lucene/Spring Data — c2451c7
- [x] 3.4 Test: `forRentObject`/`forSaleObject` niepuste, `forRentLand` zwraca cechy gruntow — c2451c7
- [x] 3.5 Test: round-trip wartosci wielokrotnych zachowuje wszystkie wartosci — c2451c7
- [x] 3.6 Test: `toFeatures` deterministyczne — c2451c7

#### Manual

- [ ] 3.7 Filtry cech dla ofert typu "obiekt" pokazuja pozycje
- [ ] 3.8 Filtry dla gruntow pokazuja cechy gruntow
- [x] 3.9 `hac` kompiluje sie z `commons-domain` bez `<exclusions>` — zweryfikowane 2026-08-29 probnie (pom przywrocony): build hac na JDK 17 zielony, `dependency:tree` bez `hibernate-search`/`lucene`

### Phase 4: FTS — kontrakt kodowania

#### Automated

- [x] 4.1 Build przechodzi: `mvn clean install` — a8fc37e
- [x] 4.2 Test zgodnosci kodowania dla kazdej stalej `QueryParameter` — w commons w postaci parity encoder↔bridge dla wszystkich czterech kodowan i zestawu wartosci brzegowych (`EncodingContractTest`); weryfikacja per stala enuma nalezy do migracji konsumenta, bo enumy `QueryParameter` zyja w hop i portal — a8fc37e
- [x] 4.3 Test: brak przepelnienia dla wartosci `> Integer.MAX_VALUE` — a8fc37e
- [x] 4.4 Test: porzadek wartosci ujemnych — a8fc37e
- [x] 4.5 Test: znaki specjalne Lucene nie wywracaja parsera — a8fc37e
- [x] 4.6 Test: zakres jednostronny — a8fc37e

#### Manual

- [ ] 4.7 Filtr po cesze z polskim znakiem zwraca wyniki (po reindeksie)
- [ ] 4.8 Filtr po miescie dwuczlonowym zwraca wyniki
- [ ] 4.9 Filtr "cena od" zwraca oferty powyzej 10 mln PLN
- [x] 4.10 Czas pelnego reindeksu zmierzony i zapisany — pomiar z produkcji hopa 2026-08-02: 554 s dla 4 459 830 dokumentow, zapisany w `migration-6.0.md` §6

### Phase 5: FTS — API repozytorium i paginacja

#### Automated

- [x] 5.1 Build przechodzi: `mvn clean install` — 9899ba7
- [x] 5.2 Brak typow Lucene/HS w `FullTextRepository` (zostaly wylacznie wzmianki w javadocu; `SearchQuery` uzywa `QueryParser.escape` wewnetrznie, poza sygnaturami) — 9899ba7
- [x] 5.3 Test: `getPageItems()` dla zera wynikow zwraca pusta liste — 9899ba7
- [x] 5.4 Test: `Page` roznione numerem strony nie sa rowne — 9899ba7
- [x] 5.5 Test: sortowanie malejace emituje jeden order — 9899ba7
- [x] 5.6 Test: `IndexerMonitor` odrzuca drugie pozyskanie locka — 9899ba7
- [x] 5.7 Test integracyjny FTS (H2 + Lucene RAM) — 9899ba7

#### Manual

- [ ] 5.8 Pusta lista wynikow renderuje sie bez bledu
- [ ] 5.9 Rownolegly reindeks odrzucany
- [ ] 5.10 Sortowanie malejace daje odwrocona kolejnosc

### Phase 6: Domkniecie

#### Automated

- [x] 6.1 Build przechodzi: `mvn clean install` — 4b9c424
- [x] 6.2 `mvn dependency:analyze` bez `Used undeclared` — dla scope compile; zostaje `hamcrest-core` w scope test (przychodzi z `junit`/`hamcrest-all`, deklarowanie go byloby szumem) — 4b9c424
- [x] 6.3 `spring-test` poza compile classpath — razem ze `spring-webmvc`, oba `provided` w `-test` — 4b9c424
- [x] 6.4 Workflow CI zielony — 4b9c424
- [x] 6.5 `migration-6.0.md` pokrywa kazda zmiane lamiaca z faz 3-5 — 4b9c424

#### Manual

- [x] 6.6 Probne podniesienie `hop` do 6.0 kompiluje sie wg dokumentu migracji — spelnione realna migracja: `hop` na `master` (`743c5e9c`) ma `homeportal.commons.version = 6.0` i buduje sie na zielono (9 modulow)
- [x] 6.7 `hac` kompiluje sie z `commons-domain` bez `<exclusions>` — patrz 3.9
