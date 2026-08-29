---
date: 2026-08-01T09:48:51+02:00
researcher: WRAZEN Grzegorz
git_commit: 346bd663ee2773da551df050edd4c1bdd6621c88
branch: commons-refactoring
repository: homeportal.commons
topic: "Ogolny przeglad modulow commons, usuniecie bugow, refaktor full-text search (wydanie 6.0)"
tags: [research, codebase, commons-java, commons-data, hibernate-search, lucene, tech-debt, jdk17]
status: complete
last_updated: 2026-08-01
last_updated_by: WRAZEN Grzegorz
---

# Research: przeglad modulow commons, bugi, refaktor full-text search

**Date**: 2026-08-01T09:48:51+02:00
**Researcher**: WRAZEN Grzegorz
**Git Commit**: 346bd663ee2773da551df050edd4c1bdd6621c88
**Branch**: commons-refactoring
**Repository**: homeportal.commons (`pl.homeportal:homeportal-commons:6.0`)

## Research Question

Ogolny przeglad piatki modulow aggregatora (`-java`, `-data`, `-logging`, `-mail`, `-test`), wylapanie bugow oraz rozpoznanie pod refaktor full-text search. Zakres ustalony z uzytkownikiem: bez `geo-api` / `location-api`, nacisk na bugi + architekture FTS + dlug techniczny + wplyw na konsumentow, glebokosc pelna (baza pod `/10x-plan`).

Decyzja podjeta w trakcie: **caly refaktor wychodzi jako wersja 6.0** — bump z 5.0 wykonany w root pom + 5 modulach (`mvn validate` OK), praca idzie na branchu `commons-refactoring`.

## Summary

Repo to 69 klas produkcyjnych, 10 klas testowych, **~14% pokrycia nominalnego (~12% realnego)** i **zero uruchamiania testow w CI** (jedyny workflow to `mvn -B -DskipTests deploy`). Znaleziono kilkadziesiat konkretnych defektow; kilkanascie z nich to bledy dzialajace dzis na produkcji, nie teoretyczne ryzyko.

Cztery wnioski, ktore powinny uksztaltowac plan:

1. **Full-text search nie jest do "refaktoru" w sensie kosmetycznym — jest do przepisania.** Warstwa buduje zapytania Lucene przez konkatenacje stringow, koduje wartosci osobno po stronie indeksu (bridge) i po stronie zapytania (`SearchQuery.normalize`), a te dwa kodowania **nie zgadzaja sie** dla pol z akcentami i dla pol bez `PropertyTypeBridge`. Efekt: czesc filtrow (np. cechy z polskimi znakami, miasta z nazwa dwuczlonowa) zwraca **zawsze zero wynikow, bez bledu**.
2. **HS5 → HS6 jest nieunikniony i zaczyna sie od publicznego API.** `FullTextRepository` wystawia w sygnaturach `org.apache.lucene.search.SortField[]` i `org.hibernate.search.jpa.FullTextQuery`, a interfejs dziedziczy **17 repozytoriow downstream**. Dopoki te typy sa w API, kazdy konsument kompiluje sie przeciw Lucene 5 / HS 5.
3. **Bump wersji 6.0 nie jest lokalny.** `hop`/`portal`/`importer` nie nazywaja wersji commons — uzywaja `${project.parent.version}`, czyli **wlasnej** wersji 5.0. Zeby wziac commons 6.0, musza albo podbic sie do 6.0, albo wprowadzic wlasciwosc `homeportal.commons.version` (wzorzec z `hac`).
4. **`hac` juz uciekl z commons** — ma recznie skopiowane `OfferProduct`/`OfferActivity`/`OfferMarket`/`FeatureType`, bo `commons-data` jest zroniete z javax + Hibernate 5 + Lucene. To najmocniejszy argument za wydzieleniem bezzaleznosciowego modulu domenowego w 6.0.

> Zrodlo ustalen: cztery rownolegle sub-agenty (bug hunt `-java`, architektura+bugi `-data`, audyt dlugu/POM-ow, skan konsumentow). Czesc znalezisk zostala przez agentow **odtworzona wykonaniem kodu** (oznaczone nizej), reszta pochodzi z czytania zrodel — przed poprawka kazde warto przypiac testem regresyjnym.

## Detailed Findings

### 1. Full-text search — kontrakt kodowania jest zlamany

