# Migracja builda commons na Gradle'a — linia 7.x

## Overview

Przeniesienie builda sześciu modułów commons z Mavena na Gradle'a **na gałęzi `jdk17`** (linia 7.x)
tak, żeby opublikowany artefakt pozostał nieodróżnialny dla trzech konsumentów, którzy go czytają.
Efektem jest wydanie **7.1** — zbudowane Gradle'em, wypuszczone z CI, z dowodem równoważności
zebranym zanim rejestr zostanie dotknięty.

## Current State Analysis

Build jest ubogi: 7 wtyczek wyłącznie z przypiętymi wersjami, **jedna** aktywna
(`maven-source-plugin` / `attach-sources`), zero profili, zero filtrowania zasobów, zero generowania
kodu, jeden plik zasobów testowych. Port samych zadań jest mechaniczny.

Drogi jest **produkt tego builda**, nie sam build:

- Publikowane pomy są **dosłowną kopią plików źródłowych** — Maven nie robi flatteningu. Każdy
  z sześciu modułów ma `<parent>pl.homeportal:homeportal-commons:7.0</parent>` i deklaruje
  zależności **bez wersji**; wersje rozwiązują się dopiero u konsumenta, z `dependencyManagement`
  agregatora (34 wpisy, w tym dwa importy BOM: `spring-framework-bom:5.2.9.RELEASE`
  i `spring-data-releasetrain:Moore-SR10`).
- **Trzy wpisy `dependencyManagement` nie są używane przez żaden moduł** — istnieją wyłącznie po to,
  żeby przykryć wersje **tranzytywne**: `org.javassist:javassist:3.29.2-GA`, `commons-io:2.6`,
  `org.apache.commons:commons-compress:1.0`.
- **Pięć zależności ma scope `provided`** i jest częścią kontraktu: `lombok`, `javax.servlet:servlet-api`
  (obie z DM), `commons-email` i `velocity` w `-mail`, `spring-webmvc` i `spring-test` w `-test`.
- `homeportal-commons-domain` **nie jest deklarowany przez żadnego konsumenta**, a portal (145 importów),
  hop (83) i importer (18) kompilują się przeciw jego klasom — dociera tranzytywnie przez `-data`
  i `-logging`.
- Rejestr przyjmuje wersję **raz**: `409 Conflict` przy nadpisaniu, a jedyną drogą wstecz jest
  kasowanie pakietu. W tym repo zdarzyło się to dwa razy, raz zabierając przy okazji sąsiednią wersję.

Konsumenci: `portal`, `hac`, `importer` na **7.0**; `hop` na **6.0** (linia z `mastera`, JDK 8).
Repozytorium GitHub Packages deklaruje w pomie **wyłącznie `hac`** (`homeportal.hac/pom.xml:58-65`) —
portal, hop i importer rozwiązują commons z lokalnego `~/.m2`, a `~/.m2/settings.xml` nie ma
żadnego `<repositories>`.

## Desired End State

Na gałęzi `jdk17` build prowadzi Gradle. W GitHub Packages leży **7.1**: sześć jarów + sześć
`-sources.jar` + sześć pomów, bez pliku `.module`. Pomy są płaskie (bez `<parent>`), ale niosą
`<dependencyManagement>` z trzema nadpisaniami tranzytywnymi i zachowują pięć wpisów `provided`.
`portal`, `hac` i `importer` budują się przeciw 7.1 bez zmian we własnych pomach, a diff ich drzew
zależności wobec 7.0 jest **pusty poza numerem wersji commons**.

Weryfikacja: `mvn dependency:get -Dartifact=pl.homeportal:homeportal-commons-java:7.1` do **czystego**
`maven.repo.local` ściąga artefakt, a `javap -verbose` na jego klasie pokazuje `major version: 61`.

### Key Discoveries:

- Konwencja weryfikacji istnieje i jest sprawdzona: `dependency:get` do izolowanego repo + `javap`,
  porównanie przeciw poprzedniej linii, baseline testów mierzony osobnym krokiem, zakaz wyciszania
  sprawdzany grepem (`context/archive/2026-08-01-commons-jdk17-migration/plan.md:316-322, 205-206`).
- Baseline testów: **139** w rozkładzie `-java` 59 / `-domain` 15 / `-data` 59 / `-logging` 4 /
  `-mail` 2 / `-test` 0; klasy: 32 / 17 / 23 / 8 / 1 / 1.
- ⚠️ `StringUtilsTest.java` zawiera znaki spoza ASCII — BSD `grep` uznaje plik za binarny i **cicho**
  go pomija. Naiwny `grep -c '@Test'` daje 133 zamiast 139; liczyć `grep -a` albo z raportów surefire.
