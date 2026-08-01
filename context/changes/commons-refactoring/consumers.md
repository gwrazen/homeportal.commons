# Mapa uzycia commons u konsumentow

Zywy dokument: uzupelniany przy kazdej fazie, **zanim** zmieni sie dane API. Sluzy dwom celom —
sprawdzeniu, czy poprawka nie lamie konsumenta, i jako material zrodlowy dla `migration-6.0.md` (faza 6).

Skanowane repozytoria: `homeportal.portal`, `homeportal.hop`, `homeportal.importer`, `homeportal.hac`,
`homeportal.spy` (spy nie ma zadnego powiazania kodowego — tylko recznie synchronizowany katalog cech).

Skladnia werdyktu:
- **bezpieczne** — zachowanie u konsumenta nie zmienia sie
- **zmiana zachowania** — konsument zobaczy roznice; opis w kolumnie
- **do sprawdzenia** — jeszcze nieprzeskanowane

---

## Faza 2 — `-java`, `-mail`, `-logging`

### `zip/ZipEntryExtractor`

| Konsument | Miejsce | Na czym polega |
|---|---|---|
| importer | `homeportal-importer-worker-galactica/.../GalacticaImportAdapter.java:302` | `extract(OFFERS_XML_FILE, pkg)` → `IOUtils.toString(is, UTF_8)`, potem jawne `is.close()` |
| importer | `.../processor/OfferProcessor.java:377` (`getImageContent`) | `extract(...)` → `IOUtils.toByteArray(is)`, **bez `close()`** |
| importer | `.../processor/ResourcesProcessor.java:66` | `extract(...)` w petli po zasobach, w `try/catch`, **bez `close()`** |
| importer | `.../processor/AgentProcessor.java:31` | import statyczny `extract` |
| importer | `GalacticaImportAdapter.java:291` | `isAvailable(packageName)` jako bramka przed przetwarzaniem paczki |

**Werdykt: zmiana zachowania (akceptowana).** Wpis jest teraz wczytywany w calosci do `ByteArrayInputStream`,
a `ZipFile` zamykany w `try-with-resources`. Wariant alternatywny (strumien zamykajacy archiwum w `close()`)
nie naprawilby wycieku w `OfferProcessor` i `ResourcesProcessor`, bo one nigdy nie wolaja `close()` — a to
wlasnie one lecą w petli i wyczerpuja deskryptory. Koszt: przejsciowo dwie kopie zawartosci w pamieci.
Potwierdzone z uzytkownikiem 2026-08-01: paczki galactiki sa male, koszt do zaakceptowania.
Dodatkowo `extract` rzuca teraz `HomeportalServiceException` przy braku wpisu zamiast NPE.

### `text/StringUtils.stripInvalidXmlCharacters`

| Konsument | Miejsce | Na czym polega |
|---|---|---|
| importer | `GalacticaImportAdapter.java:304` | wejsciem jest tresc `oferty.xml`, czyli string zaczynajacy sie od `<`; linia 305 dodatkowo robi `replaceFirst("^([\\W]+)<", "<")` |

**Werdykt: bezpieczne.** Stary kod zachowywal pierwszy znak wylacznie wtedy, gdy byl nim `<` — czyli
dokladnie w przypadku XML-a. Poprawka (zachowanie kazdego poprawnego znaku) daje dla tego wejscia identyczny
wynik, a ewentualny BOM i tak usuwa `replaceFirst` w nastepnej linii.

### `text/StringUtils.normalize`

22 miejsca uzycia (portal 19, importer 2, hac 1) — generowanie slugow i URL-i.
**Werdykt: bezpieczne.** Regex bez zmian; przemianowana zostala tylko prywatna stala
(`NOT_ALPHANUMERIC` → `NOT_ALPHABETIC`), bo usuwa takze cyfry. Zmiana samego regexu zmienilaby juz
zaindeksowane URL-e — swiadomie poza zakresem.

### `reflection/ClassFieldReader.readFieldValues`

| Konsument | Miejsce | Na czym polega |
|---|---|---|
| importer | `homeportal-importer-configuration/.../ApplicationConfiguration.java:135` | `readFieldValues(this).forEach((k,v) -> log(k,v))` |
| portal | `homeportal-portal-configuration/.../ApplicationConfiguration.java:425` | jw. |
| hop | `homeportal-hop-configuration/.../ApplicationConfiguration.java:66` | jw. |

