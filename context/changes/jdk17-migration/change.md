---
change_id: jdk17-migration
title: Migracja homeportal.commons z Javy 8 na 17 — biblioteka idzie OSTATNIA, bo konsument na 8 nie odczyta bajtkodu 17; całość na osobnym branchu, bez merge'a
status: new
created: 2026-08-01
updated: 2026-08-21
archived_at: null
---

## Notes

Priorytet: 🟡 — **ocena własna.** Nic nie jest zepsute; biblioteka na 1.8 działa i jest czytana
przez wszystkich konsumentów, także tych na 17.

⚠️ **CAŁOŚĆ ROBIMY NA OSOBNYM BRANCHU I NIE MERGUJEMY** — decyzja usera 2026-08-21.
Branch: `feat/commons-jdk17`, odbity od `master`. Żadnego `merge` do `master`, żadnego wydania
artefaktu z tej gałęzi. Przy bibliotece to zastrzeżenie waży więcej niż przy aplikacji: wydany
artefakt trafia do czterech repozytoriów naraz i nie da się go cofnąć jednym `revert`.

Treść dopisana 2026-08-21 (ticket założony 2026-08-01 jako sam tytuł, bez ustaleń).

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
