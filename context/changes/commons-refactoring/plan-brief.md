# Commons 6.0 — refaktor modulow, bugfixy i naprawa full-text search — Plan Brief

> Full plan: `context/changes/commons-refactoring/plan.md`
> Research: `context/changes/commons-refactoring/research.md`

## What & Why

Wyszukiwarka commons ma zlamany kontrakt kodowania: indeks i zapytanie koduja wartosci niezaleznie i sie rozjezdzaja, przez co czesc filtrow (cechy z polskimi znakami, miasta o nazwach dwuczlonowych, ceny powyzej ~10 mln PLN) zwraca **zawsze zero wynikow, bez zadnego bledu**. Przy okazji rozpoznanie wykazalo kilkanascie innych bledow dzialajacych na produkcji — puste listy cech dla ofert typu "obiekt", gubione wartosci wielokrotne, ciche gubienie maili logowane jako sukces. Wydanie 6.0 naprawia to komplet, kazda poprawke przypinajac testem, i przygotowuje grunt pod pozniejsza migracje HS6/JDK17.

## Starting Point

69 klas produkcyjnych, ~14% pokrycia testami, **CI nie uruchamia testow w ogole** (jedyny workflow to deploy z `-DskipTests`). Build nie ma ustawionego encodingu przy 24 plikach z polskimi znakami — a to wlasnie te literaly decyduja o dopasowaniu w Lucene. `FullTextRepository` wystawia typy Lucene i Hibernate Search w sygnaturach i jest dziedziczony przez 17 repozytoriow downstream; jego `save`/`delete` koliduja z `CrudRepository` i wygrywaja rozstrzygniecie, przez co `save(nowyObiekt)` robi `merge` i zostawia obiekt transientny. `hac` juz uciekl z commons — ma reczne kopie modelu domenowego, bo `-data` jest zroniete z ORM+Lucene.

## Desired End State

Filtry wyszukiwarki dzialaja dla wszystkich wartosci, ktore uzytkownik moze wpisac. Kazdy znany bug jest naprawiony i ma test, ktory bez poprawki nie przechodzi. CI uruchamia testy na kazdy push. Istnieje modul `commons-domain` bez zaleznosci ORM, z ktorego `hac` moze korzystac bezposrednio zamiast utrzymywac kopie. Publiczne API nie zawiera typow Lucene ani Hibernate Search, wiec przyszla migracja HS6 nie kaskaduje na konsumentow.

## Key Decisions Made

| Decyzja | Wybor | Dlaczego | Zrodlo |
| --- | --- | --- | --- |
| Zgodnosc API | 6.0 lamie API | Major bump pozwala zdjac kolizje z `CrudRepository` i schowac typy HS — inaczej placimy ten koszt drugi raz przy HS6 | Plan |
| Zakres HS | Zostajemy na HS5 | Naprawia bledy dzialajace dzis bez ciagniecia calego stosu Hibernate; HS6 staje sie potem zmiana w 2 plikach + 4 bridge'ach | Plan |
| Modul domenowy | Nowy `commons-domain` | `hac` moze skasowac kopie i przestac wykluczac `commons-data`; rozcina tez `-logging`→`-data` | Plan |
| Indeks Lucene | Pelny reindeks przy wdrozeniu | Bez zmiany formatu nie da sie naprawic przepelnienia `intValue()` ani porzadku wartosci ujemnych | Plan |
| Zakres bugfixow | Wszystkie potwierdzone, kazdy z testem | Bledy dzialaja na produkcji, a poprawki sa male i niezalezne | Plan |
| Testy | Test regresyjny do kazdej poprawki + CI z `mvn verify` | Przy 14% pokrycia to jedyny sposob, by refaktor FTS nie wprowadzil cichych regresji | Plan |
| Porzadki | Higiena buildu + rozciecie `-logging` + prune + martwy kod | Encoding jest warunkiem wstepnym dla testow kodowania; reszta zmniejsza powierzchnie pod HS6/JDK17 | Plan |
| Konsumenci | Commons + `migration-6.0.md` | Kazde repo ma wlasny cykl wydawniczy; mieszanie ich w jednym planie blokuje wszystko do momentu, az wszystko bedzie gotowe | Plan |
| Wydania | Jedno 6.0, fazy w kolejnosci ryzyka | Fundament (encoding, CI) musi powstac przed zmianami, ktore bez niego sa nieweryfikowalne | Plan |

## Scope

**In scope:** higiena buildu i CI z testami; wszystkie potwierdzone bugfixy w `-java`, `-data`, `-logging`, `-mail`; wydzielenie `commons-domain`; rozciecie `-logging`→`-data`; naprawa kontraktu kodowania FTS; zmiana API `FullTextRepository`; rozplatanie `Page`/`PageItems`; prune zaleznosci i martwego kodu; dokument migracji.

