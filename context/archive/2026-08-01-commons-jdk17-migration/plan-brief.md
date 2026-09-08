# Migracja commons na JDK 17 — Plan Brief

> Pełny plan: `context/changes/commons-jdk17-migration/plan.md`

## What & Why

`homeportal.commons` kompiluje się do bajtkodu 1.8, podczas gdy `hac` stoi już na Javie 17,
a trzy pozostałe repa mają migrację w planach. Podnosimy bibliotekę na 17 i wydajemy ją jako
**nową linię major 7.0**, zostawiając 6.x nietknięte dla konsumentów na Javie 8.

## Starting Point

Sześć modułów, 102 pliki `.java`, wersja 6.0, `maven.compiler.source/target` = 1.8, Lombok
1.16.14, Spring 5.2.9, Hibernate 5.0.10. Konsumenci pinują własne wersje: `hac` → 5.0,
`hop`/`portal`/`importer` → 6.0. JDK 17.0.7 i Maven 3.5.0 (działa na 17) są na maszynie.

## Desired End State

W GitHub Packages leży sześć artefaktów `pl.homeportal:homeportal-commons-*:7.0` z bajtkodem 17,
zbudowanych z gałęzi `jdk17` oznaczonej tagiem `v7.0`. `master` zostaje na 6.0 i Javie 8.
`hac` udowodnił lokalnie, że wstaje przeciw 7.0 — ale w jego repo nie ma commitu.

## Key Decisions Made

| Decyzja | Wybór | Dlaczego | Źródło |
|---|---|---|---|
| Wydanie artefaktu | Tak, pod podbitą wersją | Zmiana decyzji z 2026-08-21; wersja pinowana u konsumentów sprawia, że wydanie nikogo nie ruszy | User |
| Numer wersji | **7.0** — nowa linia major | Bajtkod niezgodny wstecz; numer ma o tym mówić, a 6.x zostaje dla Javy 8 | Plan |
| Gałąź | `jdk17`, **bez merge'a** do `mastera` | Master dalej buduje się na 8 i obsługuje poprawki do 6.x | User |
| Ślad po wydaniu | Tag `v7.0` + sha w `change.md` | Wydajemy z gałęzi spoza `mastera` — tag przeżywa jej skasowanie | Plan |
| Zakres podbić | Minimum: Lombok + to, co realnie padnie | Jedna zmienna naraz; Spring ma własny ticket `commons-spring-upgrade` | Plan |
| Gdy `data` padnie na 17 | Minimalne podbicie w linii Hibernate 5.x | To samo API, więc konsumenci `data` nie dostają zmiany zachowania | Plan |
| Kryterium wydania | Testy commons + hac zbudowany i **wstający** | Niezgodności refleksji wychodzą przy starcie kontekstu, nie przy kompilacji | Plan |

## Scope

**In scope:** poziom kompilacji 17, Lombok 1.16.14 → 1.18.30, minimalne podbicie Hibernate
(jeśli testy tego wymagają), wersja 7.0 w sześciu modułach reaktora, publikacja do GitHub
Packages, tag `v7.0`.

**Out of scope:** merge do `mastera`, upgrade Springa, przejście na `jakarta.*`, podbicie
wersji commons u konsumentów, martwe katalogi `geo-api` i `location-api` (parent 4.0/4.1,
poza reaktorem).

## Architecture / Approach

Jedna zmienna naraz: najpierw kompilator razem z Lombokiem (bez tego drugiego pierwsze nie
ruszy), potem moduł `data` — jedyne miejsce z realnym ryzykiem runtime — potem numer wersji
i sprawdzenie u konsumenta, a na końcu nieodwracalne wydanie. `master` przez cały czas stoi
nietknięty jako linia 6.x.

## Phases at a Glance

| Faza | Co daje | Główne ryzyko |
|---|---|---|
| 1. Branch, kompilator, Lombok | Wiarygodna lista tego, co pęka na 17 | Lombok 1.16.14 zasypuje build błędami z `com.sun.tools.javac`, jeśli podbić go osobno |
| 2. Moduł `data` na zielono | Pełny build z testami na 17 | Hibernate 5.0.10 / javassist może wymagać wyjścia poza linię 5.x |
| 3. Wersja 7.0 + dowód u haca | Artefakt 7.0 lokalnie, konsument wstaje | Hac stoi na 5.0 — skok o dwie wersje może mylić rozjazd API z problemem JDK |
| 4. Wydanie i tag | 7.0 w GitHub Packages, `v7.0` na origin | Nieodwracalne — GitHub Packages nie pozwala nadpisać wersji |

**Prerequisites:** JDK 17 (jest: 17.0.7), Maven 3.5.0 (działa na 17), poświadczenia do
serwera `github` w `~/.m2/settings.xml`, dostęp do repo `homeportal.hac` do budowy kontrolnej.
**Estimated effort:** jedna sesja na fazy 1-2, druga na 3-4 — o ile `data` nie okaże się cięższe.

## Open Risks & Assumptions

- Zakładam, że Hibernate w linii 5.x wystarczy do zieleni na 17. Jeśli nie — to inna decyzja
  i wracam z pytaniem, zamiast wychodzić poza 5.x na własną rękę.
- Spring 5.2.9 zostaje na 17, choć oficjalne wsparcie tej linii kończy się na JDK 15. Działa,
  ale stanem docelowym nie jest — to zakres `commons-spring-upgrade`.
- Wydanie z gałęzi spoza `mastera` znaczy, że opublikowane 7.0 nie ma odpowiednika na głównej
  gałęzi. Tag `v7.0` to jedyne, co temu przeciwdziała — bez niego ślad ginie przy porządkach.
- Nie wiem z góry, czy hac faktycznie wstanie przeciw 7.0; to właśnie ma sprawdzić faza 3,
  zanim cokolwiek pójdzie na zewnątrz.

## Success Criteria (Summary)

- `mvn clean install` na JDK 17 przechodzi z kompletem testów, bez wyłączania czegokolwiek.
- `hac` buduje się i **wstaje** przeciw commons 7.0 zbudowanemu z gałęzi `jdk17`.
- 7.0 jest w GitHub Packages i otagowane, a `master` nadal wydaje linię 6.x na Javie 8.