Warstwa: `homeportal-commons-data/src/main/java/pl/homeportal/commons/data/`

Zapytanie powstaje jako lista stringow AND-owanych w `search/SearchQuery.java:110-129`. Kazdy builder (`addParameter`, `addNotParameter`, `addOrParameter`, `addPhraseParameter`, `addRangeParameter`, `addDateRangeParameter`) sam sklada fragment `field:value`. Wartosc jest normalizowana **na sztywno** przez `PropertyTypeBridge` (termy) albo `NumericBridge` (zakresy) — `search/SearchQuery.java:40,154-163`. Strona indeksu wybiera bridge **per pole, adnotacja** w encjach downstream. Nic tych dwoch stron nie spina.

Konsekwencje (najwazniejsze najpierw):

- **Cechy z polskimi znakami nie da sie odfiltrowac.** `search/bridge/FeatureBridge.java:44` zachowuje `\p{L}` (indeksuje `"wtórny"`), a zapytanie strippuje akcenty do `"wtorny"`. Filtr po `FEATURES` zwraca zero trafien, zawsze. (Test `FeatureBridgeTest.java:48` wprost dokumentuje ta asymetrie.)
- **`PropertyTypeBridge` usuwa wszystkie spacje** (`search/bridge/PropertyTypeBridge.java:21-23`), wiec dla pola indeksowanego bez tego bridge'a (np. `City.name` w portalu, `@Field` bez `@FieldBridge`) term `nowysacz` nigdy nie trafi w tokeny `[nowy, sącz]`.
- **`addPhraseParameter` jako jedyny nie normalizuje niczego** (`search/SearchQuery.java:64-70`) — fraza przeciw polu z `PropertyTypeBridge` nie ma szans trafic, a niezescape'owany `"` daje malformed query.
- **Brak escapowania Lucene w ogole** (`search/SearchQuery.java:37-92`): term `x OR product:1` sklei sie do `xORproduct:1`, co parser czyta jako klauzule na polu sterowanym przez uzytkownika. Znaki `[ ~ ^ { }` po prostu rzucaja wyjatek → 500.
- **`NumericBridge` tnie przez `intValue()`** (`search/bridge/NumericBridge.java:30`): cena `3_000_000_000` indeksuje sie jako `-1294967296`, bez paddingu, poza porzadkiem. Padding zerami psuje tez porzadek liczb ujemnych (`:46-56`).
- **`addRangeParameter` nie ma zakresu otwartego**, wiec `hop` wymyslil sentinel `MAX = "9999999"` (`homeportal.hop/.../HopSearchQueryBuilder.java:35,85`) — oferty powyzej ~10 mln PLN wypadaja z wynikow "cena od".

### 2. Full-text search — analizator, sesja, transakcje

`repository/FullTextRepositoryImpl.java`:

- `getAnalyzer(boolean)` (`:229-236`) tworzy `PerFieldAnalyzerWrapper` z **pusta mapa per-field**, wiec degeneruje sie do jednego analizatora dla wszystkich pol wszystkich encji; wlasciwe zrodlo (`getSearchFactory().getAnalyzer(t)`) nie jest uzywane. Do tego kazde wywolanie alokuje nowy `StandardAnalyzer`/`KeywordAnalyzer` (oba `Closeable`) i nigdy go nie zamyka — powolny wyciek pod ruchem.
- **Tylko `indexOne` ma `@Transactional`** (`:181-182`). `save`/`delete`/`deleteAll`/`purge`/`indexAll` nie maja; `flushToIndexes()` pisze do Lucene **przed** commitem, wiec rollback zostawia indeks opisujacy nieistniejace wiersze.
- `save(S)` i `delete(T)` **maja te sama sygnature co `CrudRepository`** i wygrywaja rozstrzygniecie fragmentu Spring Data (`:45-53`, `:56-62`). `repository.save(nowaOferta)` robi `merge` zamiast `persist` (argument zostaje transientny), a `delete` na detached rzuca `IllegalArgumentException` zamiast zrobic `merge`+`remove` jak `SimpleJpaRepository`.
- `deleteAll(Class)` (`:64-74`) laduje cala tabele do pamieci i usuwa wiersz po wierszu.
- `countByIndex` (`:93-101`) robi `e.printStackTrace()` i `return -1`; sentinel nie jest sprawdzany przez zadnego wolajacego, a klasa **nie ma loggera**.
- `createPageable` (`:242-249`) — brak `else`: dla pola z `reverse` dodaje `DESC`, a potem **zawsze** `ASC`. `getSort` (`:253-260`) i tak zwraca tylko pierwszy order.

