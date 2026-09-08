---
change_id: commons-jdk17-migration
title: Migracja homeportal.commons z Javy 8 na 17 — osobna linia 7.0, bo konsument na 8 nie odczyta bajtkodu 17; wydanie z gałęzi `jdk17`, bez merge'a do mastera
status: archived
created: 2026-08-01
updated: 2026-09-08
archived_at: 2026-09-08T19:34:08Z
---

## Notes

Priorytet: 🟡 — **ocena własna.** Nic nie jest zepsute; biblioteka na 1.8 działa i jest czytana
przez wszystkich konsumentów, także tych na 17.

⚠️ **CAŁOŚĆ ROBIMY NA OSOBNYM BRANCHU I NIE MERGUJEMY** — decyzja usera 2026-08-21.
Branch: `feat/commons-jdk17`, odbity od `master`. Żadnego `merge` do `master`, żadnego wydania
artefaktu z tej gałęzi. Przy bibliotece to zastrzeżenie waży więcej niż przy aplikacji: wydany
artefakt trafia do czterech repozytoriów naraz i nie da się go cofnąć jednym `revert`.

Treść dopisana 2026-08-21 (ticket założony 2026-08-01 jako sam tytuł, bez ustaleń).

## ⚠️ Zmiana decyzji 2026-09-08: artefakt WYDAJEMY, pod podbitą wersją

User: „artefakt wydajemy ale wersje bumped". To znosi zapis „żadnego wydania artefaktu"
powyżej — zostaje w mocy tylko część o braku merge'a do `mastera`.

- linia **7.0** na JDK 17, linia **6.x** zostaje na `masterze` dla konsumentów na Javie 8;
- gałąź nazywa się **`jdk17`** (nie `feat/commons-jdk17`) — decyzja usera 2026-09-08,
  zgodnie z konwencją gałęzi w tym repo (`commons-refactoring`, `development`);
- wydanie znaczone anotowanym tagiem `v7.0`, bo gałąź nie wchodzi do `mastera`.

**Warunek wejścia z sekcji niżej przestaje obowiązywać.** Nie trzeba czekać, aż wszystkie
cztery repa będą na 17: każdy konsument pinuje własną właściwość `homeportal.commons.version`
i nie zobaczy 7.0, dopóki sam jej nie podbije.

**Korekta pomiaru konsumentów (2026-09-08).** Tabela niżej mówi o `commons-java` i
`commons-logging` — realnie konsumowanych jest **5 z 6 modułów**, a `hac` pinuje **5.0**, nie 6.0:

| repo | pinuje | moduły |
|---|---|---|
| `hac` | 5.0 | `java`, `logging` |
| `hop` | 6.0 | `java`, `data`, `logging` |
| `portal` | 6.0 | `java`, `data`, `logging`, `mail`, `test` |
| `importer` | 6.0 | `java`, `data`, `logging`, `mail` |

Plan: `plan.md` (skrót w `plan-brief.md`).

⚠️ **Przemianowany 2026-08-29 z `jdk17-migration` na `commons-jdk17-migration`** — na życzenie
usera, dla zgodności z bliźniakami w pozostałych repach (`hp-jdk17-migration`,
`hop-jdk17-migration`, `importer-jdk17-migration`) i z konwencją prefiksu widoczną
w zarchiwizowanym `commons-refactoring`. Odsyłacze do starej nazwy poprawione w portalu, hopie i imporcie; w `context/archive/2026-08-01-commons-refactoring/` **zostały stare nazwy** —
archiwum jest tylko do odczytu i zapisuje stan z dnia zamknięcia.

## Stan zastany — zmierzony 2026-08-21

| co | wartość |
|---|---|
| `maven.compiler.source/target` | **1.8** |
| Spring Framework BOM | 5.2.9.RELEASE (`spring.data.releasetrain` Moore-SR10) |
| modułów | 6 |
| plików `.java` | 113 |
| plików z importem `javax.*` | 19 |
| linii `javax.{persistence,servlet,validation,annotation}` | **24** |
| wersja artefaktu | 6.0 |

