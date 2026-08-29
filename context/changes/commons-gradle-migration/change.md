---
change_id: commons-gradle-migration
title: Przejście commons z Mavena na Gradle'a — biblioteka publikowana do GitHub Packages, nie wdrażana
status: new
created: 2026-08-29
updated: 2026-08-29
archived_at: null
---

## Notes

przejscie na gradle'a

Zgłoszone przez usera 2026-08-29 dla całego homixa, prowadzone jako osobny ticket w każdym repo.
Bliźniaki: `hp-gradle-migration`, `hac-gradle-migration`, `hop-gradle-migration`,
`importer-gradle-migration`, `spy-gradle-migration`.

## Stan wyjściowy (zmierzony 2026-08-29)

6 modułów (`java`, `domain`, `data`, `mail`, `logging`, `test`), **JDK 8**, wersja **6.0**.

## ⚠️ Tu problem jest inny niż w pozostałych pięciu

Tamte repozytoria mają `wagon-maven-plugin` i wysyłają jara po FTP na serwer. **Commons nie ma
wagona w ogóle** — to biblioteka, nie aplikacja. Zamiast tego ma `distributionManagement`
wskazujące na **GitHub Packages** (`https://maven.pkg.github.com/gwrazen/homeportal.commons`).

Czyli migracja nie musi rozwiązywać problemu deployu po FTP, ale musi rozwiązać **publikowanie
z uwierzytelnieniem do GitHub Packages** — w Gradle to blok `publishing` plus poświadczenia,
których dziś Maven bierze z `settings.xml`.

## ⚠️ Commons jest zależnością wszystkich pięciu pozostałych repozytoriów

Każde z nich ciągnie `pl.homeportal:homeportal-commons-*` **przy stałej wersji**. Skutek: to repo
migruje się **pierwsze albo ostatnie, nigdy w środku** — a jeśli zmieni się sposób publikowania
artefaktu, pięć innych buildów przestanie go znajdować. Gradle konsumuje artefakty Mavena bez
problemu i odwrotnie, więc stan mieszany jest technicznie w porządku; ryzyko siedzi wyłącznie
w tym, **skąd i pod jaką współrzędną** artefakt jest pobierany.

## Dlaczego nie od razu plan

Zgłoszenie podaje rozwiązanie, nie problem. Zanim powstanie plan, trzeba odpowiedzieć, **co Gradle
ma naprawić** — tu tym bardziej, bo to najmniejszy build w homixie i bez kroków nietypowych.
Stąd `/10x-frame` przed `/10x-plan`.

## Uwaga o nazewnictwie

Prefiks `commons-` jest tu konwencją: tak nazywa się zarchiwizowany `commons-refactoring`
i tak nazywają się bliźniaki w pozostałych repach. Otwarty ticket migracyjny, który stał bez
prefiksu, został **przemianowany 2026-08-29 na `commons-jdk17-migration`**.
