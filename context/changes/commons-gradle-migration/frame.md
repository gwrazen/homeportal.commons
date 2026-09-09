# Frame Brief: Migracja commons z Mavena na Gradle'a

> Krok framingowy przed `/10x-plan`. Dokument zapisuje, co *faktycznie* jest tu
> problemem — oddzielnie od tego, co założono na wejściu.

## Reported Observation

„przejscie na gradle'a" — zgłoszone 2026-08-29 dla całego homixa, prowadzone jako osobny
ticket w każdym repo. W commons build to Maven: 6 modułów, publikacja do GitHub Packages,
brak wagona/FTP.

## Initial Framing (preserved)

- **Stated cause / approach**: commons to przypadek inny niż pozostałe pięć repo — nie ma
  wagona i deployu po FTP, ma `distributionManagement` na GitHub Packages, więc migracja
  musi rozwiązać publikowanie z uwierzytelnieniem (`change.md:26-31`). Build oceniony jako
  „najmniejszy w homixie i bez kroków nietypowych" (`change.md:42-44`).
- **Proposed direction**: zmigrować build commons na Gradle'a, „pierwszy albo ostatni,
  nigdy w środku" (`change.md:33-38`).
- **Pre-dispatch narrowing** (2026-09-09): objaw — „**nic nie boli** — chodzi o spójność
  homixa" + „nie rozdzieliłem tego, po prostu chcę Gradle'a"; zakres — „**tylko jdk17
  (7.0)**"; kolejność — „**pierwszy, commons jest poligonem**".
- **Doprecyzowanie po dowodach** (Krok 4): kryterium sukcesu — „**jedno narzędzie w całym
  homixie**"; oczekiwanie od poligonu — „**najmniejsze ryzyko, najmniejszy build**";
  linia 6.x — „**ma umrzeć**, hop idzie na 7.0".

## Dimension Map

1. **Motyw** — co Gradle ma naprawić; bez tego nie ma kryterium „gotowe".
2. **Reprezentatywność poligonu** — czy nauka z commons przenosi się na pięć aplikacji.
3. **Kontrakt artefaktu** — czy jar i pom z Gradle'a są nieodróżnialne dla konsumentów.
   ← tu leżało wyjściowe framing (zawężone do uwierzytelnienia)
4. **Zakres linii** — migracja gałęzi `jdk17` w repo, które prowadzi dwie żywe linie.
5. **Koszt portu** — co Maven tu realnie robi.

## Hypothesis Investigation