Konsumenci: `homeportal-commons-java` w **hop**, **portal** i **hac**; `homeportal-commons-logging`
w **importerze**. Czyli commons dotyka czterech repozytoriów z pięciu.

## ⚠️ Kolejność jest ODWROTNA, niż podpowiada intuicja: commons idzie OSTATNI

Odruch mówi „najpierw biblioteka, potem aplikacje". Tutaj to **złamałoby build trzech projektów**.

**Java 17 czyta bajtkod Javy 8, ale Java 8 NIE odczyta bajtkodu 17** (`UnsupportedClassVersionError`).
Dopóki hop, portal i importer stoją na 8, podniesienie commons do 17 wywala je wszystkie naraz.

**Dowód, że dzisiejszy stan nikomu nie przeszkadza:** `homeportal.hac` chodzi na **Java 17
+ Spring Boot 3.3.5** i konsumuje `homeportal-commons-java` skompilowane do **1.8** — bez problemu,
na produkcji. Czyli commons na 1.8 **nie blokuje** migracji żadnej aplikacji i nie jest warunkiem
wstępnym [[hop-jdk17-migration]], [[hp-jdk17-migration]] ani [[importer-jdk17-migration]].

To odpowiada na punkt 3 z „Do rozstrzygnięcia" tamtych trzech ticketów: **nie, commons nie musi
iść pierwszy.** Może iść ostatni i tak jest bezpieczniej.

## Warunek wejścia

Ten ticket wolno **zamknąć wdrożeniem** dopiero wtedy, gdy **wszystkie cztery** repozytoria
konsumujące artefakt są na 17. Wcześniej można na branchu zbadać koszt — ale nie wydawać wersji.

Jeśli kiedyś zajdzie potrzeba wydania commons na 17 przed migracją konsumentów, jedynym
bezpiecznym wariantem jest **równoległa linia wersji** (np. 6.x na 1.8 i 7.x na 17), a nie
podbicie w miejscu. To osobna decyzja i osobny koszt utrzymania dwóch gałęzi.

## Jak to zbadać, nie łamiąc nikomu builda

1. Branch, podbicie `maven.compiler.release` na 17, `mvn clean install` — zobaczyć, co pęka
   w samej bibliotece (113 plików, więc szybko).
2. Sprawdzić, czy 24 linie `javax.*` w ogóle wymagają ruchu: **dla samego JDK 17 nie wymagają**
   (Jakarta jest wymuszona dopiero przez Spring Boot 3 / Jakarta EE 9 po stronie konsumenta).
3. Zbudować lokalnie **hac** przeciwko tak zbudowanemu commons — to jedyny konsument już na 17,
   więc jedyny, który da odpowiedź bez czekania na pozostałe trzy migracje.

## Kryterium sukcesu

- `mvn clean install` przechodzi na JDK 17 z kompletem testów, bez ich wyłączania;
- `hac` buduje się i **wstaje** przeciwko commons zbudowanemu na 17;
- artefakt **nie** jest wydany ani zmergowany do `master`.

⚠️ „Zbudowało się" nie jest kryterium — biblioteka wchodzi w cztery aplikacje, a skutki
niezgodności bajtkodu i refleksji wychodzą przy starcie kontekstu, nie przy kompilacji.

## Powiązane

[[hop-jdk17-migration]], [[hp-jdk17-migration]], [[importer-jdk17-migration]] — trzy aplikacje
do przeniesienia na 17; ten ticket wchodzi **po nich**, nie przed.

## Zmierzone na 17 — faza 1 (2026-09-08)

Branch `jdk17`, `maven.compiler.release` = 17, Lombok 1.16.14 → **1.18.30**, JDK 17.0.7,
Maven 3.5.0.

- **Kompilacja: przechodzi w komplecie.** `mvn clean install -DskipTests` — wszystkie sześć
  modułów SUCCESS, `javap` na wyjściu pokazuje `major version: 61`. Lombok po podbiciu nie
  zgłasza nic; poza nim nie pękł ani jeden plik. 24 linie `javax.*` nie wymagały ruchu, zgodnie
  z przewidywaniem.
