---
change_id: commons-spring-upgrade
title: Upgrade Springa w commons — Framework 5.2.9 i Spring Data Moore, bez Spring Boota
status: new
created: 2026-08-29
updated: 2026-08-29
archived_at: null
---

## Notes

upgrade spring boota

Zgłoszone przez usera 2026-08-29 dla całego homixa. Osobne tickety powstały w `hac`
(`hac-spring-boot-upgrade`) i tutaj; w `hop`, `portal` i `importer` temat świadomie **został
w ticketach `*-jdk17-migration`**, bo tam Spring i JDK to jedna decyzja.

## ⚠️ Nazwa jest inna niż zamówienie — celowo

W commons **nie ma Spring Boota**. Są dwie rzeczy niższego poziomu, zmierzone 2026-08-29:

| | |
|---|---|
| `spring.framework.bom.version` | **5.2.9.RELEASE** |
| `spring.data.releasetrain.version` | **Moore-SR10** |

To wersje odpowiadające linii Spring Boot 2.2.x — czyli tej, na której stoją `hop`, `portal`
i `importer`. Ticket o „upgrade Spring Boota" nie miałby tu czego podnosić; podnosi się BOM
Frameworka i release train Spring Data.

## Sedno: commons nie decyduje o swojej wersji Springa sam

Commons jest zależnością wszystkich pięciu pozostałych repozytoriów. Podniesienie tu Springa
**wymusza zgodność u konsumentów** — a ci są dziś w dwóch różnych światach:

- `hop`, `portal`, `importer` — Spring Boot **2.2.10.RELEASE**, JDK 8, `javax.*`
- `hac` — Spring Boot **3.3.5**, JDK 17, `jakarta.*`

Dlatego ten ticket **nie jest samodzielny**: jego zakres wyznaczają `hop-jdk17-migration`,
`importer-jdk17-migration` i `hp-jdk17-migration`. Podniesienie commons przed nimi zablokuje
im build; po nich — jest formalnością.

## Znalezione przy zakładaniu: uśpiona kolizja javax/jakarta

`homeportal-commons-java` importuje `javax.servlet.http.HttpServletRequest`
(`ControllerUtils`, `LanguageResolver`) oraz `javax.validation.*` (`ObjectValidator`,
`DateTimeUtils`). W Jakarta EE oba pakiety zmieniają nazwę na `jakarta.*`.

`hac` — stojący na Spring Boot 3.3.5, czyli w świecie `jakarta.*` — **deklaruje zależność od
`homeportal-commons-java`**. Sprawdzone: w kodzie haca **nie ma ani jednego importu
`pl.homeportal.commons`**, a jedyne wystąpienie `ControllerUtils` to javadoc mówiący, że hac
świadomie z niego nie korzysta. Czyli kolizja jest **uśpiona, nie żywa**: jar leży na ścieżce
klas, ale jego klasy dotykające `javax.*` nie są ładowane.

⚠️ To znaczy, że **pierwsze użycie w hacu czegokolwiek z `commons-java` wywali się w runtime**,
a nie przy kompilacji. `javax.xml.xpath` (3 wystąpienia) jest bezpieczny — zostaje w `javax.*`
także w Jakarcie, bo to część JDK.

## Dlaczego nie od razu plan

Zgłoszenie podaje kierunek, nie problem, a zakres tego ticketu zależy od trzech innych.
Stąd `/10x-frame` przed `/10x-plan`.
