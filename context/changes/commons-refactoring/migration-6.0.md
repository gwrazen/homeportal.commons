# Migracja na homeportal-commons 6.0

Dokument dla konsumentów: `hop`, `portal`, `importer`, `hac`. Każdy migruje we własnym repo i tempie —
commons 6.0 nie wymusza jednoczesnego przejścia wszystkich.

Mapa użycia API u konsumentów (co gdzie jest wołane) żyje w `consumers.md`.

---

## 1. Jak wziąć wersję 6.0

`hop`, `portal` i `importer` **nie nazywają dziś wersji commons** — biorą `${project.parent.version}`,
czyli własną wersję 5.0. W tej postaci nie da się wziąć commons 6.0 bez podbicia własnej wersji produktu.

Rekomendacja: wprowadzić własność, wzorem `hac` (`homeportal.hac/pom.xml:32`):

```xml
<properties>
    <homeportal.commons.version>6.0</homeportal.commons.version>
</properties>
```

i użyć jej we wszystkich deklaracjach `homeportal-commons-*`. To odwiązuje cykl wydawniczy konsumenta
od commons.

**Pułapka:** `homeportal.hop/homeportal-hop-management/pom.xml:31` ma zaszyte `<version>5.0</version>`
dla `homeportal-commons-logging`. Bez poprawki ten moduł zostanie na 5.0 i wciągnie dwie wersje tej samej
klasy na classpath.

### Nowy moduł

Doszedł `homeportal-commons-domain` — model domenowy bez ORM, Lucene i Springa (`Product`, `Market`,
`Activity`, system cech, interfejsy znacznikowe). **Pakiety się nie zmieniły** (`pl.homeportal.commons.data.model.*`),
więc importy zostają bez zmian; `-data` zależy od `-domain`, więc dotychczasowi konsumenci `-data`
dostają go automatycznie.

Dla `hac`: można teraz zależeć od samego `homeportal-commons-domain` i **skasować ręczne kopie**
`OfferProduct`, `OfferActivity`, `OfferMarket`, `FeatureType`. `-logging` nie zależy już od `-data`,
więc blok `<exclusions>` z `homeportal.hac/pom.xml:87-98` jest zbędny.

---

## 2. Zmiany łamiące kompilację

| Było | Jest | Gdzie boli |
|---|---|---|
| `FullTextRepository.save(S)` | `indexedSave(S)` | 17 repozytoriów dziedziczących — patrz §3, to nie jest zwykły rename |
| `FullTextRepository.delete(T)` | `indexedDelete(T)` | jw. |
| `FullTextRepository.createQuery(...)` | usunięte z interfejsu | zero użyć downstream |
| `SortFieldAware.getSortFields(): List<SortField>` | `getSortSpecs(): List<SortSpec>` | zero użyć downstream |
| `SearchQuery.getSortFields()` | `getSortSpecs()` | zero użyć downstream |
| `IndexerMonitor.acquireLock(String): void` | `: boolean` | schedulery indeksowania — patrz §3 |
| `LoggingSupport` `<T extends AbstractEntity>` | `<T extends Identifiable>` | źródłowo zgodne (encje dziedziczą po `AbstractEntity`), ale **wymaga rekompilacji** |
| `Page extends PageRequest` | `Page implements Pageable` | 6 formularzy — patrz §4 |
| `SessionMockProvider` | usunięty | mock w produkcyjnym jarze, zero użyć |

---

## 3. Zmiany zachowania BEZ błędu kompilacji

To jest najniebezpieczniejsza kategoria — kod się kompiluje i zachowuje inaczej.

**`repository.save(x)` zacznie robić `persist` zamiast `merge`.** Po zmianie nazw w commons wywołanie
`save` rozstrzyga się na `SimpleJpaRepository.save`. Do 5.0 wygrywał fragment z commons, bo miał tę samą
erased signature — i robił `merge`, przez co **przekazany obiekt nie dostawał identyfikatora** (dostawała
go zwracana kopia). Kod ignorujący wynik `save` zachowa się teraz poprawnie. Przejrzyj miejsca wołające
`save`/`delete` na repozytoriach dziedziczących `FullTextRepository`.