**Werdykt: zmiana zachowania (zyskowna).** Zadna z trzech klas nie dziedziczy, wiec dodane przejscie po
nadklasach nic tu nie zmienia. Pola `@Value` bywaja `null` — to byl zrodlem NPE, ktory poprawka usuwa.
Widoczna roznica: pola **statyczne** sa teraz pomijane, wiec z logu konfiguracji znikna stale w rodzaju `LOG`.

### `file/Files`

| Konsument | Miejsce | Na czym polega |
|---|---|---|
| importer | `homeportal-importer-service/.../CleanerService.java:51,53,55` | `deleteFiles(dir, Pattern.compile("oferty_%s\\d*_\\d*.zip"))` |
| portal | `homeportal-portal-service/.../CompanyService.java:369` | `deleteDirectory(katalog agencji)` |
| portal | `homeportal-portal-management/.../PortalsTempCleanScheduler.java:16` | import statyczny `deleteDirectory` |

**Werdykt: bezpieczne.** Wzorzec importera pasuje wylacznie do plikow `.zip`, nigdy do nazw katalogow,
wiec przestawienie kolejnosci (katalog sprawdzany przed wzorcem) nie zmienia tam niczego. Poprawka ma
znaczenie dopiero dla wzorca pasujacego do katalogu — takiego uzycia dzis nie ma. Nowoscia sa logi `WARN`,
gdy `delete()` zwroci `false` (wczesniej log klamal, ze plik zostal usuniety).

### `security/MD5Encoder.createMD5Hash`

| Konsument | Miejsce | Na czym polega |
|---|---|---|
| portal | `homeportal-portal-service/.../UserService.java:528` | `createMD5Hash(user.getEmail(), key)` |
| importer | `homeportal-importer-service/.../UserService.java:355` | identyczne wywolanie (duplikat) |

**Werdykt: bezpieczne w praktyce.** Danymi wejsciowymi sa adres e-mail i klucz z konfiguracji — w obu
przypadkach ASCII, dla ktorego UTF-8 i dowolny domyslny charset daja te same bajty. Zmiana ma znaczenie
tylko dla znakow spoza ASCII. Do wyroznienia w dokumencie migracji.

### `security/PasswordGenerator.generate`

| Konsument | Miejsce | Do czego |
|---|---|---|
| portal | `.../web/mvc/form/RegistrationForm.java:116` | `apiKey` przy rejestracji |
| portal | `.../management/mbean/security/ApiKeyManager.java:42` | regeneracja klucza API z JMX |
| portal | `.../service/UserService.java:188, :555` | reset i generowanie hasla uzytkownika |

**Werdykt: zmiana zachowania (zyskowna).** Konsumenci nie polegaja na konkretnym rozkladzie znakow,
tylko na dlugosci. Przejscie z `Math.random()` na `SecureRandom` zmienia jedynie zrodlo losowosci —
istotne, bo z tych wartosci powstaja klucze API i hasla resetu.

### `datetime/DateTimeUtils`

| Metoda | Konsumenci | Uwaga |
|---|---|---|
| `todayMinusMonths` | portal `OfferService.java:548` (wygasanie ofert), importer `CleanerService.java:74,83`, hop `Request.java:200` | juz dzis poprawna (`LocalDateTime.minusMonths`) — bez zmian |
| `todayMinusYears` | hop `Request.java:207`, testy integracyjne hop-client | **bledna** (rok = 372 dni); poprawka przesuwa granice filtra o ~7 dni na rok |
| `todayPlusMonths`, `todayPlusYears` | **zero uzyc downstream** | poprawka bez ryzyka |

**Werdykt: zmiana zachowania, waska.** Jedyny realny konsument bledu to `Request.addedmin(todayMinusYears(n))`
w kliencie hop — filtr "dodane od" przesunie sie o ~7 dni na kazdy rok wstecz, w strone poprawnej daty.

### `i18n/Language`

| Konsument | Miejsce | Na czym polega |
|---|---|---|
| portal | `homeportal-portal-service/.../NotificationService.java:60` | `getByLocale(locale)` przy wyborze jezyka powiadomienia |

`Language.UKRAINIAN` nie jest uzywany bezposrednio w zadnym z repozytoriow.
**Werdykt: zmiana zachowania (zyskowna), wymaga aliasu.** Po zmianie `"ua"` → `"uk"` przegladarka
wysylajaca `Accept-Language: uk` zacznie byc rozpoznawana (dzis `getByValue("uk")` zwraca `null`).
Alias dla starej wartosci `"ua"` musi zostac na stale, bo moze byc utrwalona w bazie konsumenta.