**Out of scope:** migracja HS6 / jakarta / JDK 17 (zmiana `jdk17-migration`); aktualizacja konsumentow (`hop`, `portal`, `importer`, `hac` — osobne zmiany w swoich repo); zmiana nazw pakietow uzywanych w `@EnableJpaRepositories`; zmiana nazw stalych `FeatureType` (sa zapisane w bazie portalu); migracja hasel na bcrypt; `geo-api` i `location-api`.

## Architecture / Approach

Kolejnosc podyktowana ryzykiem, nie wartoscia biznesowa: najpierw fundament pozwalajacy cokolwiek udowodnic (encoding + CI), potem poprawki niezalezne, potem zmiany strukturalne, na koncu lamiace API. Kazda faza zostawia repo w stanie zdatnym do wydania.

Rdzen zmiany FTS: kodowanie wartosci przestaje byc zaszyta decyzja w `SearchQuery`, a staje sie deklaracja przy `QueryParameter` — kazdy parametr mowi, jakim encoderem sie posluguje, a test parametryzowany sprawdza zgodnosc ze strona indeksu dla kazdej stalej enuma. To jednoczesnie naprawia rozjazd i jest ksztaltem, ktorego oczekuje `ValueBridge` z HS6.

Zasada dla bugfixow: **test najpierw** — commit z testem, ktory nie przechodzi, potem poprawka.

## Phases at a Glance

| Faza | Co dostarcza | Kluczowe ryzyko |
| --- | --- | --- |
| 1. Fundament: build i CI | Encoding UTF-8, Spring BOM, slf4j, CI z `mvn verify`, naprawa testow bez asercji | Zmiana wersji Springa moze ujawnic ukryte niezgodnosci |
| 2. Bugfixy poza FTS | ~20 poprawek w `-java`, `-mail`, `-logging`, kazda z testem | Czesc zmienia obserwowalne zachowanie (mail, hasla spoza ASCII) |
| 3. `commons-domain` + bugfixy domenowe | Nowy modul, poprawki `FeatureTypeProvider`/`FeatureConverter`, rozciecie `-logging`→`-data` | Zmiana sygnatur `LoggingSupport` uzywanych w 343 miejscach downstream |
| 4. FTS: kontrakt kodowania | Encoder przy `QueryParameter`, escapowanie, zakresy otwarte, `NumericBridge` bez `intValue()` | Zmienia format indeksu — od tej fazy reindeks jest obowiazkowy |
| 5. FTS: API i paginacja | Rename metod, `@Transactional`, ukrycie typow HS, rozplatanie `Page` | Najwieksza zmiana lamiaca dla konsumentow |
| 6. Domkniecie | Prune, martwy kod, `migration-6.0.md` | Skan objal 5 repo, nie dowodzi braku innych konsumentow |

**Prerequisites:** branch `commons-refactoring` (jest), srodowisko testowe z mozliwoscia pelnego reindeksu, dostep do lokalnych klonow `hac` i `hop` do weryfikacji manualnej.
**Estimated effort:** ~6 sesji, po jednej na faze; fazy 2 i 4 sa najwieksze i moga wymagac dwoch.

## Open Risks & Assumptions

- **Reindeks na produkcji to okno serwisowe** — do jego zakonczenia wyszukiwarka zwraca niepelne wyniki. Czas mierzony w fazie 4 na srodowisku testowym; jesli okaze sie za dlugi, wraca temat dual-write (dzis odrzucony).
- **Poprawka `FeatureTypeProvider` zmienia to, co uzytkownik widzi na filtrach** dla ofert typu "obiekt" i "grunt" — dzis listy sa puste/bledne. Zaklada sie, ze poprawne listy sa pozadane; wymaga potwierdzenia przy weryfikacji manualnej.
- **`Language` z `"ua"` na `"uk"`** — jesli stara wartosc jest utrwalona w bazie konsumenta, alias musi zostac na stale, nie tymczasowo.
- **Commons 6.0 przez jakis czas nie ma zadnego konsumenta**, wiec realna weryfikacja odklada sie do pierwszej migracji (rekomendowany pilot: `hop` — najmniejsza powierzchnia FTS).
- **Skan martwego kodu objal 5 repozytoriow** i nie dowodzi, ze nic innego nie uzywa usuwanych klas — stad prune jest ostatnia faza.

## Success Criteria (Summary)

- Uzytkownik wyszukiwarki dostaje wyniki dla filtrow, ktore dzis milczaco zwracaja zero: cechy z polskimi znakami, miasta dwuczlonowe, ceny powyzej 10 mln PLN, oferty typu "obiekt" i grunty
- Zaden znany bug z rozpoznania nie jest juz w kodzie, a kazdy ma test, ktory go lapie; CI uruchamia te testy na kazdy push
- `hac` moze skasowac reczne kopie modelu, a przyszla migracja HS6 nie kaskaduje na konsumentow, bo typy Lucene/HS zniknely z publicznego API