**`countByIndex` rzuca zamiast zwracać `-1`.** `portal/.../mbean/index/IndexManager.java:197` wstawia
wynik wprost do komunikatu JMX — obejmij `try/catch`, inaczej wyjątek poleci na konsolę JMX.

**`IndexerMonitor.acquireLock` może odmówić.** Dotąd ustawiał flagę bezwarunkowo (nie był blokadą).
Schedulery (`portal/.../IndexerScheduler`, `hop/.../MassIndexerScheduler`) i JMX muszą sprawdzać wynik:

```java
if (!monitor.acquireLock("scheduler")) { return; }   // indeksowanie już trwa
```

**`FeatureConverter.toFeatureMap` zwraca pełną wartość cechy wielowartościowej.** Było `"prąd"`,
jest `"prąd^woda^gaz"` — poprzednia wersja gubiła dane przy każdej serializacji. Dla cech
jednowartościowych (wszystkie numeryczne, `MARKET`, `HEATING`...) bez zmian. Jeśli gdzieś renderujesz
wartość cechy wprost do widoku, użyj nowego `toFeatureValues()` zwracającego `Map<String, List<String>>`.

**`FeatureTypeProvider` zwraca poprawne listy.** `forRentObject()` i `forSaleObject()` zwracały dotąd
**puste** listy, a `forRentLand()` — cechy hal. Po poprawce oferty typu „obiekt" i grunty pod wynajem
dostają własne zestawy filtrów. Gettery zwracają `unmodifiableList`.

**`Language` rozpoznaje `"uk"`.** Ukraiński miał wartość `"ua"` (kod kraju). Stara wartość działa dalej
jako alias — nie trzeba migrować danych.

**`MD5Encoder` liczy skrót z bajtów UTF-8.** Dla ASCII (adresy e-mail) wynik identyczny. Różnica
dotyczy wyłącznie danych spoza ASCII.

**`Files.deleteFiles` wchodzi do katalogów pasujących do wzorca.** Dotąd katalog pasujący do wzorca
kończył się nieudanym `delete()` i pominięciem rekurencji. Wzorzec importera (`oferty_%s\d*_\d*.zip`)
pasuje tylko do plików, więc tam bez zmian.

**Nieudana wysyłka maila przestaje wyglądać jak sukces.** `VelocityEmail.send()` rzuca zamiast zwracać
`null` — dotąd `NotifierAdapter` logował `"Email sent ... response: null"` przy każdym błędzie SMTP.

---

## 4. Paginacja

`Page` implementuje `Pageable`, zamiast dziedziczyć po `PageRequest`. **Bez zmian:** nazwy pól
(`page`, `size`, `sort`, `reverse` — to nazwy parametrów HTTP w publicznym API hop-a), 1-based numeracja
`getPageNumber()`, `getSortField()`, `isReverseOrder()`.

**Zmienione:**
- `getOffset()` liczy `(page-1) * size`; dotąd zwracało stale `20`, bo czytało stan nadklasy
- `equals`/`hashCode` porównują realny stan formularza; dotąd dwa formularze różniące się stroną były **równe**
- `next()`/`previousOrFirst()`/`first()` zwracają `Page` w numeracji 1-based; dotąd `PageRequest` w 0-based
- doszło `toPageable()` — jedyne miejsce konwersji 1-based → 0-based

Ręczne konwersje `getPageNumber() - 1` (`hop/.../SearchQueryBuilder.setPageable`,
`portal/.../common/service/SearchQueryBuilder.java:427`) można zastąpić `form.toPageable()`.

`PageItems.getPageItems()` zwraca pustą listę zamiast `null` przy zerowej liczbie wyników — to naprawia
NPE w widoku przy każdym pustym wyszukiwaniu (`portal/.../AbstractListController.java:124`).

---