### `image/ImageProcessor`

| Konsument | Miejsce | Na czym polega |
|---|---|---|
| portal | `.../web/mvc/controller/wizard/EWizardStepPhotosController.java:184` | `new ImageProcessor()` + `add(...)` przy uploadzie zdjec |
| importer | `.../processor/OfferProcessor.java:310` | `new ImageProcessor()` w petli po zdjeciach oferty |

**Werdykt: do potwierdzenia przy implementacji** — zamiana watku na `ExecutorService` nie zmienia API
(`new ImageProcessor()` + `add`), ale zmienia model wykonania. Oba miejsca tworza procesor lokalnie
i nie wspoldziela go miedzy watkami.

### `mail/NotifierAdapter`

22 klasy dziedziczace (portal 21, importer 1). Konsumenci wolaja `notify(dto)`; wariant `notify(dto, false)`
nie wystepuje w kodzie produkcyjnym konsumentow — testy portalu weryfikuja tylko `notify(any())`.
**Werdykt: bezpieczne.** Usuniecie pola `fork` ze stanu instancji nie zmienia sygnatur uzywanych downstream.

### `logging/LoggingSupport.logWithoutExceptionForSaveOrUpdate` / `...ForDelete`

| Konsument | Miejsce |
|---|---|
| portal | `.../service/ResourceService.java:58, :91, :104`, `LocationService.java:36`, `IdentityService.java:66`, `ImportService.java:118` |

**Werdykt: bezpieczne.** Sygnatury zostaja; zmienia sie tylko to, ze przekazany wyjatek trafia
faktycznie do logu (dzis parametr jest ignorowany).

### `exception/Homeportal*Exception`

`HomeportalSecurityException` jest w portalu wolany wylacznie 4-argumentowym konstruktorem
(`DepartmentFormValidator.java:55,70`, `ResourcesController.java:675,683,709`, `PortalsController.java:538,566,572,587`,
`AgencyController.java:189`). Konstruktora bezargumentowego `HomeportalServiceException` nikt downstream nie uzywa.
**Werdykt: bezpieczne.** Dodanie konstruktora z przyczyna i `serialVersionUID` niczego nie lamie.

---

---

## Faza 3 — `commons-domain`, model domenowy, rozciecie `-logging`

### `logging/LoggingSupport` (343 uzycia downstream)

**Werdykt: bezpieczne w kodzie zrodlowym, niezgodne binarnie.** Ograniczenie generyczne zmienilo sie
z `<T extends AbstractEntity>` na `<T extends Identifiable>` (nowy interfejs w `-domain`, implementowany
przez `AbstractEntity`). Kazde wywolanie downstream kompiluje sie bez zmian — encje konsumentow dziedzicza
po `AbstractEntity`, wiec spelniaja nowy kontrakt. Zmienia sie erasure, wiec konsument **musi sie
przekompilowac** (i tak musi, bo 6.0 jest wydaniem lamiacym).

### `model/feature/FeatureTypeProvider`

| Konsument | Miejsce |
|---|---|
| portal | `homeportal-portal-model/.../offer/OfferSaleHall.java:76` i rownolegle klasy `Offer*` — `getFeatureTypes()` |
| hop | `homeportal-hop-model/...` — analogicznie (12 uzyc) |

**Werdykt: zmiana zachowania (naprawa).** `forRentObject()` i `forSaleObject()` zwracaly dotad **puste
listy**, a `forRentLand()` — cechy hal. Po poprawce oferty typu "obiekt" dostaja wlasny zestaw filtrow,
a grunty pod wynajem — grunowy. Gettery zwracaja teraz `unmodifiableList`: konsument, ktory probowalby
modyfikowac zwrocona liste, dostanie `UnsupportedOperationException` (dzis nikt tego nie robi).

### `model/feature/FeatureConverter`

| Konsument | Miejsce | Na czym polega |
|---|---|---|
| portal | `homeportal-portal-model/.../offer/Offer.java:238,248` | `toFeatureMap(features)` → `Map<String,String>`, odczyt przez `featureMap.get(nazwa)` |
| portal | `homeportal-portal-management/.../OfferManager.java:210` | `toFeatures(featuresMap)` — zapis z powrotem do bazy |
| hop | `homeportal-hop-model/.../PortalOffer.java:246` | `toFeatureMap(features)` |
| hac | `homeportal-hac-client/.../OfferFeatures.java:23` | tylko dokumentacja ksztaltu (`grupa → lista kodow`) |