- **Testy: 59 uruchomionych, 9 błędów, wszystkie w jednym miejscu** —
  `FullTextRepositoryIntegrationTest` w module `data`, każdy z tym samym powodem:

  ```
  java.lang.NoClassDefFoundError: javax/xml/bind/JAXBException
      at FullTextRepositoryIntegrationTest.setUp(FullTextRepositoryIntegrationTest.java:83)
  ```

⚠️ **Przyczyna jest inna, niż zakładał plan.** To nie javassist ani wersja Hibernate, tylko
**JAXB usunięty z JDK w Javie 11** (JEP 320). Hibernate 5.0 potrzebuje go do bootstrapu
`EntityManagerFactory`, a na Javie 8 dostawał go z JDK za darmo. Lekarstwem jest dodanie
zależności JAXB, nie podbicie Hibernate — zakres fazy 2 do skorygowania.

⚠️ **Uboczny skutek do posprzątania:** build na branchu zainstalował do `~/.m2` artefakty
**6.0 z bajtkodem 17**. Dopóki nie odtworzymy tam prawdziwego 6.0 (przebudowa z `mastera`
na JDK 8), lokalny build `hop`/`portal`/`importer` na ósemce wywali się na
`UnsupportedClassVersionError`.

## Faza 2 — moduł `data` na zielono (2026-09-08)

Dwie zależności, zero podbić Hibernate. Pełny `mvn clean install` na JDK 17: **BUILD SUCCESS**,
139 testów (59 `java` / 15 `domain` / 59 `data` / 4 `logging` / 2 `mail`), zero wyciszeń.

| co | z czego | na co | po co |
|---|---|---|---|
| JAXB | brak | `javax.xml.bind:jaxb-api` + `org.glassfish.jaxb:jaxb-runtime` **2.3.1** | JEP 320 wyrzucił JAXB z JDK w Javie 11, a Hibernate potrzebuje go do bootstrapu |
| javassist | 3.18.1-GA (tranzytywnie z Hibernate 5.0.10) | **3.29.2-GA** | 3.18 definiuje klasy przez `ClassLoader.defineClass`, co JPMS blokuje od Javy 16 |

⚠️ **Hibernate ORM (5.0.10), Search (5.5.4) i Lucene (5.3.1) zostały nietknięte.** Plan
przewidywał ich podbicie — okazało się niepotrzebne, bo problem siedział wyłącznie w javassiście.
To jest istotne dla konsumentów: 7.0 nie zmienia im wersji Hibernate ani Lucene'a.

JAXB wchodzi w zasięgu `compile` (api) i `runtime` (impl), czyli **jedzie tranzytywnie do
konsumentów** — decyzja usera 2026-09-08: `commons-data` daje im Hibernate, więc powinno dawać
też to, czego Hibernate potrzebuje na 17.

**Baseline z JDK 8 na `masterze`: identyczny** — 59/15/59/4/2, BUILD SUCCESS. Liczba testów nie
spadła. Przy okazji odtworzone w `~/.m2` prawdziwe 6.0 (bajtkod 52), więc lokalne buildy
`hop`/`portal`/`importera` na ósemce znów działają.

## Faza 3 — wersja 7.0 i dowód u konsumenta (2026-09-08)

- Reaktor podbity `6.0` → **7.0** (root + sześć modułów), `mvn clean install` na 17 zielony.
- Sześć artefaktów 7.0 leży w `~/.m2`; `javap` na 7.0 → `major version: 61`, na 6.0 → `52`.
- **`hac` zbudowany przeciw 7.0 z pełnym kompletem testów: BUILD SUCCESS**, wszystkie moduły,
  372 testy bez błędu — w tym testy podnoszące kontekst Spring Boota. Zmiana
  `homeportal.commons.version` 5.0 → 7.0 w `hac/pom.xml` była tymczasowa i została **cofnięta**
  (`git checkout -- pom.xml`, drzewo haca czyste).
