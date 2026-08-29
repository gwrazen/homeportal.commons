---
change_id: commons-refactoring
title: Przeglad modulow commons, usuniecie bugow i refaktor full-text search
status: archived
created: 2026-08-01
updated: 2026-08-29
archived_at: 2026-08-29T07:16:02Z
---

## Notes

Ogolny przeglad modulow, usuniecie bugow, full text search refactoring.

Zakres wstepny:
- przeglad wszystkich piatki modulow aggregatora (`-java`, `-data`, `-logging`, `-mail`, `-test`)
- wylapanie i usuniecie bugow znalezionych po drodze
- caly refaktor wychodzi jako wersja **6.0** (`pl.homeportal:homeportal-commons:6.0`) — bump wykonany 2026-08-01 w root pom + 5 modulach; konsumenci (hac/hop/portal/importer) nadal wskazuja 5.0 i wymagaja osobnego przelaczenia
- praca idzie na branchu `commons-refactoring` (odbity od `master` @ 346bd66), nie na master
- refaktor full-text search w `-data` (`FullTextRepository` / `FullTextRepositoryImpl`, `SearchQuery` + `SearchQueryBuilder` + `QueryParameter`, `search/bridge`)
