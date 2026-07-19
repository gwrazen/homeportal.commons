# commons — baza wiedzy (single file)

**homeportal.commons** — współdzielona biblioteka Maven (`pl.homeportal:homeportal-commons:5.0`,
packaging `pom`, Java 8) narzędzi dla platformy Homeportal; **biblioteka, nie aplikacja** (brak `main`) —
budowana, instalowana do lokalnego repo i konsumowana przez pozostałe repa homixa. Uwaga: **buduje się
wyłącznie na JDK 8** (Lombok 1.16.14 pada na JDK 16+), a konsumenci budują na JDK 17 wobec gotowego jara.

## Skille AI (rejestr)

Skille AI homixa mają wspólne, wersjonowane źródło — sibling repo **`homeportal.ai.registry`**;
globalne `mb/mbd/md/itest` są tam symlinkowane do `~/.claude/skills`, a `install.js` je synchronizuje.
Pełny model scope/origin/wersji i lista repów homixa: `homeportal.hac/hac.md` §11.