- Jedyne zasoby niebędące klasami to **11 szablonów `mail/*.vm`** w `-mail`; pozostałe pięć modułów
  nie ma w jarze ani jednego pliku spoza `.class` i wspólnego szkieletu `META-INF`.
- Rollbacku technicznie nie ma. Działa wyłącznie to, że konsument pinuje wersję: poprawka po wydaniu
  to **kolejny numer**, nigdy ponowny deploy (`archive/…/plan.md:292-293`).
- Gradle nie jest zainstalowany na stanowisku (`which gradle` → brak), `mvn -v` → 3.5.0 na JDK 1.8.0_222.
  Wrapper rozwiązuje pierwsze; build linii 7.x i tak wymaga `JAVA_HOME` na 17.

## What We're NOT Doing

- **Nie ruszamy `mastera`** ani linii 6.0 — zostaje na Mavenie do czasu wygaszenia (osobna decyzja).
- **Nie migrujemy hopa na 7.0** — to warunek wygaszenia linii 6.x, osobny ticket w `hop`.
- **Nie podbijamy wersji commons u konsumentów** i nie commitujemy niczego w `portal`, `hac`,
  `importer` ani `hop`. Zmiany w ich pomach w fazie 4 są tymczasowe i wracają przez `git checkout`.
- **Nie przechodzimy na JUnit 5** — 27 klas zostaje na JUnit 4.
- **Nie ożywiamy ani nie usuwamy `geo-api` i `location-api`** — są poza reaktorem i poza tym ticketem.
- **Nie podnosimy żadnej wersji zależności** — ani Springa, ani Hibernate'a, ani Lomboka.
  Rozwiązane wersje mają zostać co do numeru takie same jak w 7.0.
- **Nie naprawiamy skilli `mb`/`md`/`mbd`** — żyją w `homeportal.ai.registry`, są globalne dla całego
  homixa, więc zmiana tam należy do osobnego ticketu. Po fazie 6 przestają działać w tym repo i to
  jest świadomy, zapisany koszt.
- **Nie sprzątamy osieroconego `homeportal-commons/6.1/`** z lokalnego `~/.m2` — to stan stanowiska,
  nie repozytorium.

## Implementation Approach

Sześć faz, ciętych po niezależnych ryzykach, zgodnie z konwencją tego repo: faza 1 produkuje
**pomiar**, a nie postęp; faza ostatnia jest jedyną nieodwracalną i wchodzi wyłącznie po wyraźnym
„tak" usera.

Kolejność jest podyktowana tym, że dowód musi powstać **zanim** rejestr zostanie dotknięty: najpierw
baseline, potem build bez publikacji, potem metadane wypchnięte do izolowanego katalogu, potem trzej
konsumenci przeciw temu katalogowi, potem CI, i dopiero na końcu wydanie. Maven zostaje w drzewie
przez pięć faz jako punkt odniesienia — znika dopiero po zielonym wydaniu.

## Critical Implementation Details

**`api`, nie `implementation`.** Domyślne `implementation` w Gradle publikuje zależność ze scope'em
`runtime`. Trzej konsumenci kompilują się przeciw klasom `commons-domain`, którego **nie deklarują
wprost** — dociera do nich tranzytywnie przez `-data` i `-logging`. Zależności dziś w scope `compile`
muszą trafić do `api`, inaczej trzy buildy przestaną się kompilować, a pom będzie wyglądał poprawnie.

**Trzy nadpisania tranzytywne muszą przetrwać jako `constraints`.** `javassist 3.29.2-GA` przykrywa
3.18.1-GA przychodzące spod Hibernate 5.0.10; starsza wersja definiuje klasy przez
`ClassLoader.defineClass`, co JPMS blokuje od Javy 16. Utrata tego wpisu wychodzi **przy starcie
kontekstu u konsumenta**, nie przy kompilacji commons.

**`useJUnit()` jest obowiązkowe.** Wszystkie 27 klas to czysty JUnit 4. Gradle 8+ domyślnie
uruchamia JUnit Platform i przy projekcie bez silnika vintage wykrywa **zero testów, kończąc się
sukcesem** — nieodróżnialnie od zielonego builda.

**Lombok wymaga dwóch deklaracji** (`compileOnly` + `annotationProcessor`, wersja 1.18.30). Dziś
działa mechanizmem `META-INF/services` na compile classpath i nie ma `annotationProcessorPaths`,
więc nie ma z czego tego przepisać — trzeba to napisać od zera.

**Izolacja lokalnego repozytorium.** Przy migracji na JDK 17 build na gałęzi zainstalował do `~/.m2`
artefakty 6.0 z bajtkodem 17 i położył lokalne buildy trzech konsumentów
(`archive/…/change.md:138-141`). Każda instalacja i każde `dependency:get` w tym planie idą do
**osobnego** `maven.repo.local`, nigdy do domyślnego.