| Hipoteza | Dowody | Werdykt |
| --- | --- | --- |
| **1. Motyw nie istnieje** | Sześć bliźniaczych ticketów ma identyczną treść `## Notes`: jedną linię „przejscie na gradle'a". Brak celu udokumentowany w 7 miejscach: `hp-gradle-migration/change.md:21` („Cel nie jest jeszcze nazwany"), `hac.md:405` („Motywacja: DO USTALENIA (…) Migracja nie daje użytkownikowi niczego"), trzy `morning.md`. Jedyny pomiar buildu — portal, 9 modułów, 691 testów, **~1 min** — falsyfikuje hipotezę o czasie kompilacji. Zero kryteriów sukcesu w którymkolwiek tickecie. | **STRONG** |
| **2. Poligon uczy nie tego** | Przecięcie zbiorów problemów jest **puste**: commons jako jedyny ma `distributionManagement`/`maven-deploy` i jako jedyny nie ma wagona-FTP, Spring Boota ani `copy-rename` — te trzy ma pozostałe pięć. Ticket sam to pisze: „tu problem jest inny niż w pozostałych pięciu" (`change.md:22`). Bliźniaki zakładały wprost, że **commons jest poza zakresem**: `hp-gradle-migration/change.md` — „user wyłącza go z definicji »homixa«". Aktywność 3 mies.: commons 48 commitów vs portal 1376. | **STRONG** |
| **3. Kontrakt artefaktu, nie uwierzytelnienie** | Pomy modułów są **chude**: `<parent>pl.homeportal:homeportal-commons</parent>`, zależności **bez wersji** (`homeportal-commons-data/pom.xml:26-95`), wersje z dwóch BOM-ów importowanych w parencie (`pom.xml:73-90`). Konsument musi pobrać pom agregatora, żeby cokolwiek rozwiązać. `commons-domain` **nie jest deklarowany przez żadnego konsumenta** — portal (145 importów), hop (83), importer (18) kompilują się przeciw jego klasom tranzytywnie. Pięć zależności `provided` jest częścią kontraktu (`-mail:48,54`, `-test:31,37`). Gradle `maven-publish` generuje płaski pom bez parenta i BOM-ów, `compileOnly` nie publikuje wcale, `implementation` → scope `runtime` (zabija kompilację trzech konsumentów). Historia: **cztery incydenty publikacyjne w miesiąc**, wszystkie o tożsamość artefaktu — `d2ca1ed` (10 h ciszy na poczcie prod portalu), `2026-08-24-commons-6-0-artifact-missing-german` (zdalne 6.0 ≠ lokalne, 409 Conflict, skasowanie 5.0 przy okazji), `e538380` (7 pakietów usuniętych z rejestru), 7.0 wydane z lokalnej maszyny, nie z CI. | **STRONG** |
| **4. Zakres „tylko jdk17" mija się z celem** | `master` = 6.0/JDK 8 (konsument: `hop`, `homeportal.hop/pom.xml:24`), `jdk17` = 7.0/JDK 17 (portal, hac, importer). Merge zakazany bezterminowo: `archive/2026-08-01-commons-jdk17-migration/change.md:15`, `plan.md:79`, `plan-brief.md:29`; `plan.md:360` przypisuje masterowi stałą rolę linii hotfixowej 6.x. Workflowy CI **już się rozjechały** (JDK 8 vs 17 w obu plikach). Ticket gradle'owy (2026-08-29, 9 dni przed powstaniem gałęzi) opisuje stan wyjściowy jako „JDK 8, wersja 6.0" (`change.md:20`) — czyli linię, z której bierze już tylko jeden konsument. | **STRONG** (osłabione decyzją z Kroku 4: 6.x ma umrzeć → to warunek wstępny, nie trwały rozjazd) |
| **5. Koszt portu** | Build jest faktycznie ubogi: 7 pluginów tylko z pinem wersji, **jeden** aktywny (`maven-source-plugin`), zero profili, zero filtrowania, zero generowania kodu, jeden plik zasobów testowych. Ale cztery rzeczy wymagają decyzji: parent+BOM (p. 3), mapowanie `provided`, Lombok (w Gradle dwie deklaracje — dziś brak `annotationProcessorPaths`, nie ma z czego przepisać), oraz martwe moduły `geo-api`/`location-api` (obcy parent `pl.homeportal-platform`, `packaging ejb`, zacommitowany `target/` z jarem, ostatni commit 2020-06-29). Osobno: 27 klas / 139 testów **czystego JUnit 4**; Gradle 8+ bez `useJUnit()` wykrywa zero testów i kończy zielono. Gradle nie jest zainstalowany na stanowisku; `mvn -v` → 3.5.0 na JDK 1.8.0_222. | **WEAK** (port jest tani; drogie są decyzje wokół niego) |

## Narrowing Signals

- **„Nic nie boli — chodzi o spójność homixa"** — pierwszy w ogóle zapis motywu w tej
  sprawie. Rozstrzyga wymiar 1: nie ma czego naprawiać, jest cel organizacyjny.
- **„Jedno narzędzie w całym homixie"** jako kryterium — spłaca się **dopiero po
  ostatnim repo**. Ryzyko każdej migracji zapada od razu, wartość dopiero na końcu.
