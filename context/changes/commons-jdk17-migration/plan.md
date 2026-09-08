# Migracja homeportal.commons na JDK 17 — wydanie linii 7.0

## Overview

Na branchu `jdk17` (odbitym od `master`, **bez merge'a z powrotem**) podnosimy commons z Javy 8
na 17 i publikujemy wynik do GitHub Packages jako **nową linię major 7.0**. Linia 6.x zostaje
na `masterze` i dalej obsługuje konsumentów stojących na Javie 8.

Zmiana decyzji wobec pierwotnej treści ticketu (2026-09-08): artefakt **wydajemy**, tylko pod
podbitą wersją. Wariant „równoległa linia wersji", który `change.md` wymieniał jako alternatywę,
staje się głównym scenariuszem — a warunek „czekamy, aż wszystkie cztery repa będą na 17" znika,
bo każdy konsument pinuje własną wersję i nie zobaczy 7.0, dopóki sam jej nie wpisze.

## Current State Analysis

Zmierzone 2026-09-08 w `homeportal.commons` na `masterze`:

| co | wartość |
|---|---|
| `maven.compiler.source/target` | **1.8** (`pom.xml:23-24`) |
| wersja artefaktu | **6.0** (`pom.xml:10`) |
| moduły w reaktorze | 6: `java`, `domain`, `data`, `mail`, `logging`, `test` |
| pliki `.java` w reaktorze | 102 |
| Lombok | **1.16.14** (`pom.xml:33`) |
| Spring Framework BOM | 5.2.9.RELEASE, Spring Data Moore-SR10 |
| Hibernate ORM / Search / Lucene | 5.0.10.Final / 5.5.4.Final / 5.3.1 |
| Maven | 3.5.0 (`/Users/grzegorz/Work/apps/apache-maven-3.5.0`) |
| JDK 17 | 17.0.7 w `/Library/Java/JavaVirtualMachines/jdk-17.jdk` |
| tagi w repo | **brak żadnego** |
| gałęzie | `master`, `development`, `commons-refactoring`, `cherry` |

Konsumenci i wersja, na której dziś stoją:

| repo | pinuje | konsumowane moduły |
|---|---|---|
| `hac` (Java 17, Boot 3.3.5) | **5.0** (`pom.xml:32`) | `java`, `logging` |
| `hop` (Java 8) | 6.0 (`pom.xml:24`) | `java`, `data`, `logging` |
| `portal` (Java 8) | 6.0 (`pom.xml:27`) | `java`, `data`, `logging`, `mail`, `test` |
| `importer` (Java 8) | 6.0 (`pom.xml:35`) | `java`, `data`, `logging`, `mail` |

⚠️ To koryguje zapis w `change.md`, który mówił tylko o `commons-java` i `commons-logging`.
Realnie konsumowanych jest **5 z 6 modułów**, a ryzykowny `data` idzie do trzech repozytoriów.

## Desired End State

- Na `origin` stoi gałąź `jdk17` z commons kompilowanym do bajtkodu 17, przechodzącym pełny
  `mvn clean install` na JDK 17 **bez wyłączania ani jednego testu**.
- W GitHub Packages leży `pl.homeportal:homeportal-commons-*:7.0` — sześć artefaktów linii 7.x.
- Commit, z którego poszło wydanie, ma anotowany tag `v7.0` wypchnięty na `origin`.
- `master` jest **nietknięty**: nadal 1.8, nadal wersja 6.0, nadal buduje się na JDK 8.
- `hac` udowodnił lokalnie, że wstaje przeciw 7.0 — ale w jego repo **nie ma commitu**;
  podbicie `homeportal.commons.version` u konsumentów to osobna decyzja i osobny ticket.

Weryfikacja: `mvn dependency:get -Dartifact=pl.homeportal:homeportal-commons-java:7.0` ściąga
artefakt, a `javap -verbose` na dowolnej jego klasie pokazuje `major version: 61`.

### Key Discoveries:

- **Lombok 1.16.14 (`pom.xml:33`) to twardy blocker.** Wersje poniżej 1.18.22 grzebią
  w wewnętrznych API `com.sun.tools.javac` i na JDK 16+ nie startują w ogóle. Lombok jest
  w `java`, `data` i `mail` — bez podbicia nie skompiluje się nic.
- **Maven 3.5.0 działa na JDK 17** — sprawdzone: `JAVA_HOME=…/jdk-17.jdk mvn -v` zgłasza
  `Java version: 17.0.7`. Nie trzeba nowszego Mavena ani `toolchains.xml`.
- **`javax.*` nie wymaga ruchu.** 19 plików, wyłącznie `persistence`, `validation`, `servlet`,
  `mail`, `imageio` — pakiety, które na JDK 17 żyją dalej. Jakarta jest wymuszana przez
  Spring Boot 3 po stronie konsumenta, nie przez sam JDK.
- **Strefa ryzyka to `homeportal-commons-data`**: Hibernate ORM 5.0.10 + Search 5.5.4 + Lucene
  5.3.1, a w testach `FullTextRepositoryIntegrationTest` stawia H2 i indeks Lucene w pamięci.
  Stary javassist i enhancement encji to klasyczne miejsce, gdzie 17 wychodzi w runtime testu,
  a nie przy kompilacji.
- **Katalogi `homeportal-commons-geo-api` i `homeportal-commons-location-api` są martwe** —
  nie ma ich w `<modules>`, a ich `<parent>` wskazuje wersje **4.0** i **4.1**. Nie budują się
  dziś i nie wchodzą do 7.0.
- **`hac` stoi na commons 5.0, nie 6.0.** Test „hac przeciw 7.0" jest więc skokiem o dwie
  wersje — patrz „Critical Implementation Details".

## What We're NOT Doing

- **Nie mergujemy niczego do `mastera`** i nie ruszamy tam wersji 6.0.
- **Nie podnosimy Springa** (5.2.9 / Moore-SR10 zostają) — to ticket `commons-spring-upgrade`.
- **Nie przechodzimy na `jakarta.*`** — to konsekwencja Boota 3 u konsumenta, nie JDK 17.
- **Nie podbijamy wersji commons u konsumentów** i nie commitujemy niczego w `hac`, `hop`,
  `portal` ani `importer`.
- **Nie ożywiamy `geo-api` ani `location-api`.**
- Nie odświeżamy stosu „przy okazji" (Jackson, junit, Guava, Velocity) — podbijamy **wyłącznie**
  to, co blokuje kompilację albo testy na 17.

## Implementation Approach

Cztery fazy, każda z osobnym punktem odcięcia. Najpierw jedna zmienna naraz — kompilator
i Lombok — żeby lista tego, co pęka, była wiarygodna. Potem moduł `data`, bo tam ryzyko jest
realne i zamknięte w jednym miejscu. Dopiero na zielonym buildzie podbijamy wersję na 7.0
i sprawdzamy jedynego konsumenta, który już stoi na 17. Wydanie jest ostatnie i nieodwracalne
— GitHub Packages nie pozwala nadpisać opublikowanej wersji, więc `7.0` mamy jeden raz.

## Critical Implementation Details

**Kolejność w fazie 1 jest nieprzypadkowa.** Lombok trzeba podbić **razem** ze zmianą
kompilatora, w tym samym commicie. Podbicie samego `release` na 17 przy Lomboku 1.16.14 daje
wysyp błędów z `com.sun.tools.javac`, które wyglądają jak niezgodność kodu, a nie są nią —
i cała lista „co pęka" z pierwszego buildu staje się bezużyteczna.

**`release` zastępuje `source`/`target`, nie dokłada się do nich.** Zostawienie obu w `pom.xml`
kończy się tym, że Maven bierze jedno, a IDE drugie.

**Test haca to skok 5.0 → 7.0, czyli dwie wersje.** Jeśli hac nie zbuduje się przeciw 7.0,
zanim uznasz to za problem JDK 17, zbuduj go **najpierw przeciw 6.0** — jeśli padnie tak samo,
to rozjazd API między 5.0 a 6.0, sprawa niezwiązana z tym ticketem.

**Zmiana w `hac/pom.xml` jest tymczasowa i nie wolno jej zacommitować.** Przed fazą 4 wraca
`git checkout -- pom.xml` w repo haca.

## Phase 1: Branch `jdk17`, kompilator na 17 i Lombok

### Overview

Odbicie gałęzi, podbicie poziomu kompilacji i Lomboka w jednym commicie, a potem pomiar:
co poza Lombokiem faktycznie nie przechodzi na 17.

### Changes Required:

#### 1. Gałąź robocza

**Intent**: Cała praca ma stać obok `mastera`, który zostaje linią 6.x na Javie 8.

**Contract**: `git checkout -b jdk17 master`. Nazwa bez prefiksu — zgodnie z konwencją gałęzi
w tym repo (`commons-refactoring`, `development`). Żadnego merge'a do `mastera` na żadnym etapie.

#### 2. Poziom kompilacji i Lombok

**File**: `pom.xml` (root)

**Intent**: Przestawić reaktor na bajtkod 17 i podnieść Lomboka do wersji, która na 17 w ogóle
działa. Bez drugiego pierwsze nie ma prawa się skompilować.

**Contract**: `<maven.compiler.source>` i `<maven.compiler.target>` (linie 23-24) **znikają**,
w ich miejsce wchodzi `<maven.compiler.release>17</maven.compiler.release>`.
`<lombok.version>` z `1.16.14` na `1.18.30`. Reszta `<properties>` bez zmian.

#### 3. Pomiar tego, co pękło

**Intent**: Zbudować na 17 i spisać listę błędów — to ona wyznacza zakres fazy 2, a nie domysły.

**Contract**: `JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn clean install`
z katalogu głównego repo. Wynik (moduł, klasa, komunikat) trafia do `change.md` jako sekcja
„Zmierzone na 17". Jeśli padnie sam surefire (forked VM crash, a nie test), podbij
`maven.surefire.plugin.version` do 3.2.x — to część tej fazy, nie fazy 2.

### Success Criteria:

#### Automated Verification:

- Gałąź istnieje i wychodzi z `mastera`: `git rev-parse --abbrev-ref HEAD` → `jdk17`
- Kompilacja wszystkich sześciu modułów na 17 przechodzi: `mvn clean install -DskipTests`
- Bajtkod jest w wersji 17: `javap -verbose` na klasie z `homeportal-commons-java/target/classes` pokazuje `major version: 61`
- `master` nietknięty: `git diff master --stat` pokazuje wyłącznie pliki z tej fazy

#### Manual Verification:

- Pełny `mvn clean install` odpalony na 17, a jego wynik (zielony albo lista błędów) spisany w `change.md`
- Potwierdzone, że lista błędów nie zawiera już nic od Lomboka

**Implementation Note**: Po tej fazie zatrzymaj się i pokaż listę błędów. Jeśli build jest
zielony od razu, faza 2 kurczy się do samej weryfikacji i tak to zapisz.

---

## Phase 2: Moduł `data` na zielono

### Overview

Doprowadzenie `homeportal-commons-data` do pełnej zieleni na 17 — bez wyłączania testów
i bez wychodzenia poza linię Hibernate 5.x.

### Changes Required:

#### 1. Minimalne podbicie stosu Hibernate

**File**: `pom.xml` (root, sekcja `<properties>`)

**Intent**: Podnieść wyłącznie te wersje, które faktycznie padły w fazie 1 — najbardziej
prawdopodobnie `hibernate.core.version` / `hibernate.entitymanager.version` (dziś 5.0.10.Final)
wraz z ich javassistem, ewentualnie `hibernate.search.orm.version`.

**Contract**: Zostajemy w linii **5.x** (kandydat: 5.6.x, ostatnia linia 5). API pozostaje to
samo, więc `hop`, `portal` i `importer` nie dostają zmiany zachowania, gdy kiedyś przejdą na 7.0.
Jeśli okaże się, że zieleń wymaga wyjścia poza 5.x — **zatrzymaj się i zapytaj**, to inna decyzja
niż ta podjęta w planie.

#### 2. Test integracyjny zostaje włączony

**File**: `homeportal-commons-data/src/test/java/pl/homeportal/commons/data/repository/FullTextRepositoryIntegrationTest.java`

**Intent**: Ten test jest jedynym dowodem, że kodowanie po stronie indeksu i po stronie zapytania
się spotykają (mówi to jego własny javadoc). Ma przejść, nie zostać oznaczony jako ignorowany.

**Contract**: Zero `@Ignore`, zero `<skipTests>`, zero wykluczeń w surefire. Jeśli test wymaga
zmiany, to wyłącznie takiej, która nie osłabia asercji.

### Success Criteria:

#### Automated Verification:

- Pełny build z testami przechodzi na 17: `mvn clean install`
- Żaden test nie został wyciszony: `grep -rn "@Ignore\|skipTests\|<excludes>" homeportal-commons-*/src homeportal-commons-*/pom.xml` nie pokazuje nic nowego wobec `mastera`
- Liczba uruchomionych testów nie spadła wobec buildu na JDK 8 (`Tests run:` w podsumowaniu surefire)

#### Manual Verification:

- Podbite wersje wypisane w `change.md` wraz z powodem („co padało")
- Potwierdzone, że nie wyszliśmy poza linię Hibernate 5.x

**Implementation Note**: Zatrzymaj się przed fazą 3 — od tego miejsca zaczynamy dotykać numeru
wersji, czyli rzeczy widocznej na zewnątrz.

---

## Phase 3: Wersja 7.0 i dowód u konsumenta

### Overview

Podbicie reaktora na 7.0, instalacja lokalna i jedyny dowód, który realnie coś znaczy:
`hac` — konsument już stojący na Javie 17 — buduje się przeciw temu jarowi i **wstaje**.

### Changes Required:

#### 1. Numer wersji w reaktorze

**File**: `pom.xml` (root) + `homeportal-commons-{java,domain,data,mail,logging,test}/pom.xml`

**Intent**: Otworzyć linię 7.x. Major, bo bajtkod jest niezgodny wstecz — konsument na Javie 8
nie może podbić się tu przez pomyłkę.

**Contract**: `<version>6.0</version>` → `7.0` w rootcie i `<parent><version>` → `7.0` w sześciu
modułach reaktora. Zależności wewnętrzne używają `${project.parent.version}`, więc nie wymagają
ruchu. `homeportal-commons-geo-api` i `homeportal-commons-location-api` **zostają na 4.0/4.1** —
są poza reaktorem i poza tym ticketem.

#### 2. Instalacja lokalna i build haca

**Intent**: Sprawdzić 7.0 u jedynego konsumenta na 17, zanim cokolwiek pójdzie na zewnątrz.

**Contract**: `mvn clean install` w commons (JDK 17) kładzie 7.0 w `~/.m2/repository`. Potem
w repo `homeportal.hac`: `homeportal.commons.version` z `5.0` na `7.0` (`pom.xml:32`) —
**zmiana tymczasowa, nigdy niecommitowana** — i `mvn clean install`. Po teście
`git checkout -- pom.xml` w hacu.

#### 3. Start kontekstu haca

**Intent**: Kompilacja niczego tu nie dowodzi. Niezgodności refleksji i proxy wychodzą przy
podnoszeniu kontekstu Springa, na co ticket wprost zwraca uwagę.

**Contract**: Uruchomiony hac przeciw commons 7.0 podnosi kontekst bez wyjątku i odpowiada na
swój endpoint zdrowia. Wystarczy start lokalny — nic nie idzie na produkcję.

### Success Criteria:

#### Automated Verification:

- Wszystkie sześć artefaktów jest w `~/.m2`: `ls ~/.m2/repository/pl/homeportal/homeportal-commons-*/7.0/`
- Build commons zielony po podbiciu wersji: `mvn clean install`
- Build haca przeciw 7.0 przechodzi: `mvn clean install` w `homeportal.hac`
- `hac/pom.xml` wrócił do stanu z repo: `git -C homeportal.hac status --short` jest puste

#### Manual Verification:

- Hac wstaje lokalnie przeciw commons 7.0 — kontekst podniesiony, bez wyjątku w logu
- Potwierdzone, że nic nie zostało wydane: `mvn deploy` jeszcze nie padał
- Jeśli hac nie zbudował się przeciw 7.0 — sprawdzone kontrolnie przeciw 6.0, żeby odróżnić rozjazd 5.0→6.0 od problemu z JDK

**Implementation Note**: **Twarda bramka.** Faza 4 jest nieodwracalna — wchodzi wyłącznie po
Twoim wyraźnym „tak" na wynik tej fazy.

---

## Phase 4: Wydanie 7.0 i ślad po nim

### Overview

Publikacja sześciu artefaktów do GitHub Packages, tag na commicie wydania i zapis w tickecie.

### Changes Required:

#### 1. Publikacja

**Intent**: Wypchnąć linię 7.x tam, skąd konsumenci ją wezmą, gdy przejdą na 17.

**Contract**: `mvn deploy` z brancha `jdk17` na JDK 17. Cel to `distributionManagement` →
`https://maven.pkg.github.com/gwrazen/homeportal.commons` (`pom.xml:66-71`), poświadczenia
z `~/.m2/settings.xml` (serwer o id `github`) — sprawdź, że wpis tam jest, **zanim** odpalisz.

⚠️ GitHub Packages **nie pozwala nadpisać opublikowanej wersji**. `7.0` publikujemy jeden raz;
poprawka po wydaniu to `7.0.1`, nie ponowny deploy.

#### 2. Tag i wypchnięcie gałęzi

**Intent**: Wydajemy z gałęzi, która nie wchodzi do `mastera` — bez taga za pół roku nie da się
odtworzyć, z czego powstało 7.0. Tag przeżywa skasowanie gałęzi, gałąź nie przeżywa porządków.

**Contract**: `git tag -a v7.0 -m "commons 7.0 — JDK 17"` na **dokładnie tym** commicie, z którego
poszedł deploy, potem `git push origin jdk17 v7.0`. To będzie pierwszy tag w tym repo.

#### 3. Zapis w tickecie

**File**: `context/changes/commons-jdk17-migration/change.md`

**Intent**: `change.md` ma mówić prawdę o tym, co jest opublikowane — i o tym, że decyzja
„nie wydajemy" została świadomie zmieniona.

**Contract**: Sekcja „Wydanie" z: sha commitu wydania, datą, listą sześciu artefaktów 7.0,
podbitymi wersjami zależności i zdaniem, że `master` został na 6.0/1.8. Status ticketu na
`implemented`.

### Success Criteria:

#### Automated Verification:

- Artefakt jest do pobrania: `mvn dependency:get -Dartifact=pl.homeportal:homeportal-commons-java:7.0`
- Bajtkod pobranego jara to 17: `javap -verbose` na jego klasie pokazuje `major version: 61`
- Tag jest na origin: `git ls-remote --tags origin` zawiera `v7.0`
- Gałąź jest na origin: `git ls-remote --heads origin jdk17` zwraca commit
- `master` nadal na 6.0: `git show master:pom.xml | grep -m1 "<version>"` → `6.0`

#### Manual Verification:

- Wszystkie sześć artefaktów widocznych w GitHub Packages
- `change.md` zaktualizowany o sekcję „Wydanie" i status `implemented`
- Potwierdzone, że w `hac`, `hop`, `portal` i `importer` nie ma ani jednego commitu z tej pracy

---

## Testing Strategy

### Unit Tests:

- 29 istniejących klas testowych ma przejść na 17 w komplecie — żadnych wyłączeń, żadnych `@Ignore`.
- Szczególnie `ObjectValidatorTest` (`javax.validation` + Hibernate Validator 6.1.5) — walidacja
  opiera się na refleksji, czyli na tym, co na 17 zmienia się najczęściej.

### Integration Tests:

- `FullTextRepositoryIntegrationTest` (H2 + Lucene w pamięci) — jedyny test, który przechodzi
  przez enhancement encji Hibernate. Traktuj go jako główny czujnik tej migracji.

### Manual Testing Steps:

1. Zbuduj commons na JDK 17 i sprawdź `major version: 61` w wyjściowych klasach.
2. Przestaw `homeportal.commons.version` w hacu na `7.0` (bez commitu) i zbuduj hac.
3. Podnieś hac lokalnie — kontekst musi wstać bez wyjątku.
4. Cofnij zmianę w `hac/pom.xml`.
5. Sprawdź, że `master` w commons nadal buduje się na JDK 8 (`j8`, `mvn clean install`).

## Migration Notes

Konsumenci **nie migrują w tym tickecie**. Każdy z nich podbije `homeportal.commons.version`
do 7.0 dopiero, gdy sam stanie na Javie 17 — w swoim tickecie (`hop-jdk17-migration`,
`hp-jdk17-migration`, `importer-jdk17-migration`; w `hac` osobna decyzja, bo skacze z 5.0).
Do tego czasu 6.0 leży w GitHub Packages nietknięte i to ono jest u nich w użyciu.

Poprawka do linii 6.x robi się na `masterze` i wydaje jako 6.1 — gałąź `jdk17` nie jest do tego
potrzebna i nie wolno jej do tego używać.

Wycofanie: 7.0 nie da się usunąć ani nadpisać w GitHub Packages, ale nikt go nie używa, dopóki
nie wpisze go w swoim `pom.xml`. „Rollback" tej zmiany to po prostu niepodbijanie się do 7.0.

## References

- Ticket: `context/changes/commons-jdk17-migration/change.md`
- Bliźniaki w innych repach: `hop-jdk17-migration`, `hp-jdk17-migration`, `importer-jdk17-migration`
- Poprzednia duża zmiana w tym repo (wzór na wydanie i konsumentów):
  `context/archive/2026-08-01-commons-refactoring/` — zwłaszcza `consumers.md` i `migration-6.0.md`
- Ticket sąsiedni, świadomie poza zakresem: `context/changes/commons-spring-upgrade/change.md`

## Progress

> Convention: `- [ ]` pending, `- [x]` done. Append ` — <commit sha>` when a step lands. Do not rename step titles. See `references/progress-format.md`.

### Phase 1: Branch `jdk17`, kompilator na 17 i Lombok

#### Automated

- [x] 1.1 Gałąź `jdk17` istnieje i wychodzi z `mastera`
- [x] 1.2 Kompilacja sześciu modułów na 17 przechodzi (`mvn clean install -DskipTests`)
- [x] 1.3 `javap -verbose` pokazuje `major version: 61`
- [x] 1.4 `master` nietknięty (`git diff master --stat`)

#### Manual

- [x] 1.5 Pełny `mvn clean install` na 17 odpalony, wynik spisany w `change.md`
- [x] 1.6 Lista błędów nie zawiera już nic od Lomboka

### Phase 2: Moduł `data` na zielono

#### Automated

- [ ] 2.1 Pełny build z testami przechodzi na 17 (`mvn clean install`)
- [ ] 2.2 Żaden test nie wyciszony (`grep` na `@Ignore` / `skipTests` / `<excludes>`)
- [ ] 2.3 Liczba uruchomionych testów nie spadła wobec buildu na JDK 8

#### Manual

- [ ] 2.4 Podbite wersje wypisane w `change.md` z powodem
- [ ] 2.5 Potwierdzone, że nie wyszliśmy poza linię Hibernate 5.x

### Phase 3: Wersja 7.0 i dowód u konsumenta

#### Automated

- [ ] 3.1 Sześć artefaktów 7.0 w `~/.m2/repository/pl/homeportal/`
- [ ] 3.2 Build commons zielony po podbiciu wersji
- [ ] 3.3 Build haca przeciw 7.0 przechodzi
- [ ] 3.4 `hac/pom.xml` wrócił do stanu z repo (`git status --short` puste)

#### Manual

- [ ] 3.5 Hac wstaje lokalnie przeciw 7.0 — kontekst bez wyjątku
- [ ] 3.6 Potwierdzone, że nic nie zostało wydane
- [ ] 3.7 (jeśli hac padł) kontrolny build przeciw 6.0 odróżnił rozjazd API od problemu z JDK

### Phase 4: Wydanie 7.0 i ślad po nim

#### Automated

- [ ] 4.1 `mvn dependency:get` ściąga `homeportal-commons-java:7.0`
- [ ] 4.2 Pobrany jar ma `major version: 61`
- [ ] 4.3 Tag `v7.0` jest na origin
- [ ] 4.4 Gałąź `jdk17` jest na origin
- [ ] 4.5 `master` nadal na wersji 6.0

#### Manual

- [ ] 4.6 Sześć artefaktów widocznych w GitHub Packages
- [ ] 4.7 `change.md` uzupełniony o sekcję „Wydanie", status `implemented`
- [ ] 4.8 Zero commitów z tej pracy w `hac`, `hop`, `portal`, `importer`