---

## Phase 1: Baseline

### Overview

Zmierzyć i zapisać stan wyjściowy jako pliki referencyjne w folderze ticketu. Bez tego żadna
późniejsza bramka nie ma z czym porównywać. Zero zmian w buildzie.

### Changes Required:

#### 1. Drzewa zależności trzech konsumentów

**File**: `context/changes/commons-gradle-migration/baseline/deps-{portal,hac,importer}.txt`

**Intent**: Utrwalić, jak dziś rozwiązuje się commons 7.0 u każdego z trzech konsumentów — to jest
właściwa definicja kontraktu, ostrzejsza niż tekst pomu.

**Contract**: Wyjście `mvn -B dependency:tree` odpalone w korzeniu każdego z trzech repozytoriów,
na jego bieżącej gałęzi, zapisane bez obcinania. Plik musi zawierać wpisy
`pl.homeportal:homeportal-commons-*:jar:7.0`.

#### 2. Zawartość sześciu artefaktów 7.0

**File**: `context/changes/commons-gradle-migration/baseline/jar-paths.txt`

**Intent**: Zapisać zbiór ścieżek plików w każdym z sześciu jarów, żeby faza 2 mogła udowodnić,
że Gradle spakował to samo.

**Contract**: Dla każdego modułu posortowana lista wpisów z jara **z pominięciem katalogów**
(wpisy zerowej długości kończące się `/`) i z pominięciem `META-INF/MANIFEST.MF`. Źródłem są
artefakty 7.0 z `~/.m2`. Osobno wypisane 11 plików `mail/*.vm` z `-mail`.

#### 3. Baseline testów

**File**: `context/changes/commons-gradle-migration/baseline/tests.txt`

**Intent**: Utrwalić liczbę testów per moduł jako bramkę dla faz 2-6.

**Contract**: Rozkład `59 / 15 / 59 / 4 / 2 / 0` (java / domain / data / logging / mail / test),
suma **139**, oraz wynik `mvn -B clean test` na gałęzi `jdk17` z JDK 17 (linie `Tests run:`).
⚠️ Zliczanie z kodu wyłącznie przez `grep -a` — bez tego `StringUtilsTest.java` wypada cicho.

### Success Criteria:

#### Automated Verification:

- Trzy pliki `deps-*.txt` istnieją, są niepuste i każdy zawiera `homeportal-commons-java:jar:7.0`
- `jar-paths.txt` wypisuje sześć modułów, a sekcja `-mail` zawiera dokładnie 11 wpisów `mail/*.vm`
- `mvn -B clean test` na `jdk17` z JDK 17 kończy się `BUILD SUCCESS`, a suma `Tests run:` to 139
- `grep -ac '@Test'` po `src/test` daje rozkład 59 / 15 / 59 / 4 / 2 / 0
- `git status --short` w commons pokazuje wyłącznie pliki z folderu ticketu

#### Manual Verification:

- Trzy repozytoria konsumenckie stały na czystym drzewie w chwili pomiaru; odstępstwa odnotowane
  w `change.md` (importer ma cztery zmienione pliki `.class` w `target/`, śledzone przez gita)
- Baseline zacommitowany **przed** pierwszą zmianą w buildzie

**Implementation Note**: Zatrzymaj się i pokaż rozkład testów oraz trzy drzewa zależności. Jeśli
liczba testów nie wyjdzie 139, to jest sygnał o środowisku (`JAVA_HOME`, `grep`), a nie o buildzie —
rozstrzygnij to przed fazą 2, bo cała reszta planu porównuje się do tej liczby.

---

## Phase 2: Build Gradle bez publikacji

### Overview

Sześć modułów kompiluje się i przechodzi testy pod Gradle'em, przy nietkniętych pomach obok.
Bramką jest parzystość z baseline'em, nie „zbudowało się".

### Changes Required:

#### 1. Szkielet projektu

**File**: `settings.gradle.kts`, `gradle/wrapper/*`, `gradlew`, `gradlew.bat`

**Intent**: Postawić projekt wielomodułowy z wrapperem, bo Gradle nie jest zainstalowany na
stanowisku i wersja musi być przypięta w repo.

**Contract**: Sześć modułów w kolejności `java, domain, data, mail, logging, test`, nazwy katalogów
i artefaktów bez zmian (`homeportal-commons-*`). Wrapper z przypiętą wersją Gradle'a; build wymaga
JDK 17.

#### 2. Build główny

**File**: `build.gradle.kts`

**Intent**: Odtworzyć to, co dziś robi agregator: grupę, wersję, poziom kompilatora, wspólne wersje
zależności i dołączanie źródeł do publikacji.