- **„Najmniejsze ryzyko — najmniejszy build"** — zderza się z dowodem: rozmiar builda
  i promień rażenia to w tym systemie **dwie różne osie**, a commons jest w minimum
  jednej i maksimum drugiej.
- **„6.x ma umrzeć, hop idzie na 7.0"** — zdejmuje zarzut trwałego rozjazdu i zamienia
  go na warunek wstępny: dopóki `hop` siedzi na 6.0, a merge jest zakazany, migracja
  dotyczy albo złej linii, albo obu naraz.
- **Bliźniacze tickety wykluczają commons z homixa** — przy kryterium „jedno narzędzie
  w całym homixie" migracja commons wnosi do tego celu, wedle własnej definicji usera
  z 2026-08-29, **zero**.

## Cross-System Convention

W tym systemie build nigdy niczego nie zepsuł. Zepsuła się natomiast **tożsamość
opublikowanego artefaktu** — cztery razy w ciągu miesiąca, raz z dziesięcioma godzinami
cichej awarii poczty na produkcji. GitHub Packages nie pozwala nadpisać wydanej wersji
(409), więc jedyną drogą wstecz jest kasowanie pakietu, a `~/.m2` bywał jedyną kopią
zapasową. Konwencja, którą ten system wypracował po tych incydentach, jest jedna i zapisana
w tickecie jdk17: **„»Zbudowało się« nie jest kryterium"** — weryfikuje się zawartość
artefaktu, nie kolor builda. Zmiana narzędzia budującego jest ingerencją dokładnie w ten
mechanizm, a nie w ten, który nie boli.

## Reframed Problem Statement

> **Prawdziwy problem do zaplanowania**: nie „jak przepisać build commons na Gradle'a",
> tylko „w jakiej kolejności i pod jakim warunkiem migruje się homix" — bo jedyne zapisane
> kryterium (jedno narzędzie wszędzie) spłaca się dopiero po ostatnim repo, a commons jest
> w tej stawce repozytorium o **najniższej wartości poznawczej i najwyższym promieniu
> rażenia**: uczy wyłącznie tego, czego żadna z pięciu aplikacji nigdy nie zapyta, a płaci
> się za to jedynym artefaktem, którego zepsucie jest jednoczesne dla czterech aplikacji
> i nieodwracalne w rejestrze.

Wybór commons na poligon opierał się na przesłance „najmniejszy build = najmniejsze
ryzyko". Dowody pokazują, że w tym systemie to dwie **rozłączne** osie: build commons
jest rzeczywiście najmniejszy (jeden aktywny plugin, zero profili), ale jego produkt to
kontrakt czytany przez cztery aplikacje produkcyjne — z parentem, dwoma importami BOM,
pięcioma wpisami `provided` i tranzytywną widocznością `commons-domain`, którego nikt
nie deklaruje wprost. Gradle każdą z tych czterech rzeczy zmienia **domyślnie i po cichu**,
a build świeci przy tym na zielono. Do tego doszedłby piąty wariant tej samej awarii,
która w tym repo zdarzyła się już cztery razy w miesiąc.

Drugi wniosek dotyczy kolejności wewnątrz samego commons: przy decyzji „6.x ma umrzeć"
migracja builda ma warunek wstępny — `hop` na 7.0 i rozstrzygnięcie, czy `jdk17` zostaje
nowym masterem. Dziś migracja `jdk17` trafia w gałąź, która z założenia nigdy nie wraca
do mastera, a migracja mastera — w linię obsługującą jednego konsumenta.

## Confidence

**HIGH.** Cztery niezależne badania plus kontrola bez podanej hipotezy zbiegły się na tym
samym wymiarze (kontrakt artefaktu / promień rażenia). Każde twierdzenie ma zakotwiczenie
w plik:linia albo w historii gita. Motyw i kryterium sukcesu pochodzą wprost od usera
(2026-09-09), nie z domysłu.

## What Changes for /10x-plan