**Werdykt: zmiana zachowania — wymaga weryfikacji manualnej.** Wartoscia w mapie jest teraz cala czesc
po dwukropku, wiec cecha wielowartosciowa zwraca `"prąd^woda^gaz"` zamiast `"prąd"`. Dla cech
jednowartosciowych (wszystkie numeryczne, MARKET, HEATING itd.) nic sie nie zmienia. **Do sprawdzenia
w portalu: czy ktores miejsce renderuje wartosc cechy wielowartosciowej wprost do widoku** — tam
zamiast jednej wartosci pojawi sie string z separatorami. Dla takich przypadkow doszla metoda
`toFeatureValues(String)` zwracajaca `Map<String, List<String>>`.
Druga zmiana: `toFeatures` sortuje klucze, wiec ponowna serializacja tej samej mapy daje zawsze ten sam
string (wczesniej kolejnosc zalezala od `HashMap`) — `OfferManager` przestaje brudzic wiersz bez potrzeby.

---

---

## Faza 4 — kontrakt kodowania FTS

### `search/QueryParameter` (enumy w hop i portal)

| Konsument | Miejsce |
|---|---|
| hop | `homeportal-hop-service/.../search/HopQueryParameter.java:5` |
| portal | `.../common/service/PortalQueryParameter.java:5` |
| hop (test) | `.../PortalRepositoryTest.java:125` |

**Werdykt: kompiluje sie bez zmian, ale wymaga swiadomej deklaracji.** Doszla metoda
`default ValueEncoder encoder()` zwracajaca `ValueEncoders.TEXT`, czyli dotychczasowe zachowanie —
istniejace enumy nie wymagaja zmian, zeby sie zbudowac. **Ale poprawnosc wymaga nadpisania jej wszedzie
tam, gdzie pole encji jest indeksowane innym bridge'em:**

| Bridge na polu encji | Encoder do zadeklarowania |
|---|---|
| `@FieldBridge(FeatureBridge)` — np. `PortalOffer.features` | `ValueEncoders.FEATURE` |
| `@FieldBridge(NumericBridge)` — ceny, powierzchnie, pietra | `ValueEncoders.NUMERIC` |
| `@FieldBridge(DateBridge)` — daty dodania | `ValueEncoders.DATE` |
| `@FieldBridge(PropertyTypeBridge)` lub brak | `ValueEncoders.TEXT` (domyslne) |

Dopoki hop nie zadeklaruje `FEATURE` dla parametru cech, filtr po cesze z polskim znakiem
nadal bedzie zwracal zero wynikow — poprawka po stronie commons jest konieczna, ale nie wystarczajaca.

### Bridge'y (`@FieldBridge` w ~20 polach encji portal/hop)

**Werdykt: zmiana formatu indeksu — wymagany pelny reindeks.**
- `NumericBridge`: nowy format (przesuniecie o 2^63, stala szerokosc 20). Stary obcinal do `int`
  (cena 3 mld zapisywala sie jako `-1294967296`) i psul porzadek wartosci ujemnych.
- `FeatureBridge`, `PropertyTypeBridge`, `DateBridge`: wynik bez zmian — sa teraz cienkimi adapterami
  na te same encodery, ktorych uzywa strona zapytania.

### `search/SearchQuery`

| Konsument | Miejsce |
|---|---|
| hop | `.../search/HopSearchQueryBuilder.java:29,35,85` (sentinel `MAX = "9999999"`) |
| portal | `.../common/service/SearchQueryBuilder.java:424` |

**Werdykt: zmiana zachowania, zgodna w kompilacji.** Wszystkie dotychczasowe metody zostaly.
Nowosci: `addRangeFrom`/`addRangeTo` (zakresy otwarte — pozwalaja usunac sentinel `9999999`, przez ktory
oferty powyzej ~10 mln PLN wypadaly z wynikow), escapowanie skladni Lucene w kazdej wartosci, pomijanie
pustych wartosci zamiast emitowania `(field:null)`, wyjatek zamiast cichego bledu przy nienumerycznej
granicy zakresu. `isSortEmpty()` zwraca teraz to, co obiecuje nazwa (byla odwrocona; nikt jej nie wolal).

---

---