**Contract**: `group = pl.homeportal`, `version = 7.1`, toolchain Java 17, `withSourcesJar()`,
`useJUnit()` dla wszystkich modułów. Dwa BOM-y (`spring-framework-bom:5.2.9.RELEASE`,
`spring-data-releasetrain:Moore-SR10`) wciągnięte jako platformy. Trzy nadpisania tranzytywne
(`javassist:3.29.2-GA`, `commons-io:2.6`, `commons-compress:1.0`) jako `constraints`. Lombok 1.18.30
w dwóch deklaracjach: `compileOnly` i `annotationProcessor`.

#### 3. Buildy modułów

**File**: `homeportal-commons-{java,domain,data,mail,logging,test}/build.gradle.kts`

**Intent**: Przenieść zależności każdego modułu 1:1 z jego poma, z zachowaniem widoczności
i scope'ów.

**Contract**: Zależności dziś w scope `compile` idą do **`api`** (nie `implementation`) — dotyczy to
w szczególności zależności międzymodułowych: `-data` → `-java`, `-domain`; `-mail` → `-java`,
`-logging`; `-logging` → `-java`, `-domain`; `-domain` i `-test` bez zależności wewnętrznych.
`jaxb-runtime` w `-data` zostaje w `runtime`. `h2`, `junit`, `hamcrest-all`, `slf4j-simple`
w `test`. Pięć zależności `provided` (`lombok`, `servlet-api`, `commons-email`, `velocity`,
`spring-webmvc`, `spring-test`) na tym etapie jako `compileOnly` — ich obecność w metadanych
załatwia faza 3. Zasoby: `mail/*.vm` w `-mail`, `META-INF/persistence.xml` w testach `-data`.

### Success Criteria:

#### Automated Verification:

- `./gradlew build` przechodzi na JDK 17 z `BUILD SUCCESSFUL`
- Liczba uruchomionych testów to **139**, w rozkładzie 59 / 15 / 59 / 4 / 2 / 0 (raporty
  `build/test-results/test/*.xml`)
- Żaden test nie został wyciszony: `grep -rn "@Ignore\|@Disabled\|exclude" homeportal-commons-*/src`
  nie pokazuje nic nowego wobec baseline'u
- `javap -verbose` na klasie z `homeportal-commons-java/build/classes` pokazuje `major version: 61`
- Zbiór ścieżek w sześciu jarach z `build/libs` jest identyczny z `baseline/jar-paths.txt`
  (bez katalogów i `MANIFEST.MF`); `-mail` niesie 11 plików `mail/*.vm`
- Liczba plików `.class` per moduł: 32 / 17 / 23 / 8 / 1 / 1
- Maven dalej działa obok: `mvn -B clean test` nadal `BUILD SUCCESS` ze 139 testami

#### Manual Verification:

- Porównanie wyjścia obu buildów odnotowane w `change.md` — w szczególności każdy przypadek,
  w którym liczba testów albo zbiór plików się rozjechał i co było przyczyną

**Implementation Note**: Zatrzymaj się przed fazą 3. Od tego miejsca zaczynamy produkować metadane,
czyli rzecz widoczną na zewnątrz. Jeśli `./gradlew build` pokazuje mniej niż 139 testów — najpierw
sprawdź `useJUnit()`, bo zero testów przy zielonym buildzie jest w Gradle stanem domyślnym dla
JUnit 4, a nie awarią.

---

## Phase 3: Metadane i publikacja do izolowanego stagingu

### Overview

Wypchnięcie 7.1 do katalogu na dysku i sprawdzenie, że opublikowane pliki mają dokładnie ten
kształt, na który się umówiliśmy. Rejestr pozostaje nietknięty.

### Changes Required:

#### 1. Konfiguracja publikacji

**File**: `build.gradle.kts`

**Intent**: Publikować komplet artefaktów w kształcie ustalonym w frame briefie: płaskie pomy
z `dependencyManagement`, zachowane wpisy `provided`, bez metadanych Gradle'a.

**Contract**: `maven-publish` publikuje dla każdego z sześciu modułów jar + `-sources.jar` + pom.
Generowanie `.module` **wyłączone**. Pięć zależności `provided` musi znaleźć się w wygenerowanym
pomie ze scope'em `provided`. `constraints` mają wylądować w `<dependencyManagement>` pomu każdego
modułu. Repozytorium docelowe wybierane parametrem: katalog `file://` na czas faz 3-4, GitHub
Packages dopiero w fazie 6.

#### 2. Katalog stagingowy

**File**: `/tmp/commons-staging` (poza repo)

**Intent**: Odseparować artefakt eksperymentalny od `~/.m2`, którym na co dzień budują się cztery
aplikacje.

**Contract**: Publikacja wyłącznie do tego katalogu; **żadnego `publishToMavenLocal`** w tej fazie.

### Success Criteria:

#### Automated Verification:

- Staging zawiera dla każdego z sześciu modułów: `*.jar`, `*-sources.jar`, `*.pom` pod wersją `7.1`
- `find /tmp/commons-staging -name '*.module' | wc -l` zwraca **0**
- Każdy z sześciu pomów zawiera komplet zależności tego modułu ze scope'ami zgodnymi z baseline'em
  (`compile` → brak scope'u lub `compile`, `runtime` dla `jaxb-runtime` w `-data`, `test` dla
  `h2`/`junit`/`hamcrest-all`/`slf4j-simple`)
- Pomy zawierają pięć wpisów `<scope>provided</scope>` w modułach, w których dziś obowiązują
  (`-java`: lombok, servlet-api; `-data`: lombok; `-mail`: lombok, commons-email, velocity;
  `-test`: spring-webmvc, spring-test)
- `<dependencyManagement>` niesie trzy nadpisania: `javassist:3.29.2-GA`, `commons-io:2.6`,
  `commons-compress:1.0`
- Rozwiązane wersje zgodne z 7.0: Spring 5.2.9.RELEASE, Spring Data Moore-SR10, Hibernate ORM
  5.0.10.Final, Hibernate Search 5.5.4.Final, Lucene 5.3.1, JAXB 2.3.1, xercesImpl 2.12.2
- `~/.m2/repository/pl/homeportal` nie zawiera niczego w wersji 7.1

#### Manual Verification:

- Ręczne przejrzenie pomu `-mail` i `-data` — dwóch najbardziej wrażliwych; potwierdzenie,
  że `commons-email` i `velocity` nie wjechały na runtime classpath, a `jaxb-runtime` nie awansował
  do `compile`

**Implementation Note**: Zatrzymaj się przed fazą 4. Pom, który tu powstał, jest tym, co zobaczą
konsumenci — jeśli coś ma się rozjechać, to rozjedzie się właśnie tutaj, a faza 4 tylko to pokaże.

---

## Phase 4: Bramka u trzech konsumentów

### Overview

Portal, hac i importer budują się przeciw artefaktowi ze stagingu. To jest właściwy dowód
równoważności — pom można przeczytać, ale dopiero rozwiązanie u konsumenta pokazuje prawdę.

### Changes Required:

#### 1. Tymczasowe przestawienie trzech konsumentów

**File**: `homeportal.{portal,hac,importer}/pom.xml` — zmiana tymczasowa, **nigdy niecommitowana**

**Intent**: Skierować konsumenta na staging i podbić wersję commons na 7.1, żeby zmierzyć drzewo
zależności przeciw artefaktowi z Gradle'a.

**Contract**: `homeportal.commons.version` → `7.1` plus wskazanie repozytorium `file:///tmp/commons-staging`.
Po zebraniu wyników w każdym repo wraca `git checkout -- pom.xml`. Żaden z trzech konsumentów nie
dostaje commita w tym tickecie.

#### 2. Porównanie drzew

**File**: `context/changes/commons-gradle-migration/baseline/deps-*-after.txt`

**Intent**: Udowodnić, że jedyną różnicą wobec fazy 1 jest numer wersji commons.

**Contract**: `mvn -B dependency:tree` w tych samych miejscach co w fazie 1, z izolowanym
`maven.repo.local`, żeby rozwiązanie szło ze stagingu, a nie z `~/.m2`. Diff wobec baseline'u pusty
po odfiltrowaniu `7.0` → `7.1`.

### Success Criteria:

#### Automated Verification:

- `mvn -B clean install` przechodzi w `portal`, `hac` i `importer` przeciw artefaktowi ze stagingu
- Diff `deps-portal.txt` ↔ `deps-portal-after.txt` po podmianie `7.0`→`7.1` jest **pusty**;
  to samo dla `hac` i `importer`
- Portal kompiluje klasy używające `pl.homeportal.commons.data.model.*` (145 importów) — czyli
  `commons-domain` dociera tranzytywnie
- `mvn -B dependency:tree -Dincludes=org.javassist:*` u konsumenta pokazuje **3.29.2-GA**, nie 3.18.1-GA
- `mvn -B dependency:tree -pl <moduł> -Dincludes=org.apache.velocity:*` nie pokazuje `velocity`
  na runtime classpath konsumenta
- Liczba testów u konsumentów nie spadła wobec ich zwykłego buildu
- `git status --short` w `portal`, `hac` i `importer` czysty po przywróceniu pomów

#### Manual Verification:

- Hac podnosi kontekst Spring Boota w swoich testach — potwierdzenie, że nie ma regresji refleksji
  ani proxy (kompilacja tego nie dowodzi)
- Wynik trzech buildów spisany w `change.md`, wraz z liczbami testów