### 3. Paginacja (`pageable/`)

- `Page extends org.springframework.data.domain.PageRequest` (`pageable/Page.java:13`) i **przesłania** pola `page`/`size`/`sort`, wolajac `super(1, 20, ...)`. Wiec `equals`/`hashCode`/`getOffset`/`next()` czytaja stan nadklasy: dwa `Page` roznione tylko `setPage` sa **rowne**. Do tego jest 1-based przy 0-based Springu — konwersja `-1` powtorzona w trzech miejscach (`search/SearchQueryBuilder.java:16`, `repository/FullTextRepositoryImpl.java:134`, `portal/.../SearchQueryBuilder.java:427`).
- `PageItems.getPageItems()` **zwraca `null` przy zerowej liczbie wynikow** (`pageable/PageItems.java:55-87,157-164`) — pusta lista wynikow to NPE w widoku; wolane bezwarunkowo w `portal/.../AbstractListController.java:124`.
- `PageItems.pageableToUri` (`:94-143`) refleksja siega po prywatne pola `PageRequest` z `setAccessible(true)` i lapie `catch (Exception) { return null; }` bez logowania. Na JDK 17 to `InaccessibleObjectException` — **po cichu**, linki pagera zamienia sie w `"."`.

### 4. Bugi domenowe (`-data/model`)

- **`FeatureTypeProvider` ma cztery bledy copy-paste, dzialajace dzis**: `forRentLand()` zwraca `forRentHall` (`:313-316`); `initForRentObject()` wypelnia `forRentOffice` (`:152-174`) → `forRentObject()` **zawsze pusty**; to samo dla sprzedazy (`:272-291`) → `forSaleObject()` **zawsze pusty**; `FLOOR_QUANTITY` dodany dwa razy do `forSaleHouse` (`:219-220`). Wszystkie gettery oddaja zywe, mutowalne statyczne `LinkedList`.
- **`FeatureConverter` gubi wartosci wielokrotne**: `extractValues` (`:76-79`) robi `split("^")` — w Javie `^` to zero-width anchor, wiec string **nie jest dzielony**; niezaleznie `toFeatureMap` (`:39`) bierze i tak tylko `values[0]`. `MEDIA:prąd^woda^gaz` przezywa jako jedna wartosc. Kolejnosc wyjscia `toFeatures` (`:26,53`) zalezy od `HashMap` → re-serializacja brudzi wiersz i wymusza re-indeks.
- `SearchQuery.isSortEmpty()` (`:105-108`) zwraca **odwrotnosc** swojej nazwy (`sortFields.isEmpty() ? false : true`). Nikt jej nie wola — jedyny powod, dla ktorego to jeszcze nie wybuchlo.

### 5. Bugi w `-java` (fundament, 28 klas, 5 testow)

Wysokie (czesc odtworzona wykonaniem):

- `reflection/ClassFieldReader.java:15-16` — `Collectors.toMap` na polu `null` → **NPE**; istniejacy test uzywa obiektu w pelni wypelnionego, wiec nigdy tego nie lapie.
- `text/StringUtils.java:41-65` — `stripInvalidXmlCharacters` **usuwa pierwszy znak kazdego wejscia** (`"Mieszkanie"` → `"ieszkanie"`), NPE na `null`. Metoda jest nietestowana i opiera sie na internalu Xercesa (`org.apache.xerces.util.XMLChar`, `:3`) — jedyny powod, dla ktorego `xercesImpl` (+ JPMS-wrogi `xml-apis`) jest w zaleznosciach.
- `zip/ZipEntryExtractor.java:29-35,37-47` — `ZipFile` nigdy nie zamykany (wyczerpanie deskryptorow w petli importera), `isAvailable` zwraca `true` dla **dowolnego** czytelnego pliku.
- `exception/HomeportalServiceException.java:39` — `getMessage()` **NPE** gdy uzyto konstruktora bezargumentowego; wybucha wewnatrz handlera bledu, maskujac oryginalna przyczyne.
- `security/MD5Encoder.java:22-23,41-44` — domyslny charset platformy (haslo z `ł`/`ż` daje inny skrot po zmianie JVM/OS), `null` przy `NoSuchAlgorithmException`. `security/PasswordGenerator.java:19-30` — `Math.random()` (LCG, przewidywalny) do generowania hasel.
- `image/ImageProcessor.java:135-143` — `substring(0, -1)` dla nazwy bez kropki; `:44-61,89` — spin loop na **nie-`volatile`** fladze z pustym `catch`, jeden watek OS na obrazek.
- `datetime/DateTimeUtils.java:16-17,70-83` — `MONTH = DAY*31`, `YEAR = 372 dni`: `todayPlusYears(1)` myli sie o 7 dni, a `todayMinusMonths` (poprawne, na `LocalDateTime`) nie jest odwrotnoscia `todayPlusMonths`.
- `i18n/Language.java:13` — Ukrainski jako `"ua"`; ISO 639-1 to `"uk"`, wiec `getByValue("uk")` zwraca `null`, a `messages_uk.properties` nigdy sie nie rozwiazuje.

