# Migracja builda commons na Gradle'a — Plan Brief

> Pełny plan: `context/changes/commons-gradle-migration/plan.md`
> Frame brief: `context/changes/commons-gradle-migration/frame.md`

## What & Why

Przenosimy build sześciu modułów commons z Mavena na Gradle'a na gałęzi `jdk17` (linia 7.x).
Motywem jest **spójność homixa** — jedno narzędzie w każdym repo — a nie żaden zmierzony ból:
build commons jest najuboższy w stawce i nic w nim dziś nie zawodzi. Frame ustalił, gdzie leży
prawdziwe ryzyko: rozmiar builda i promień rażenia to w tym systemie dwie różne osie, a commons
jest w minimum jednej i maksimum drugiej. Jego produkt to kontrakt czytany przez trzy aplikacje
produkcyjne, a rejestr przyjmuje wersję raz i nieodwracalnie.

## Starting Point

Build ubogi: siedem wtyczek wyłącznie z przypiętymi wersjami, jedna aktywna, zero profili i zero
generowania kodu. Drogi jest produkt: publikowane pomy są dosłowną kopią źródła, z `<parent>`
i wersjami rozwiązywanymi dopiero u konsumenta z dwóch BOM-ów; trzy wpisy `dependencyManagement`
przykrywają wersje **tranzytywne**; pięć zależności ma scope `provided`; `commons-domain` dociera
do konsumentów tranzytywnie, bez jawnej deklaracji. Konsumenci: portal, hac i importer na 7.0,
hop na 6.0 z `mastera`.

## Desired End State

Na gałęzi `jdk17` build prowadzi Gradle, a w GitHub Packages leży **7.1**: sześć jarów, sześć
`-sources.jar`, sześć płaskich pomów, zero plików `.module`. Portal, hac i importer budują się
przeciw 7.1 bez zmian we własnych pomach, a diff ich drzew zależności wobec 7.0 jest pusty poza
numerem wersji. Maven znika z gałęzi `jdk17`; `master` zostaje na Mavenie i na 6.0.

## Key Decisions Made

| Decyzja | Wybór | Dlaczego | Źródło |
| --- | --- | --- | --- |
| Kolejność w homixie | commons pierwszy | Decyzja usera po przeczytaniu frame'u — zastrzeżenie o wartości poznawczej poligonu podniesione i świadomie odrzucone | Frame |
| Linia | tylko `jdk17` (7.x) | Trzech z czterech konsumentów czyta 7.x; linia 6.x ma umrzeć, więc nie wkładamy w nią pracy | Plan |
| Kształt metadanych | płaskie pomy + `constraints` | Idiomatyczne dla Gradle'a i zachowuje przykrycie `javassist 3.29.2-GA`, od którego zależy start EMF na JDK 17 | Plan |
| `provided` | zachowane w pomie | Pom jest kontraktem; moduł `-mail` już raz położył pocztę na produkcji przez złą współrzędną zależności | Plan |
| Gradle Module Metadata | wyłączone | Wszyscy konsumenci to Maven; `.module` wygrywałby z pomem, który weryfikujemy | Plan |
| Bramka | trzej konsumenci na 7.0 | Hop stoi na 6.0/JDK 8, więc przeciw 7.x nie da się go zbudować | Plan |
| Pierwsza publikacja | izolowany staging `file://` | Rejestr nietykany do momentu dowodu; build na gałęzi już raz zatruł `~/.m2` | Plan |
| Wydanie | z CI, nie z laptopa | Przy 7.0 artefakty poszły lokalnie, a przebieg z tagu `v7.0` do dziś jest czerwony | Plan |
| Zakres | nic ponad build i CI | Gdy bramka pokaże różnicę, ma być jasne, że wina jest w migracji | Plan |

## Scope

**In scope:** build Gradle dla sześciu modułów na gałęzi `jdk17` · konfiguracja publikacji ·
oba workflowy CI · wydanie 7.1 · usunięcie pomów z gałęzi `jdk17`.