**Implementation Note**: **Zatrzymaj się.** Jeśli którykolwiek diff nie jest pusty, wróć do fazy 3 —
to jest jedyny moment, w którym różnicę w metadanych da się poprawić bez konsekwencji w rejestrze.

---

## Phase 5: CI na Gradle'a

### Overview

Oba workflowy przechodzą na Gradle'a i muszą być zielone, zanim cokolwiek pójdzie do rejestru.
Przy wydaniu 7.0 zabrakło dokładnie tego kroku — przebieg z tagu `v7.0` do dziś jest czerwony.

### Changes Required:

#### 1. Build workflow

**File**: `.github/workflows/build.yml` (gałąź `jdk17`)

**Intent**: Budować i testować Gradle'em na każdy push i pull request.

**Contract**: JDK 17, wrapper zamiast lokalnego Gradle'a, walidacja wrappera, cache Gradle'a
zamiast Mavena. Krok uruchamia pełny `build` z testami — nie `assemble`.

#### 2. Publish workflow

**File**: `.github/workflows/publish.yml` (gałąź `jdk17`)

**Intent**: Publikować z CI, z poświadczeniami z `GITHUB_TOKEN`, zamiast z czyjejś maszyny.

**Contract**: Nadal wyłącznie `workflow_dispatch` (rejestr nie pozwala nadpisać wersji).
`permissions: packages: write`. Poświadczenia do repozytorium Packages przekazywane Gradle'owi
przez zmienne środowiskowe — `setup-java` generujący `settings.xml` przestaje być użyteczny.

#### 3. Przećwiczenie uwierzytelnienia

**Intent**: Sprawdzić ścieżkę publikacji Gradle → GitHub Packages, zanim użyjemy numeru, który ma
zostać na zawsze. Ta ścieżka nigdy w tym repo nie przeszła przez CI.

**Contract**: Publikacja na współrzędnej kontrolnej (wersja `7.1-ci-check`), nigdy na `7.1`.
⚠️ Wersji kontrolnej też nie da się usunąć bez kasowania pakietu — zostaje w rejestrze jako
świadomy koszt i ma być odnotowana w `change.md`.

### Success Criteria:

#### Automated Verification:

- Przebieg `build.yml` na gałęzi `jdk17` kończy się zielono, z 139 testami w logu
- Przebieg `publish.yml` z wersją `7.1-ci-check` kończy się zielono
- `mvn dependency:get -Dartifact=pl.homeportal:homeportal-commons-java:7.1-ci-check` do **czystego**
  `maven.repo.local` ściąga artefakt
- W rejestrze nie ma niczego pod wersją `7.1`

#### Manual Verification:

- Historia Actions dla gałęzi `jdk17` nie zawiera czerwonych przebiegów poza tymi sprzed migracji

**Implementation Note**: Zatrzymaj się przed fazą 6. Zielone CI jest warunkiem wejścia w krok
nieodwracalny — to jest ta pozycja, której zabrakło przy 7.0.

---

## Phase 6: Wydanie 7.1

### Overview

Publikacja z CI, tag, ślad w tickecie i usunięcie Mavena z gałęzi. Faza nieodwracalna.

### Changes Required:

#### 1. Wydanie

**Intent**: Wypuścić 7.1 zbudowane Gradle'em, tą samą ścieżką, która przeszła w fazie 5.

**Contract**: `publish.yml` uruchomiony ręcznie na commicie, z którego ma pójść wydanie. Sześć
modułów + `-sources.jar`. ⚠️ Rejestr przyjmuje `7.1` **raz** — poprawka po wydaniu to `7.2`,
nigdy ponowny deploy.

#### 2. Tag

**Intent**: Gałąź `jdk17` nie wchodzi do `mastera`, więc bez taga nie da się później odtworzyć,
z czego powstało 7.1.

**Contract**: `git tag -a v7.1` na **dokładnie tym** commicie, z którego poszedł deploy, potem push
gałęzi i taga.

#### 3. Usunięcie Mavena z gałęzi

**File**: `pom.xml` + sześć pomów modułowych (tylko gałąź `jdk17`)

**Intent**: Zostawić jeden system budowania. Pomy żyły przez pięć faz jako punkt odniesienia dla
bramek — po zielonym wydaniu nie mają już czego dowodzić.

**Contract**: Siedem plików usuniętych wyłącznie na `jdk17`. `master` nietknięty — dalej Maven,
dalej 6.0. ⚠️ Od tego commita skille `mb`, `md` i `mbd` przestają działać w tym repo; odnotować
w `change.md` i w `commons.md`.

#### 4. Zapis wydania

**File**: `context/changes/commons-gradle-migration/change.md`, `commons.md`

**Intent**: Utrwalić, co zostało wydane i czym się to teraz buduje.