### 6. Dlug techniczny i POM-y

- **Kodowanie zrodel nie jest ustawione** (`pom.xml`, brak `project.build.sourceEncoding`), a 24 pliki maja polskie znaki (m.in. `Market.java:12` → `"wtórny"`). Build na maszynie nie-UTF-8 cicho psuje te literaly — i to te same literaly, ktore decyduja o dopasowaniu w Lucene.
- **Trzy wersje Springa w jednym buildzie**: `spring-context` 5.1.4 (`pom.xml:38`), `spring-test` 5.2.9 (`:47`), a `spring-data-jpa` 2.2.10 ciagnie `spring-core`/`beans`/`aop` w 5.2.9 na glebokosci 2 (wygrywaja z pinowanym 5.1.4). To klasyczna konfiguracja pod `NoSuchMethodError`. Lek: `spring-framework-bom` + `spring-data-bom`.
- **`slf4j-api 1.8.0-beta2`** (`pom.xml:23`) — beta linii, ktora nigdy nie wyszla GA, z inna metoda bindowania niz 1.7; w reaktorze **nie ma zadnego bindingu**. Realnie: logi z commons moga byc NOP-em u konsumentow.
- **Trojka HS/ORM/Lucene jest wewnetrznie spojna** (HS 5.5.4 wymaga Lucene `[5.3,5.4)` i ORM `[5.0,5.1)` — zgadza sie), tylko zamrozona w 2015/2016. Kazdy ruch w ORM wymusza HS6.
- **`-logging` zalezy od `-data`** przez jeden import (`LoggingSupport.java:5` → `AbstractEntity`). Dlatego `-mail` ciagnie Hibernate, Hibernate Search i Lucene, zeby wyslac maila. Jeden import do rozciecia.
- Nieuzywane zaleznosci: `commons-io`, `commons-compress` (**1.0, z 2009**), `commons-email` w `-java`, oba `javax.el`, `hibernate-validator` w `-logging`, `spring-webmvc` + `junit` w `-test`. Uzywane, ale **niezadeklarowane**: `javax.mail`, `commons-collections` (przychodzi jako `provided` tranzytywnie przez Velocity → `NoClassDefFoundError` u konsumenta, ktory wezmie sam `-mail`).
- `-test` publikuje `MvcTestUtils` w `src/main` z `spring-test` w scope `compile` → testowy kod Springa lezy na produkcyjnym classpathie konsumentow. `SessionMockProvider` (mock!) jedzie w produkcyjnym jarze `-mail`.
- **CI nie uruchamia testow** (`.github/workflows/publish.yml:37` → `mvn -B -DskipTests deploy`), a GitHub Packages nie pozwala nadpisac wydanej wersji — stad wymog bumpu wersji przy kazdej zmianie.

### 7. Bugi w `-mail` i `-logging` (0% pokrycia)