**Out of scope:** `master` i linia 6.0 · migracja hopa na 7.0 · podbicie wersji commons
u konsumentów · JUnit 5 · martwe moduły `geo-api` i `location-api` · podnoszenie wersji zależności ·
naprawa globalnych skilli `mb`/`md`/`mbd` (osobne repo, osobny ticket).

## Architecture / Approach

Dowód musi powstać **zanim** rejestr zostanie dotknięty. Stąd kolejność: baseline → build bez
publikacji → metadane wypchnięte do katalogu na dysku → trzej konsumenci przeciw temu katalogowi →
CI → wydanie. Maven zostaje w drzewie przez pięć faz jako punkt odniesienia dla bramek i znika
dopiero po zielonym wydaniu.

## Phases at a Glance

| Faza | Co dostarcza | Główne ryzyko |
| --- | --- | --- |
| 1. Baseline | Drzewa zależności trzech konsumentów, 139 testów, zbiory ścieżek w jarach | Pomiar zrobiony niechlujnie unieważnia wszystkie późniejsze bramki (`grep` bez `-a` daje 133) |
| 2. Build Gradle | Sześć modułów kompiluje się i testuje, Maven nietknięty obok | `useJUnit()` pominięte → zero testów przy zielonym buildzie |
| 3. Metadane + staging | 7.1 w katalogu `file://`, pomy w ustalonym kształcie | `implementation` zamiast `api` → scope `runtime` w pomie, pom wygląda poprawnie |
| 4. Bramka konsumentów | Pusty diff drzew u portala, haca i importera | Utrata `constraints` → javassist 3.18.1-GA i awaria dopiero przy starcie kontekstu |
| 5. CI | Oba workflowy zielone, uwierzytelnienie przećwiczone | Ścieżka Gradle → Packages nigdy tu nie przeszła przez CI; wersja kontrolna zostaje w rejestrze |
| 6. Wydanie 7.1 | Artefakt w rejestrze, tag, Maven usunięty z gałęzi | Nieodwracalne — rejestr przyjmuje wersję raz, rollbackiem jest tylko kolejny numer |

**Prerequisites:** JDK 17 jako `JAVA_HOME` · dostęp do GitHub Packages z uprawnieniem `packages: write` ·
czyste drzewa w `portal`, `hac` i `importer` na czas fazy 4 · Gradle nie musi być zainstalowany (wrapper).

**Estimated effort:** ~3-4 sesje. Fazy 1-3 idą szybko, faza 4 to trzy pełne buildy aplikacji
(kilkanaście minut na przebieg), fazy 5-6 zależą od przebiegów CI.

## Open Risks & Assumptions

- Zakładam, że `constraints` trafiają do `<dependencyManagement>` publikowanego pomu. Gdyby się
  okazało, że nie — faza 4 to wychwyci (javassist), a decyzja o kształcie metadanych wraca do
  rozstrzygnięcia; nie obchodzę tego na własną rękę.
- Wersja kontrolna `7.1-ci-check` zostanie w rejestrze na zawsze. To świadomy koszt przećwiczenia
  ścieżki publikacji przed użyciem numeru docelowego.
- Po fazie 6 skille `mb`, `md` i `mbd` przestają działać w tym repo. Naprawa należy do innego
  ticketu i innego repozytorium.
- Linia 6.x zostaje na Mavenie do czasu przejścia hopa na JDK 17 — repo ma przejściowo dwa systemy
  budowania na dwóch gałęziach.

## Success Criteria (Summary)

- `mvn dependency:get` do czystego repozytorium ściąga sześć modułów 7.1, a `javap` pokazuje
  `major version: 61`.
- Portal, hac i importer budują się przeciw 7.1 bez zmian w swoich pomach, z pustym diffem drzew
  zależności.
- 139 testów, zero wyciszeń, na każdym etapie.