**Contract**: Sekcja „Wydanie" z sha commitu, datą, listą sześciu artefaktów 7.1, adresem
repozytorium i zdaniem, że `master` został na 6.0/Mavenie. W `commons.md` komendy `./gradlew`
zamiast `mvn` oraz informacja o wersji kontrolnej `7.1-ci-check` w rejestrze.

### Success Criteria:

#### Automated Verification:

- `mvn dependency:get -Dartifact=pl.homeportal:homeportal-commons-<moduł>:7.1` do czystego
  `maven.repo.local` ściąga wszystkie sześć modułów
- `javap -verbose` na klasie z pobranego jara pokazuje `major version: 61`
- `git ls-remote --tags origin` zawiera `v7.1`
- `git ls-remote --heads origin jdk17` zwraca commit wydania
- `git show master:pom.xml | grep -m1 "<version>"` nadal `6.0`
- Na gałęzi `jdk17` nie ma żadnego `pom.xml`; na `master` jest ich siedem

#### Manual Verification:

- Wyraźne „tak" usera na wynik fazy 5 **przed** uruchomieniem publikacji
- Sześć artefaktów potwierdzonych pobraniem (nie widokiem w UI)
- `change.md` i `commons.md` opisują stan po migracji, w tym utratę działania skilli mavenowych

**Implementation Note**: **Twarda bramka.** Ta faza jest nieodwracalna i wchodzi wyłącznie po
Twoim wyraźnym „tak". Podbicie wersji commons u konsumentów **nie należy** do tego ticketu — 7.1
leży w rejestrze i nikt jej nie zobaczy, dopóki sam nie wpisze jej w swoim pomie. To jest zarazem
jedyny dostępny rollback.

---

## Testing Strategy

### Unit Tests:

- Bez zmian w treści testów. 27 klas, 139 metod, JUnit 4 + Hamcrest.
- Bramką jest liczba i rozkład per moduł, nie kolor builda.
- Zakaz wyciszania sprawdzany mechanicznie w każdej fazie dotykającej builda.

### Integration Tests:

- `FullTextRepositoryIntegrationTest` w `-data` wymaga H2 i deskryptora
  `src/test/resources/META-INF/persistence.xml` — zasób musi trafić na test classpath Gradle'a.
- Testy konsumentów w fazie 4 są integracją właściwą: hac podnosi kontekst Spring Boota, czego
  kompilacja commons nie dowodzi.

### Manual Testing Steps:

1. Po fazie 2 porównać wyjście `./gradlew build` i `mvn clean test` obok siebie — liczby testów
   i listę modułów.
2. Po fazie 3 przeczytać pom `-mail` i `-data` ręcznie, szukając scope'ów.
3. Po fazie 4 przejrzeć log builda haca pod kątem błędów podnoszenia kontekstu.
4. Po fazie 6 pobrać artefakt na czysto i sprawdzić bajtkod.

## Performance Considerations

Czas builda nie jest kryterium tej zmiany i nie należy go używać jako uzasadnienia — build portalu
(9 modułów, 691 testów) schodzi dziś w ~1 minutę, a commons jest wielokrotnie mniejszy. Jedyny
istotny koszt czasowy to faza 4: trzy pełne buildy aplikacji, kilkanaście minut na przebieg.

## Migration Notes

- Konsumenci **nie migrują w tym tickecie**. Każdy podbije `homeportal.commons.version` do 7.1
  wtedy, kiedy zechce; do tego czasu 7.0 leży w rejestrze nietknięte i to ono jest w użyciu.
- `hop` zostaje na 6.0 z `mastera` — linia 6.x żyje, dopóki hop nie stanie na JDK 17.
- Rollback nie istnieje technicznie. Jedyną formą wycofania jest niepodbijanie się do 7.1;
  poprawka po wydaniu to 7.2.

## References

- Frame brief: `context/changes/commons-gradle-migration/frame.md`
- Konwencja weryfikacji i wydania: `context/archive/2026-08-01-commons-jdk17-migration/plan.md:271-322`
- Zakaz wyciszania testów: `context/archive/2026-08-01-commons-jdk17-migration/plan.md:197-206`
- Incydent tożsamości artefaktu: `homeportal.portal/homeportal-portal-application/context/archive/2026-08-24-commons-6-0-artifact-missing-german/change.md`
- Ostrzeżenia o zależnościach: `pom.xml:126-141` (javax.mail), `homeportal-commons-java/pom.xml:93-99` (javax.el)
- Konsumenci: `homeportal.portal/pom.xml:27`, `homeportal.hac/pom.xml:31,58-65`, `homeportal.importer/pom.xml:35`, `homeportal.hop/pom.xml:24`

## Progress

> Convention: `- [ ]` pending, `- [x]` done. Append ` — <commit sha>` when a step lands. Do not rename step titles. See `references/progress-format.md`.

### Phase 1: Baseline

