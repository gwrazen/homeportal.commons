---
change_id: commons-spring-boot-3
title: Linia commons na jakarcie jako wersja 8.0 — warunek wstępny Boota 3 w portalu, hopie i importerze, a przy okazji jedyne miejsce, gdzie hac już dziś nosi bombę z opóźnionym zapłonem
status: archived
created: 2026-09-10
updated: 2026-09-11
archived_at: 2026-09-11T07:45:00Z
---

## Notes

Zgłoszone przez usera **2026-09-10** jako rozszerzenie trójki `hp-spring-boot-3`,
`hop-spring-boot-3`, `importer-spring-boot-3`. Commons nie należy do „homixa" z definicji, ale
**jest wejściem tego tematu** — dokładnie tak samo, jak przy migracji na Gradle'a
(decyzja usera z 2026-09-09: „commons idzie pierwszy"), gdzie okazał się poligonem.

Priorytet: 🟡 — **ocena własna.** Nic nie jest zepsute i nic tego nie wymusza **dziś**.
⚠️ Podnieś do 🟠 w dniu, w którym zapadnie decyzja o Boocie 3 w którymkolwiek z trzech repozytoriów —
ten ticket jest wtedy pierwszym krokiem, a nie równoległym. Ta sama konwencja, co
w `hop-hibernate-search-6`.

## Stan zmierzony 2026-09-10 (gałąź `jdk17`, wersja 7.0)

| co | wartość |
|---|---|
| build | Gradle **9.7.1**, JDK 17 (jedyne repo homixa na 9.x) |
| Spring | BOM **5.2.9.RELEASE** + spring-data **Moore-SR10** — obie z 2020 |
| Hibernate | Core **5.0.10.Final**, Search **5.5.4.Final**, Validator 6.1.5 |
| konsumenci | `hac` (Boot **3.3.5**), `portal`, `hop`, `importer` (Boot **2.7.18**) |

⚠️ **Wersje Springa z commons NIE wyciekają do aplikacji** — sprawdzone na drzewie zależności
portalu: `spring-web` rozwiązuje się tam na **5.3.31** z BOM-u Boota, nie na 5.2.9 z commons.
Stary BOM commons obowiązuje wyłącznie przy budowaniu samego commons. To zawęża ten ticket:
nie chodzi o „commons ciągnie wszystkich w dół", tylko o **przestrzeń nazw `javax` w API**.

## Gdzie siedzi `javax` — i to jest cała treść tego ticketu

| moduł | plików z `javax` | które pakiety |
|---|---:|---|
| `homeportal-commons-java` | **7** | `javax.servlet`, `javax.imageio`, `javax.validation` |
| `homeportal-commons-data` | **6** | `javax.persistence` |
| `homeportal-commons-location-api` | 4 | `javax.ejb`, `javax.xml` |
| `homeportal-commons-mail` | 2 | `javax.mail` |
| `-domain`, `-geo-api`, `-logging`, `-test` | **0** | — |

Dwa uściślenia, żeby nie policzyć tego dwa razy:

- **`javax.validation` NIE jest długiem** — moduł deklaruje `jakarta.validation:jakarta.validation-api`
  w wersji **2.0.2**, a ta linia nadal używa pakietu `javax.validation`. Rename na `jakarta.validation`
  przychodzi dopiero z 3.0 i **jest częścią tej roboty**, ale nie jest zaniedbaniem.
- **`javax.imageio` i `javax.xml` zostają** — to pakiety z JDK, nie z Javy EE. Boot 3 ich nie rusza.

Realna praca to więc: `javax.servlet` (3 klasy w `-java`), `javax.persistence` (6 plików
w `-data`), `javax.mail` (2 pliki), `javax.ejb` (`-location-api`) i podbicie
`jakarta.validation-api` 2.0.2 → 3.x.

## ⚠️ Hac już dziś nosi bombę z opóźnionym zapłonem — i to jest jedyna rzecz „na teraz"

`homeportal-commons-java` deklaruje `javax.servlet:servlet-api` jako **`provided`**
(`homeportal-commons-java/build.gradle.kts:24`), a trzy klasy realnie go importują:
`mvc/controller/ControllerUtils`, `i18n/LanguageResolver`, `datetime/DateTimeUtils`.

Hac chodzi na **Boocie 3.3.5**, gdzie na classpathie jest `jakarta.servlet`, a `javax.servlet`
**nie ma wcale**. Działa wyłącznie dlatego, że **nigdy tych klas nie woła** — sprawdzone:
jedyne wystąpienie `ControllerUtils` w całym repo haca to **komentarz javadoc** w `LoggingService`
mówiący, że ten serwis celowo stoi na czystym SLF4J. `LanguageResolver`, `ObjectValidator`
i `DateTimeUtils` nie występują w hacu ani razu.

Skutek pierwszego wywołania którejkolwiek z nich: **`NoClassDefFoundError javax/servlet/http/HttpServletRequest`
w runtime**, przy zielonym buildzie. To jest dokładnie ten kształt, którego w tym warsztacie
pilnujemy — kompiluje się, wdraża się, milczy, a wywala się przy pierwszym użyciu.

## Mechanizm wydania — dlaczego to NIE wymusza migracji wszystkich naraz

Każdy konsument **przypina wersję commons na sztywno** (`homeportalCommonsVersion = "7.0"`
w każdym `build.gradle.kts`). Dlatego linia jakartowa nie musi być rozgałęzieniem repozytorium:

- **commons 8.0** wychodzi na jakarcie i obsługuje konsumentów na Boocie 3;
- **commons 7.0** zostaje w rejestrze i w `~/.m2` nietknięty — portal, hop i importer siedzą na nim,
  dopóki nie przejdą;
- kolejność migracji konsumentów przestaje być wymuszona: każdy podbija pin wtedy, kiedy jest gotowy.

⚠️ Cena tego rozwiązania: **przez jakiś czas żyją dwie linie commons naraz**, a poprawka zrobiona
w jednej nie trafia do drugiej sama. To jest dokładnie ta klasa ryzyka, która w tym repo już raz
kosztowała (`GERMAN` w `homeportal-commons-java` — maszyna ze starym 6.0 w `~/.m2` nie dowiedziała
się o zmianie). Plan musi powiedzieć, **jak długo obie linie żyją i kto pilnuje backportu**.

## Czego NIE zakładać

- **JDK 17 nie jest bramką** — commons buduje się na 17 od migracji Gradle'owej.
- **To nie jest „przy okazji" migracji Gradle'a** — tamta jest skończona i zarchiwizowana (`2c8e363`).
- **Nie wolno przenieść commons na jakartę bez wydania nowej wersji.** Nadpisanie 7.0 wysadza
  portal, hopa i importera w tym samym momencie, w którym ktokolwiek przebuduje — i to jest
  awaria, którą widać dopiero na produkcji.

## Powiązane

- `hp-spring-boot-3`, `hop-spring-boot-3`, `importer-spring-boot-3` — konsumenci czekający na tę linię
- `hop-hibernate-search-6` (repo `hop`) — Search 5.5.4 w `commons-data` idzie razem z tamtą migracją
- `homeportal-commons-java/build.gradle.kts:24` — `provided("javax.servlet:servlet-api")`
- `context/archive/2026-08-29-commons-gradle-migration/` — metoda i pułapki wydawnicze tego repo

---

# Próba na gałęzi `boot3` (2026-09-10) — zmierzone, gdzie jest ściana

Gałąź `boot3` odbita od `jdk17`. Zrobione **mechanicznie**: jeden BOM Boota **3.3.5** zamiast
`spring-framework-bom` 5.2.9 + `spring-data-releasetrain` (ten drugi nie istnieje po Springu 5),
współrzędne jakartowe w miejsce javaxowych, `javax.*` → `jakarta.*` w **17 plikach**, wersja 8.0.

## Wynik: 7 modułów z 8 kompiluje się na jakarcie od pierwszego podejścia

| moduł | stan |
|---|---|
| `-java`, `-domain`, `-geo-api`, `-location-api`, `-logging`, `-mail`, `-test` | **kompilują się** |
| `-data` | **nie kompiluje się** — wyłącznie Hibernate Search |

Cała reszta migracji — Spring 5.2.9 → 6.1, Hibernate 5.0.10 → 6.5, `jakarta.validation` 2.0 → 3.0,
`javax.servlet` → `jakarta.servlet`, `javax.mail` → `jakarta.mail`, JAXB 2.3 → 4.0 — **przeszła bez
ani jednej poprawki w kodzie poza zamianą importów**. Trzy drobiazgi wyszły po drodze i są już
naprawione: `hibernate-entitymanager` nie istnieje w 6.x (wchłonął go `hibernate-core`, który
zmienił groupId na `org.hibernate.orm`), Jakarta EL 5.0 to `org.glassfish.expressly:expressly`,
a Hibernate Search zmienił współrzędne na `org.hibernate.search:hibernate-search-mapper-orm`
+ `-backend-lucene`.

## Ściana: pięć plików, jedno API

```
FullTextRepositoryImpl   — FullTextEntityManager, FullTextQuery, SearchFactory (org.hibernate.search.jpa.*)
FeatureBridge            — org.hibernate.search.bridge.StringBridge
NumericBridge            — j.w.
DateBridge               — j.w.
PropertyTypeBridge       — j.w.
```

W Hibernate Search 6/7 **cały ten pakiet nie istnieje**: `FullTextEntityManager` zastąpiła
`SearchSession`, `StringBridge` — `ValueBridge`, a DSL zapytań przepisano od zera.

## ⚠️ Korekta do łańcucha zapisanego w tym tickecie po południu

Ticket mówi, że commons wychodzi jako 8.0, a konsumenci podbijają pin **każdy wtedy, kiedy jest
gotowy**. Dla jakarty to prawda. **Dla Hibernate Search jest fałszem** — i to jest najważniejsze
znalezisko tej próby.

Mostki i `FullTextRepository` z commons obsługują adnotacje, które siedzą **w encjach konsumentów**:
**225** wystąpień `@Field`/`@Indexed`/`@FieldBridge` w `portal-model` i **122** w `hop-model`.
Search 6/7 wymienił te adnotacje wszystkie naraz (`@FullTextField`, `@GenericField`,
`@ValueBridgeRef`), więc **`commons-data` + `portal-model` + `hop-model` muszą przejść jednym
ruchem** — a do tego dochodzi przeindeksowanie (4,45 mln ofert w hopie, ~16,6 tys. w portalu).

To nie unieważnia wydania 8.0 dla siedmiu pozostałych modułów: one mogą wyjść na jakarcie
i odblokować konsumentów, którzy nie dotykają wyszukiwarki. Rozstrzygnięcie, czy `-data` idzie
w tym samym wydaniu, czy w osobnym, należy do planu.

## Czego ta próba NIE zrobiła

Nie tknęła Hibernate Search API (5 plików nadal się nie kompiluje), nie uruchomiła testów,
nie opublikowała niczego do rejestru ani do `~/.m2`, nie ruszyła portalu ani hopa.
Gałąź `boot3` jest **eksperymentem pomiarowym**, nie kandydatem do wydania.

---

# ✅ Commons przechodzi na Boota 3 — build zielony, **wersja zostaje 7.0** (2026-09-10)

Decyzja usera: *„rob teraz całość na 7"* — nie ma linii 8.0, commons migruje **w miejscu**.
Gałąź `boot3`, build zielony, **139 testów** przechodzi, w tym cały `FullTextRepositoryIntegrationTest`.

## Co zostało przepisane

| obszar | z | na |
|---|---|---|
| platforma | `spring-framework-bom` 5.2.9 + `spring-data-releasetrain` | `spring-boot-dependencies` **3.3.5** |
| przestrzeń nazw | `javax.*` | `jakarta.*` (17 plików) |
| Hibernate | Core 5.0.10 + `hibernate-entitymanager` | Core 6.5 (`org.hibernate.orm`, entitymanager wchłonięty) |
| Hibernate Search | 5.5.4 (`hibernate-search-orm`) | **7.1.1** (`hibernate-search-mapper-orm` + `-backend-lucene`) |
| sesja wyszukiwania | `FullTextEntityManager` | `SearchSession` |
| mostki | `StringBridge#objectToString` | `ValueBridge<Object,String>#toIndexedValue` |
| adnotacje encji | `@Field(store) + @FieldBridge(impl)` | `@FullTextField(projectable, valueBridge = @ValueBridgeRef(type))` |
| poczta | `commons-email` 1.2 (javax) | `commons-email2-jakarta` **2.0.0-M1** |
| EL | `javax.el` + `org.glassfish.web:javax.el` | `org.glassfish.expressly:expressly` 5.0 |

**To jest wzorzec dla `portal-model` (225 adnotacji) i `hop-model` (122)** — tam czeka dokładnie
ta sama zamiana, tylko w większej skali.

## Pięć pułapek, które kosztowały tu czas i policzą się u konsumentów

1. **Właściwości Hibernate Search zmieniły nazwy.** `hibernate.search.default.directory_provider = ram`
   → `hibernate.search.backend.directory.type = local-heap`; `hibernate.search.lucene_version` znika.
   Search 7 **odmawia startu** przy starych kluczach (HSEARCH000573), więc produkcyjne
   `application.yml` portalu i hopa wymagają tej samej korekty.
2. **Identyfikator nie jest polem indeksu.** Idiom z 5.x — `(id:[0 TO 999999999])` jako „wszystkie
   dokumenty" — trafia dziś w **nic** i licznik oddaje zero przy niepustym indeksie. Zastąpione
   przez `matchAll()`. ⚠️ Każde zapytanie konsumenta odwołujące się do pola `id` ma ten sam problem.
3. **`Pageable` dostał w Spring Data 3 metodę `withPage(int)`** — `Page` z commons implementuje ten
   interfejs, więc bez niej nie kompiluje się nic. Uwaga: argument jest 0-based, a nasza klasa
   liczy strony od 1.
4. **`parser.setLowercaseExpandedTerms(true)` zniknęło w Lucene 7.** Rozwijane termy (wildcard,
   zakresy) nie są już obniżane do małych liter przez parser. Dla pól analizowanych bez zmian,
   **dla pól `keywordAnalyser` zachowanie się różni** i wymaga sprawdzenia po stronie portalu.
5. **`EmailException` mieszka w `commons-email2-core`**, a nie w `-jakarta`.

## ⚠️ Dwa ryzyka, których ten build NIE zamyka

- **Sortowanie jest niesprawdzone.** `FullTextRepositoryIntegrationTest` **nie ma ani jednego testu
  sortowania** (sprawdzone), a Search 6/7 wymaga `sortable = Sortable.YES` na polu, żeby natywny
  `Sort` Lucene'a miał po czym sortować. Portal sortuje po cenie, dacie i metrażu — **to jest
  najpoważniejsza niewiadoma przy migracji `portal-model`**.
- **`commons-email2-jakarta 2.0.0-M1` to wydanie milestone** w bibliotece obsługującej całą pocztę
  wychodzącą czterech aplikacji. Alternatywa (przepisanie `VelocityEmail` na `MimeMessageHelper`
  Springa) jest czystsza zależnościowo, ale rusza logikę wysyłki. Do rozstrzygnięcia przed wydaniem.

## ⚠️ Czego NIE wolno teraz zrobić

**Nie publikować tego artefaktu** — ani do `~/.m2`, ani do rejestru. Wersja została **7.0**, więc
nadpisanie zastąpiłoby javaxową bibliotekę jakartową pod tym samym numerem i **portal, hop
i importer przestałyby się budować w tej samej chwili**. Dopóki konsumenci nie przejdą na Boota 3,
ta gałąź jest wyłącznie lokalna. `publishToMavenLocal` na niej to awaria trzech repozytoriów naraz.

---

# ✅ ZAMKNIETE 2026-09-11

Spring Boot 2.7.18 -> 3.3.5 wykonane i **wdrozone na produkcje 2026-09-11**. Testy: **139**.
Szczegoly znalezisk w tresci powyzej.