## Faza 5 — API repozytorium i paginacja

### `repository/FullTextRepository` (17 repozytoriow dziedziczacych)

| Zmiana | Wplyw na konsumenta |
|---|---|
| `save(S)` → `indexedSave(S)` | **Zmiana zachowania bez bledu kompilacji.** `repository.save(x)` nadal sie kompiluje, ale rozstrzyga sie teraz na `SimpleJpaRepository.save` — czyli `persist` dla encji transientnej zamiast `merge`. Kod, ktorego nikt nie tknal, zacznie dzialac inaczej (poprawnie: przekazany obiekt dostanie identyfikator). Wymaga przejrzenia miejsc wolajacych `save` na repozytoriach FTS. |
| `delete(T)` → `indexedDelete(T)` | jw. — `delete` wraca do semantyki Spring Data (merge dla detached zamiast wyjatku) |
| `createQuery(...)` usuniete z interfejsu | zero uzyc downstream (zweryfikowane) |
| `SortField[]` → `SortSpec` w `SortFieldAware` | zero uzyc downstream |
| `SearchQuery.getSortFields()` → `getSortSpecs()` | zero uzyc downstream |
| `deleteAll(Class)` | bulk delete zamiast petli po encjach — bez zmiany sygnatury; uwaga: **nie odpala kaskad JPA ani listenerow** |
| `countByIndex` | zamiast sentinela `-1` rzuca `HomeportalServiceException`. `portal/.../IndexManager.java:197` formatuje wynik do komunikatu JMX — po migracji trzeba go objac try/catch |

Klasa ma teraz `@Transactional` na poziomie typu i nie robi eager `flushToIndexes()` — publikacja do
indeksu nastepuje przy commicie. Konsument bez ambientnej transakcji dostanie `TransactionRequiredException`
zamiast cichego rozjazdu bazy z indeksem.

### `pageable/Page` (6 formularzy dziedziczacych, w tym kontrakt REST hop-a)

**Werdykt: kompiluje sie, ale zmienia semantyke pochodnych metod.** `Page` implementuje teraz `Pageable`
zamiast dziedziczyc po `PageRequest`. Zachowane bez zmian: nazwy pol (`page`, `size`, `sort`, `reverse` —
to nazwy parametrow HTTP w publicznym API hop-a), 1-based numeracja `getPageNumber()`, `getSortField()`,
`isReverseOrder()`.

Zmienione: `getOffset()` liczy teraz `(page-1) * size` (wczesniej zwracalo stale 20, bo czytalo stan
nadklasy), `equals`/`hashCode` porownuja realny stan formularza (wczesniej dwa formularze rozniace sie
strona byly rowne), `next()`/`previousOrFirst()`/`first()` zwracaja `Page` w numeracji 1-based (wczesniej
`PageRequest` w 0-based). Doszlo `toPageable()` — jedyne miejsce, gdzie zyje konwersja 1-based → 0-based;
konsumenci moga zastapic nim swoje reczne `getPageNumber() - 1` (`hop/.../SearchQueryBuilder.setPageable`,
`portal/.../SearchQueryBuilder.java:427`).

### `pageable/PageItems`

**Werdykt: naprawa bledu widocznego dla uzytkownika.** `getPageItems()` zwracalo `null` przy zerowej
liczbie wynikow (`portal/.../AbstractListController.java:124` wola to bezwarunkowo) — teraz zwraca pusta
liste. Budowanie linkow nie mutuje juz formularza zwiazanego z zadaniem, a blad budowy linku trafia do
logu zamiast byc polykany.

### `index/IndexerMonitor`

**Werdykt: zmiana sygnatury.** `acquireLock(String)` zwraca teraz `boolean` (dotad `void`) i faktycznie
odrzuca pozyskanie, gdy indeksowanie juz trwa. Wolajacy (`portal/.../IndexerScheduler`, `IndexManager`,
`hop/.../MassIndexerScheduler`) powinni sprawdzac wynik zamiast zakladac, ze blokada zawsze przysluguje.

---

## Do przeskanowania w kolejnych fazach
- **Faza 4**: enumy `QueryParameter` w hop i portal, `@FieldBridge` w encjach portal/hop
- **Faza 5**: 17 repozytoriow dziedziczacych `FullTextRepository`, 6 formularzy dziedziczacych `Page`,
  `@EnableJpaRepositories(basePackages = ... "pl.homeportal.commons.data.repository")` w trzech aplikacjach