- `NotifierAdapter` — `fork` jest **stanem instancji** (`:28`) ustawianym przez `notify(dto,false)` (`:49`) i nigdy nie przywracanym. Na singletonie Springa jedno wywolanie synchroniczne trwale przelacza wszystkie kolejne. Do tego `new Thread(...).start()` na kazdy mail (`:85-87`).
- `VelocityEmail.send()` (`:227-231`) lapie `Exception` i zwraca `null`, po czym `NotifierAdapter:95-96` loguje **`"Email sent. ... response: null"`** — nieudana wysylka jest logowana jako sukces.
- 4 z 11 szablonow `.vm` odwoluja sie do zmiennych (`$user`, `$newPassword`, `$resetLink`), ktorych `VelocityEmail.model()` (`:211`) nigdy nie wklada do kontekstu — nie da sie ich wyrenderowac.
- `LoggingSupport`: 10 publicznych stalych-szablonow (`INFORMATION_SAVE`, `ERROR_DELETE`, ...) nie ma **zadnego** odwolania ani w repo, ani downstream; `logWithoutExceptionForSaveOrUpdate` (`:196-199`) i `...ForDelete` (`:207-210`) przyjmuja `Exception` i go nie loguja.

### 8. Wplyw na konsumentow (co zlamie zmiana API)

Cztery repozytoria Java konsumuja commons; `spy` nie (jedyne powiazanie to recznie synchronizowany katalog cech wzgledem `FeatureType`).

Najczesciej importowane typy: `LoggingSupport` (343 importy), `Constants` (141), `DateTimeUtils` (112), `ControllerUtils` (87), `Language` (67), `Product` (54), `Activity` (52), `FeatureType` (50), `AbstractEntity` (46). Surowych wystapien `FeatureType` — **1013**, 55 z 68 stalych uzywanych downstream.

**~140 klas downstream dziedziczy po typach commons** — to najkruchsze sprzezenie:

| Baza w commons | Liczba potomkow |
|---|---|
| `AbstractEntity` (`@MappedSuperclass`) | 45 |
| `NotifierAdapter` | 22 |
| `FullTextRepository` (interfejs Spring Data) | 17 |
| `AbstractScheduler` | 15 |
| `FormAwareController` | 14 |
| `ISale` / `IRent` | po 12 |
| `AbstractForm` | 11 |
| `Page` (rozszerza `PageRequest`) | 6 |

Sprzezenia, ktorych nie widac w importach:

1. **Nazwa pakietu jako konfiguracja** — wszystkie trzy aplikacje Springowe rejestruja `@EnableJpaRepositories(basePackages = {..., "pl.homeportal.commons.data.repository"})` (np. `portal/.../PortalRepositoryConfiguration.java:7`). Przeniesienie/zmiana nazwy pakietu `data.repository` psuje sie **w runtime, nie przy kompilacji**.
2. **`FeatureType` jest zapisany w bazie** — `portal/.../FeatureTranslation.java:31-32` ma `@Enumerated(EnumType.STRING)`. Zmiana nazwy stalej to migracja danych.
3. **`Page` jest kontraktem REST hop-a** — `HopSearchRequest extends Page` bindowany jako `@ModelAttribute` (`hop/.../OffersController.java:68,83`), wiec `page`/`size`/`sort`/`reverse` to **nazwy parametrow HTTP** publicznego API.
4. **`Product`/`Activity` generuja kanoniczne URL-e SEO** (`portal/.../WebOffer.java:559,564`).
5. **Bridge'e definiuja schemat indeksu Lucene** — zmiana formatu wyjscia uniewaznia istniejacy indeks az do pelnego reindeksu.

Sygnaly, ze API commons uwiera:

- **`hac` skopiowal model** (`hac/.../OfferProduct.java`, `OfferActivity`, `OfferMarket`, `FeatureType`) i musi wykluczac `commons-data` z tranzytywnego drzewa `commons-logging` (`hac/pom.xml:87-98`), bo jego stack (Hibernate 6 / jakarta) nie da sie pogodzic z commons-data.
- **`portal` ma wlasny `SearchQueryBuilder`** (`portal/.../common/service/SearchQueryBuilder.java:44`, 474+ linii), ktory **nie** dziedziczy po abstrakcji z commons — ta abstrakcja jest realnie uzyta 1 raz (w `hop`).
- **`hop` zdefiniowal `Page` dwa razy wlasnorecznie** jako DTO odpowiedzi, bo commons `Page` niesie semantyke zadania (dziedziczy po `PageRequest`).

### 9. Blokery JDK 17 (dla zmiany `jdk17-migration`, nie tej)

