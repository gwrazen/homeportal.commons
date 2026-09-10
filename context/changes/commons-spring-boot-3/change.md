---
change_id: commons-spring-boot-3
title: Linia commons na jakarcie jako wersja 8.0 — warunek wstępny Boota 3 w portalu, hopie i importerze, a przy okazji jedyne miejsce, gdzie hac już dziś nosi bombę z opóźnionym zapłonem
status: new
created: 2026-09-10
updated: 2026-09-10
archived_at: null
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