- Skok 5.0 → 7.0 przeszedł od razu, więc kontrolny build przeciw 6.0 okazał się niepotrzebny.

## Gałęzie `jdk17` w pozostałych repach (2026-09-08)

Na życzenie usera założone gałęzie `jdk17` (odbite od `mastera`, wypchnięte na `origin`)
w `hac`, `hop`, `portal` i `importer` — **bez podbijania tam wersji**: te repa nie stoją jeszcze
na 17, więc numer mówiący o platformie byłby nieprawdą. Wersję podbija się w ich własnych
ticketach. `spy` pominięty — projekt Pythonowy, nie ma artefaktu Mavena.

Numer **7.0 zostaje** — decyzja usera po rozważeniu mylącej zbieżności z „Javą 1.7".

## ~~Faza 4 wstrzymana~~ — cofnięte tego samego dnia (patrz „Wydanie" niżej)

Nic nie zostało opublikowane ani wypchnięte z tego repo. Stan:

- `mvn deploy` **nie padał** — w GitHub Packages nadal jest tylko 5.0 i 6.0;
- tagu `v7.0` **nie zakładam** — tag wskazywałby wydanie, którego nie ma;
- gałąź `jdk17` żyje **wyłącznie lokalnie**, z trzema commitami (fazy 1-3);
- sześć artefaktów 7.0 leży w `~/.m2` i tyle wystarczy, żeby budować przeciw nim lokalnie
  (`hac` już to zrobił: 372 testy zielone).

Do wznowienia zostaje sama faza 4 z `plan.md`: deploy, tag `v7.0`, push gałęzi i tagu,
a potem przeniesienie folderu ticketu na `mastera` (gałąź nie wchodzi do niego merge'em).

## Wydanie 7.0 — 2026-09-08

User cofnął wstrzymanie („mozesz wydac 7.0"). Opublikowane i otagowane:

| co | wartość |
|---|---|
| commit wydania | **645f5f0** (gałąź `jdk17`) |
| tag | **`v7.0`**, anotowany, na origin |
| artefakty | `homeportal-commons-{java,domain,data,mail,logging,test}:7.0` + `-sources` |
| repozytorium | `https://maven.pkg.github.com/gwrazen/homeportal.commons` |
| weryfikacja | `dependency:get` do czystego `maven.repo.local` ściąga jar, `javap` → `major version: 61` |

Podbite zależności w 7.0 wobec 6.0: Lombok 1.16.14 → 1.18.30, javassist 3.18.1-GA → 3.29.2-GA
(tranzytywny), plus **nowe** `jaxb-api` i `jaxb-runtime` 2.3.1. Hibernate ORM 5.0.10,
Search 5.5.4 i Lucene 5.3.1 **bez zmian**.

`master` został nietknięty: wersja 6.0, `maven.compiler.source/target` 1.8. Linia 6.x dalej
obsługuje `hop`, `portal` i `importer`; `hac` stoi na 5.0. Żadne repo konsumujące nie zostało
podbite do 7.0 — to osobna decyzja, poza tym ticketem.

## CI gałęzi `jdk17` — poprawione 2026-09-08 po wydaniu

Build na GitHubie padał na każdym pushu do `jdk17`: `.github/workflows/build.yml` przypinał
**JDK 8**, a javac z ósemki nie zna flagi `--release` (`invalid flag: --release`). Sam plik to
przewidywał — komentarz mówił, że zmiana wersji JDK należy do tego ticketu.

Na gałęzi `jdk17` oba workflow (`build.yml`, `publish.yml`) budują teraz na **JDK 17**;
na `masterze` zostają na 8, bo linia 6.x to dalej kod Javy 8. Przebieg po poprawce: zielony.

⚠️ Przebieg z tagu `v7.0` pozostaje czerwony w historii Actions — tag wskazuje commit sprzed
poprawki CI. Artefakty 7.0 poszły z lokalnej maszyny, nie z tego workflow, więc na wydanie
to nie wpływa.