Plan **nie powinien** być planem portu builda commons. Do rozstrzygnięcia przed planem są
dwie rzeczy: (1) czy poligonem nie ma być `importer` — najmniejsza z aplikacji, mająca
komplet tego, co realnie boli (wagon-FTP, Spring Boot, `copy-rename`), której awaria
zatrzymuje jeden nocny job zamiast czterech aplikacji; (2) czy commons w ogóle wchodzi
do zakresu, skoro bliźniacze tickety wykluczają go z definicji homixa. Jeśli mimo to
commons idzie pierwszy, plan musi mieć bramkę **równoważności artefaktu**, nie bramkę
„zbudowało się": pusty diff `mvn dependency:tree` u czterech konsumentów przed i po,
diff pomów i `unzip -l` jarów, liczba uruchomionych testów = 139, wydanie pod **nową**
wersją (6.2 / 7.1), nigdy w miejsce istniejącej.

## References

- `context/changes/commons-gradle-migration/change.md:20,22,26-31,33-38,42-44`
- `pom.xml:9,22-23,33,63-69,73-90,126-141,145-149,294-347`
- `homeportal-commons-{data,mail,test,java}/pom.xml` — scope `provided`, zależności bez wersji
- `.github/workflows/{build,publish}.yml` (obie gałęzie, rozjazd JDK 8/17)
- `context/archive/2026-08-01-commons-jdk17-migration/{change.md:15,197-227, plan.md:79,355-361, plan-brief.md:29,71-72}`
- `homeportal.portal/homeportal-portal-application/context/archive/2026-08-24-commons-6-0-artifact-missing-german/change.md`
- Commity: `d2ca1ed`, `4b9c424`, `2104748`, `e538380`, tag `v7.0`
- Bliźniacze tickety: `hp-gradle-migration`, `hac-gradle-migration`, `hop-gradle-migration`, `importer-gradle-migration` (wszystkie `new`, wszystkie bez planu)
- `homeportal.hac/hac.md:402-410` — „Motywacja: DO USTALENIA"

## Decyzja usera (2026-09-09, po przeczytaniu briefu)

**Commons idzie pierwszy mimo powyższego.** Zastrzeżenie o wartości poznawczej poligonu
zostało podniesione i świadomie odrzucone — to nie jest przeoczenie, tylko wybór.

Co z tego wynika dla planu, bezwarunkowo:

- Bramką jest **równoważność artefaktu**, nie zielony build: pusty diff `mvn dependency:tree`
  we wszystkich modułach portalu, hopa, haca i importera przed i po podmianie; diff pomów
  w `~/.m2`; `unzip -l` jarów (w tym `mail/*.vm` w `-mail` i `-sources.jar`).
- **139 uruchomionych testów** w 27 klasach — liczba, nie kolor. Czysty JUnit 4: bez
  `useJUnit()` Gradle 8+ wykrywa zero testów i kończy się sukcesem.
- Wydanie pod **nową wersją** (6.2 / 7.1), nigdy w miejsce istniejącej — rejestr zwraca 409,
  a jedyną drogą wstecz jest kasowanie pakietu (zdarzyło się już dwa razy).
- Rozstrzygnąć na wejściu: **którą gałąź** migrujemy przy decyzji „6.x ma umrzeć" — czy
  warunkiem wstępnym jest przeprowadzenie `hopa` na 7.0 i uczynienie `jdk17` masterem.
- Rozstrzygnąć: parent pom z dwoma importami BOM (osobny artefakt `java-platform` czy
  pinowanie wersji u siebie), mapowanie pięciu `provided`, Lombok jako `compileOnly` +
  `annotationProcessor`, Gradle Module Metadata (zostawić czy wyłączyć), oraz los martwych
  `geo-api` / `location-api`.
- Skille `/mb`, `/md`, `/mbd` są **globalne** i mavenowe — po migracji przestają działać
  w tym repo. Naprawa musi być warunkowa per-repo na czas stanu mieszanego.
