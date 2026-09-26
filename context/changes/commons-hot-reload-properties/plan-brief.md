# Przeładowanie properties bez restartu — Plan Brief

> Full plan: `context/changes/commons-hot-reload-properties/plan.md`

## What & Why

Pokrętła operatora (wyłączniki schedulerów, limity, `css.version`, `geo.blocking.mode`) wymagają dziś
restartu, a zmiana w pliku bez restartu daje rozjazd plik ↔ pamięć. Wspólny mechanizm w commons
podmienia w działającej aplikacji wyłącznie wartości oznaczone jako bezpieczne.

## Starting Point

Cztery aplikacje czytają conf identycznie (`@PropertySource` + `@Value` na polach `ApplicationConfiguration`).
Większość konsumentów czyta getter przy każdym użyciu, więc podmiana pola wystarcza; wyjątkiem jest m.in. `GeoBlockService`.

## Desired End State

Edycja confa portalu działa w ciągu ~5 min bez restartu, z linią w logu „klucz: stara → nowa”. Zmiana klucza
spoza listy daje ostrzeżenie „wymaga restartu”. Operacja JMX `ConfigurationManager.reload()` robi to od ręki i zwraca raport.

## Key Decisions Made

| Decision | Choice | Why (1 sentence) |
| --- | --- | --- |
| Wyzwalacz | data modyfikacji pliku co 5 min + op JMX | automat na co dzień, JMX gdy potrzebna odpowiedź od razu |
| Biała lista | adnotacja `@Reloadable` na polu | decyzja przy polu, bez drugiego spisu kluczy |
| Zakres | commons 7.0 + portal | jedno wdrożenie do sprawdzenia; reszta homixa później |
| Pokrętła z kopią w konsumencie | geo tak (słuchacz), `known-bots.txt` później | geo to najbardziej bolesny przypadek |
| Błędna wartość | odrzucona per klucz, stara zostaje | przeładowanie nie może wpuścić stanu, w którym aplikacja by nie wstała |
| Klasa w commons | zwykła klasa, nie `@Component` | hac i importer nie skanują commons |

## Scope

**In scope:** `@Reloadable`, `PropertiesReloader`, commons 7.0 (bez podbijania wersji); portal: wyłączniki schedulerów, limit dobiegu, `css.version`, klucze `geo.*`, MBean `ConfigurationManager`.

**Out of scope:** hac/hop/importer, `known-bots.txt` i inne pliki, `cron`, pule wątków, TTL-e cache, unieważnianie `html_cache` po `css.version`.

## Phases at a Glance

| Phase | What it delivers | Key risk |
| --- | --- | --- |
| 1. commons 7.0 | mechanizm + testy + publikacja | 7.0 na GitHub Packages nienadpisywalna — HAC nie dostanie mechanizmu tą drogą |
| 2. portal — proste pokrętła | timer 5 min, JMX, pola oznaczone | konsument kopiujący wartość mimo oznaczenia |
| 3. portal — geo | przełączanie blokady krajów w locie | spójność tablic zakresów przy przeładowaniu w trakcie ruchu |

**Prerequisites:** dostęp do publikacji commons (GitHub Packages albo mavenLocal).
**Estimated effort:** ~2 sesje.

## Open Risks & Assumptions

- Plik zapisywany w trakcie odczytu — łagodzi to zwłoka ~2 s po zmianie daty.
- Wartości z `${...}` w pliku odrzucane w pierwszej wersji.

## Success Criteria (Summary)

- Zmiana limitu dobiegu na prodzie widoczna w następnym przebiegu (`/scheduls`) bez restartu.
- `geo.blocking.mode` przełączany bez restartu.
- Żaden klucz nie zmienia się po cichu: zmienione albo „wymaga restartu” w logu.