## 5. Full-text search — co trzeba zadeklarować

`QueryParameter` ma nową metodę `default ValueEncoder encoder()` zwracającą `ValueEncoders.TEXT`.
**Enumy konsumentów kompilują się bez zmian, ale poprawność wymaga deklaracji per parametr.**

Zasada: encoder musi odpowiadać bridge'owi użytemu na tym polu encji.

| `@FieldBridge` na polu encji | Encoder do zadeklarowania |
|---|---|
| `FeatureBridge` (np. `PortalOffer.features`) | `ValueEncoders.FEATURE` |
| `NumericBridge` (ceny, powierzchnie, piętra) | `ValueEncoders.NUMERIC` |
| `DateBridge` (daty dodania) | `ValueEncoders.DATE` |
| `PropertyTypeBridge` lub brak bridge'a | `ValueEncoders.TEXT` (domyślne) |

```java
FEATURES("features") {
    @Override public ValueEncoder encoder() { return ValueEncoders.FEATURE; }
},
PRICE("price") {
    @Override public ValueEncoder encoder() { return ValueEncoders.NUMERIC; }
},
```

**Dopóki tego nie zrobisz, filtr po cesze z polskim znakiem nadal będzie zwracał zero wyników** —
poprawka po stronie commons jest konieczna, ale niewystarczająca.

### Zakresy otwarte

Doszły `addRangeFrom(param, from)` i `addRangeTo(param, to)`. Pozwalają usunąć sentinel
`MAX = "9999999"` z `hop/.../HopSearchQueryBuilder.java:35,85`, przez który oferty powyżej
~10 mln PLN wypadały z wyników „cena od".

### Escapowanie

Wartości są escapowane składnią Lucene. Jeśli gdzieś przekazujesz do `SearchQuery` gotowy fragment
zapytania zamiast surowej wartości użytkownika — przestanie działać jak fragment i zostanie potraktowany
jako literał. To celowe: dotąd wartość `x OR product:1` sklejała się w `xORproduct:1`, czyli klauzulę
na polu sterowanym przez użytkownika.

---

## 6. Procedura wdrożenia — reindeks jest obowiązkowy

`NumericBridge` zmienił format zapisu (przesunięcie o 2^63, stała szerokość 20 znaków). Stary format
obcinał wartości do `int` (cena 3 mld zapisywała się jako `-1294967296`) i psuł porządek liczb ujemnych.
**Indeks zbudowany przed 6.0 nie pasuje do zapytań z 6.0.**

Kolejność:

1. Wdróż kod (aplikacja startuje, wyszukiwarka działa na starym indeksie — z niepełnymi wynikami dla pól numerycznych)
2. Uruchom pełny `indexAll` (JMX: `IndexManager`, albo scheduler)
3. Poczekaj na zakończenie — `IndexerMonitor.isRunning()` wraca na `false`
4. Zweryfikuj filtry: cecha z polskim znakiem, miasto dwuczłonowe, cena powyżej 10 mln
5. Dopiero teraz kieruj ruch użytkowników na nowe filtry

**Czas pełnego reindeksu: _do zmierzenia na środowisku testowym_** — wpisz zmierzoną wartość tutaj przed
wdrożeniem produkcyjnym. Do jego zakończenia wyszukiwarka zwraca niepełne wyniki, więc to okno serwisowe.

Reindeks nie może iść równolegle z drugim reindeksem — `IndexerMonitor` teraz to egzekwuje.

---

## 7. Kolejność migracji konsumentów

Rekomendowany pilot: **`hop`** — najmniejsza powierzchnia FTS (2 repozytoria dziedziczące
`FullTextRepository`), ma już `HopSearchQueryBuilder` dziedziczący po abstrakcji z commons, i jako jedyny
realnie używa `SearchQueryBuilder`. Dopiero po nim `portal` (10 repozytoriów, własny 474-liniowy
`SearchQueryBuilder`), `importer` (5 repozytoriów) i `hac` (może przy okazji skasować kopie modelu).