#### Automated

- [ ] 1.1 Trzy pliki `deps-*.txt` istnieją i zawierają `homeportal-commons-java:jar:7.0`
- [ ] 1.2 `jar-paths.txt` obejmuje sześć modułów, `-mail` ma 11 wpisów `mail/*.vm`
- [ ] 1.3 `mvn -B clean test` na `jdk17` daje BUILD SUCCESS i 139 testów
- [ ] 1.4 `grep -ac '@Test'` daje rozkład 59 / 15 / 59 / 4 / 2 / 0
- [ ] 1.5 `git status --short` pokazuje wyłącznie pliki z folderu ticketu

#### Manual

- [ ] 1.6 Stan trzech repozytoriów konsumenckich w chwili pomiaru odnotowany w `change.md`
- [ ] 1.7 Baseline zacommitowany przed pierwszą zmianą w buildzie

### Phase 2: Build Gradle bez publikacji

#### Automated

- [ ] 2.1 `./gradlew build` przechodzi na JDK 17
- [ ] 2.2 139 testów w rozkładzie 59 / 15 / 59 / 4 / 2 / 0
- [ ] 2.3 Zero wyciszeń wobec baseline'u
- [ ] 2.4 `javap -verbose` pokazuje `major version: 61`
- [ ] 2.5 Zbiór ścieżek w sześciu jarach zgodny z `baseline/jar-paths.txt`
- [ ] 2.6 Liczba plików `.class`: 32 / 17 / 23 / 8 / 1 / 1
- [ ] 2.7 `mvn -B clean test` nadal zielony ze 139 testami

#### Manual

- [ ] 2.8 Porównanie obu buildów odnotowane w `change.md`

### Phase 3: Metadane i publikacja do izolowanego stagingu

#### Automated

- [ ] 3.1 Staging ma jar + sources + pom dla sześciu modułów w wersji 7.1
- [ ] 3.2 Zero plików `.module`
- [ ] 3.3 Scope'y w sześciu pomach zgodne z baseline'em
- [ ] 3.4 Pięć wpisów `provided` obecnych we właściwych modułach
- [ ] 3.5 `<dependencyManagement>` niesie javassist 3.29.2-GA, commons-io 2.6, commons-compress 1.0
- [ ] 3.6 Rozwiązane wersje zgodne z 7.0
- [ ] 3.7 `~/.m2` nie zawiera niczego w wersji 7.1

#### Manual

- [ ] 3.8 Pomy `-mail` i `-data` przejrzane ręcznie pod kątem scope'ów

### Phase 4: Bramka u trzech konsumentów

#### Automated

- [ ] 4.1 `mvn -B clean install` zielony w portal, hac i importer przeciw stagingowi
- [ ] 4.2 Diff drzew zależności pusty dla wszystkich trzech (po podmianie 7.0→7.1)
- [ ] 4.3 Portal kompiluje klasy z `commons.data.model` (tranzytywny `-domain`)
- [ ] 4.4 javassist u konsumenta to 3.29.2-GA
- [ ] 4.5 `velocity` nie wchodzi na runtime classpath konsumenta
- [ ] 4.6 Liczba testów u konsumentów nie spadła
- [ ] 4.7 `git status --short` czysty w trzech repozytoriach po przywróceniu pomów

#### Manual

- [ ] 4.8 Kontekst Spring Boota w hacu podnosi się bez regresji
- [ ] 4.9 Wyniki trzech buildów spisane w `change.md`

### Phase 5: CI na Gradle'a

#### Automated

- [ ] 5.1 `build.yml` zielony na `jdk17`, 139 testów w logu
- [ ] 5.2 `publish.yml` zielony dla wersji `7.1-ci-check`
- [ ] 5.3 `dependency:get` ściąga wersję kontrolną do czystego repozytorium
- [ ] 5.4 W rejestrze nie ma niczego pod `7.1`

#### Manual

- [ ] 5.5 Historia Actions bez czerwonych przebiegów po migracji

### Phase 6: Wydanie 7.1

#### Automated

- [ ] 6.1 Sześć modułów 7.1 do pobrania przez `dependency:get`
- [ ] 6.2 `javap` na pobranym jarze pokazuje `major version: 61`
- [ ] 6.3 Tag `v7.1` na origin
- [ ] 6.4 Gałąź `jdk17` wypchnięta
- [ ] 6.5 `master` nadal na 6.0
- [ ] 6.6 Brak pomów na `jdk17`, siedem pomów na `master`

#### Manual

- [ ] 6.7 Wyraźne „tak" usera przed publikacją
- [ ] 6.8 Sześć artefaktów potwierdzonych pobraniem, nie widokiem w UI
- [ ] 6.9 `change.md` i `commons.md` opisują stan po migracji, w tym utratę skilli mavenowych