Kolejnosc wymuszona zaleznosciami: **Lombok 1.16.14** (`pom.xml:29`, nie kompiluje sie na JDK 16+ w ogole — dlatego CI stoi na JDK 8) → **AspectJ 1.9.2** (`:43`) → **Spring 5.1.4** (JDK 17 dopiero od 5.3) → **Hibernate ORM 5.0.10 + HS 5.5.4 + Lucene 5.3.1** → **javax → jakarta** (21 miejsc importu w 11 plikach). Do tego `setAccessible(true)` w `ClassFieldReader.java:23` i `PageItems.java:103`, `xml-apis` w split-package z `java.xml`, oraz `new Long(...)`/`new Double(...)` w `FullTextRepositoryImpl.java:110`, `SearchQuery.java:157`, `NumericBridgeTest.java:17`.

Konkretne wywolania, ktore **znikaja** w Hibernate Search 6 (mapa pod migracje): `Search.getFullTextEntityManager` (`FullTextRepositoryImpl.java:11-13,47,58,67,79,185,210,226`), `.index()`/`.flushToIndexes()`/`.purge()`/`.purgeAll()` (`:49,50,60,61,72,73,80,81,187,188`), `.createIndexer()`/`.optimizeOnFinish()`/`getSearchFactory().optimize()` (`:168,172,194`), `createFullTextQuery(luceneQuery, Class)` (`:211`), caly pakiet `org.hibernate.search.bridge.*` (4 bridge'e + `@FieldBridge` w ~20 polach encji downstream), oraz `QueryParser.setLowercaseExpandedTerms` (`:208`) — usuniete juz w Lucene 6. Plus: `org.hibernate.search.query.dsl` i `ProjectionConstants` **nie sa uzywane nigdzie** — powierzchnia migracji jest skupiona w dwoch plikach repozytorium i czterech bridge'ach.

## Code References

- `homeportal-commons-data/src/main/java/pl/homeportal/commons/data/repository/FullTextRepositoryImpl.java:202-236` — jedyne miejsce wykonania surowego Lucene: parser, analizator, sort, polityka wyjatkow
- `homeportal-commons-data/src/main/java/pl/homeportal/commons/data/repository/FullTextRepository.java:4,38` — `FullTextQuery` i `SortField[]` w publicznym API (glowny bloker HS6)
- `homeportal-commons-data/src/main/java/pl/homeportal/commons/data/search/SearchQuery.java:37-92,154-163` — szescio-krotnie zduplikowane sklejanie zapytania + zaszyte kodowanie wartosci
- `homeportal-commons-data/src/main/java/pl/homeportal/commons/data/search/bridge/` — `FeatureBridge:44`, `PropertyTypeBridge:21-23`, `NumericBridge:30,46-56`, `DateBridge:16`
- `homeportal-commons-data/src/main/java/pl/homeportal/commons/data/pageable/Page.java:13-52` i `PageItems.java:55-143`
- `homeportal-commons-data/src/main/java/pl/homeportal/commons/data/model/feature/FeatureTypeProvider.java:152-174,219-220,272-291,313-316`
- `homeportal-commons-logging/src/main/java/pl/homeportal/commons/logging/LoggingSupport.java:5` — import, ktory wiaze `-logging` z `-data`
- `homeportal-commons-mail/src/main/java/pl/homeportal/commons/mail/NotifierAdapter.java:28,49` i `VelocityEmail.java:227-231`
- `pom.xml:21-23,29,32-38,43,47` — poziom Javy, slf4j beta, Lombok, trojka HS/ORM/Lucene, rozjazd Springa
- `.github/workflows/publish.yml:26-30,37` — komentarz o Lomboku i deploy z `-DskipTests`

## Architecture Insights

- **Kodowanie wartosci jest kontraktem, ktory nigdzie nie jest zapisany.** Strona indeksu wybiera bridge adnotacja w encji downstream, strona zapytania — na sztywno w `SearchQuery`. Naturalne miejsce na ten kontrakt to `QueryParameter`: kazdy parametr powinien deklarowac swoj encoder, a test powinien sprawdzac `queryEncoding(param) == indexBridge(field)` dla kazdej stalej enuma. To jest tez dokladnie ksztalt, ktorego chce `ValueBridge` z HS6 — jedna zmiana obsluguje i poprawnosc, i migracje.
- **`FullTextRepository` jako fragment Spring Data z nazwami `save`/`delete`/`findAll` przejmuje metody `CrudRepository`** — to nie jest rozszerzenie, to przeslonieciecie, niewidoczne w miejscu wywolania. Zmiana nazw (`indexedSave`, `indexedDelete`, `searchAll`) jest mechaniczna i zdejmuje cala klase pulapek.
- **Powtarzajacy sie wzorzec: bledy sa ciche.** `printStackTrace` + `return -1`, `catch (Exception) { return null }`, `return false`, `null` z `getPageItems()`, logowanie sukcesu przy nieudanej wysylce maila. Siedem miejsc polykajacych wyjatki. Hierarchia `Homeportal*Exception` istnieje i jest uzywana w **dwoch** klasach; `-data` nie uzywa jej wcale.
- **Cztery z szesciu najwiekszych plikow to jednoczesnie pliki o najwiekszym zageszczeniu bledow** (`FeatureTypeProvider` 352, `LoggingSupport` 271, `VelocityEmail` 264, `FullTextRepositoryImpl` 261 linii). Korelacja jest dokladna — duplikacja copy-paste jest tu nosnikiem bledow, nie tylko brzydota.
- **`-java` siedzi w korzeniu przestrzeni nazw** (`pl.homeportal.commons.text`, `.datetime`, ...), gdy pozostale moduly maja segment modulu. Kolizje nazw z JDK/frameworkiem: `commons.file.Files` vs `java.nio.file.Files`, `commons.text.StringUtils` vs `org.apache.commons.lang3.StringUtils` (statycznie importowany w tym samym pliku), `commons.data.search.bridge.DateBridge` vs `org.hibernate.search.annotations.DateBridge`.

## Historical Context (from prior changes)

Brak — `context/changes/` i `context/archive/` byly puste przed ta zmiana (szkielet `context/` powstal w commicie `dc5145d`). Jedyny wczesniejszy zapis kontekstu to `commons.md` (commit `850846a`), ktory nadal podaje wersje 5.0 i wymaga aktualizacji przy tym refaktorze.

## Related Research

- `context/changes/jdk17-migration/change.md` — osobna zmiana; sekcja 9 tego dokumentu jest jej wejsciem. Kolejnosc jest wymuszona: HS5→HS6 jest **wspolna czescia** obu zmian i nie da sie jej odlozyc za jakarta.

## Open Questions

1. **Czy 6.0 ma byc kompatybilne wstecznie?** Konsumenci stoja na 5.0 przez `${project.parent.version}` — albo `hop`/`portal`/`importer` ida do 6.0 w jednym kroku, albo dostaja wlasnosc `homeportal.commons.version`. To decyzja, ktora determinuje, czy w 6.0 wolno zmieniac sygnatury (`save`→`indexedSave`, usuniecie `FullTextQuery` z API).
2. **Czy wydzielic `commons-domain`** (Product/Activity/Market/FeatureType/interfejsy, zero zaleznosci ORM)? To pozwoliloby `hac` skasowac kopie i przestac wykluczac `commons-data`. Duzy zysk, ale zmienia koordynaty pakietow — a te sa u konsumentow w `basePackages`.
3. **Zakres HS6 w tej zmianie.** Refaktor FTS z sekcji 1-2 da sie zrobic na HS5 (kontrakt kodowania, analizator, transakcje, nazwy metod) i to jest dobre przygotowanie pod HS6. Pelne przejscie na HS6 wymaga bumpu ORM, czyli wchodzi w `jdk17-migration`. Do potwierdzenia, czy 6.0 = "porzadek na HS5", czy "od razu HS6".
4. **35 klas bez odwolan w tym repo** — skan konsumentow pokryl `hac`/`hop`/`portal`/`importer`/`spy`, ale nie inne ewentualne konsumenty; przed kasowaniem warto potwierdzic (bezpiecznie zerowe downstream: `Notifier`, `VelocityEmail`, `SessionMockProvider`, `PageItem`, `SortFieldAware`, `FullTextRepositoryImpl`, `FeatureConstants`, `ImageResizer`, oba aspekty).
5. **Czy `FeatureType`/`FeatureTypeProvider` ma zrodlo prawdy poza kodem?** `spy` synchronizuje katalog cech recznie, a `portal` trzyma nazwy stalych w bazie — poprawka pustych list `forRentObject()`/`forSaleObject()` moze zmienic to, co widzi uzytkownik na filtrach.
