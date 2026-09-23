---
change_id: commons-hot-reload-properties
title: Właściwości zmieniane bez restartu — jeden mechanizm w commons dla całego homixa
status: new
created: 2026-09-23
updated: 2026-09-23
archived_at: null
---

## Notes

Zgłoszone przez usera **2026-09-23**: *„może zrobić ticket na properties po których zmianie nie
trzeba robić restartu? to by się przydało dla całego homixa"*.

Bezpośredni powód: tego dnia limit dobiegu wyceny zmieniono 45 → 50 w pliku produkcyjnym,
a **obowiązuje dalej 45**, bo JVM czyta wartość przy starcie. Plik mówi jedno, aplikacja robi drugie —
i to jest gorsze niż brak zmiany, bo następna osoba policzy przepustowość z pliku.

Priorytet: 🟡 — **moja ocena.** Nic nie jest zepsute; to skraca pętlę „zmień wartość → zobacz skutek"
i likwiduje rozjazd plik ↔ pamięć. Podnieść do 🟠, gdyby restart kiedyś kosztował więcej niż dziś
(dziś portal wstaje w ~12 s).

## Skala — zmierzone 2026-09-23

| aplikacja | kluczy w confie produkcyjnym | `@Value` w `ApplicationConfiguration` |
|---|---:|---:|
| portal | 162 | 161 |
| hac | 116 | 56 |
| importer | 27 | 25 |
| hop | 15 | 13 |

Razem **320 kluczy**, z których **każdy** wymaga dziś restartu aplikacji.

## Co dziś boli — przypadki z ostatniego miesiąca

| wartość | skutek dzisiejszego zachowania |
|---|---|
| `scheduler.offer.valuations.day.limit` (portal) | zmieniony 23.09, **nadal nieaktywny** |
| `geo.blocking.mode` (portal) | wyłącznik blokady krajów wymaga restartu — czyli przerwy w serwisie po to, żeby przestać blokować |
| `known-bots.txt` (portal) | wzorzec botów kompilowany raz przy starcie; edycja pliku nic nie daje do restartu |
| `css.version` (portal) | bump wersji arkusza to restart, a bez niego nginx trzyma stary plik 30 dni |
| `hac.label.*` (hac) | progi etykiety liczone przy odczycie, ale wartości wczytane przy starcie — zmiana progu = restart |
| `hac.valuation.cache-ttl-hours` (hac) | zmieniony 23.09 razem z restartem HAC-a |

Wspólny mianownik: to **pokrętła operatora**, nie parametry architektury. Zmienia się je, żeby
zareagować na sytuację — a reakcja kosztuje restart.

## Czego NIE robić

⚠️ **Nie robić tego dla wszystkich 320 kluczy.** Część z nich musi być czytana raz, bo od niej zależy
budowa kontekstu: połączenie do bazy, porty, `server.port`, ścieżki katalogów, wzorce mapowań Springa.
Gorące przeładowanie takiej wartości daje aplikację w stanie, którego nie da się odtworzyć z pliku.

⚠️ **Nie przepisywać `ApplicationConfiguration` na dynamiczne gettery.** 161 pól w samym portalu,
a każde czytane w kilku miejscach — to refaktor o zasięgu całego repo, z ryzykiem nieproporcjonalnym
do korzyści. Ma powstać **lista wartości przeładowywalnych**, a nie zmiana sposobu czytania wszystkich.

⚠️ **Nie wprowadzać Spring Cloud Config ani `@RefreshScope`.** To zależność i drugi mechanizm
konfiguracji dla czterech aplikacji, które czytają jeden plik z dysku.

## Kierunek do rozważenia (do rozstrzygnięcia w planie)

Najbliżej dzisiejszego warsztatu: **operacja JMX „przeładuj konfigurację"** w module `management`,
wspólna klasa w commons. Czyta plik jeszcze raz i podmienia wartości z **jawnej białej listy**
kluczy oznaczonych jako przeładowywalne; reszta jest ignorowana i wypisana w odpowiedzi.

Za tym kierunkiem przemawia to, że JMX już jest w każdej aplikacji homixa i jest jedynym kanałem,
którym dziś wykonuje się operacje na żywym systemie (`invalidateArticle`, `resetBox`, `warmCacheHtml`).

Do rozstrzygnięcia w planie:

1. **Które wartości wchodzą na białą listę.** Kandydaci widoczni od ręki: limity i wyłączniki
   schedulerów, `geo.blocking.mode`, progi etykiety w hacu, `css.version`, pauzy i limity paczek.
   ⚠️ `cron` jest osobnym przypadkiem — zmiana harmonogramu wymaga przepięcia `@Scheduled`, a to już
   nie jest podmiana wartości.
2. **Jak wartość dociera do miejsca użycia**, skoro `@Value` wstrzykuje raz. Najmniej inwazyjnie:
   pola oznaczone jako przeładowywalne czyta się przez getter, a getter pyta o aktualną wartość —
   ale tylko dla tych z listy.
3. **Co z widocznością.** Po przeładowaniu ma zostać ślad w logu (co, z czego na co, kto wywołał),
   inaczej powstaje drugi rozjazd: plik i pamięć zgodne, ale nikt nie wie, kiedy to się stało.
4. **Co przy błędnej wartości.** Dzisiejsza walidacja startowa (np. `floor ≤ below < above ≤ cap`
   w hacu) przy starcie nie wpuszcza aplikacji. Przeładowanie musi umieć **odrzucić** zmianę
   i zostawić poprzednią, zamiast wpuścić stan, w którym aplikacja by nie wstała.
5. **Czy to samo dotyczy plików obok confa** — `known-bots.txt`, `blocked-areas.txt`, `robots.txt`.
   Mają ten sam objaw (wczytane raz), ale inny mechanizm; być może ta sama operacja JMX.

## Odbiór

Zmiana wartości z białej listy na produkcji **działa bez restartu** i widać to w zachowaniu, nie tylko
w odpowiedzi JMX: np. podniesienie limitu dobiegu zwiększa liczbę ofert w kolejnym przebiegu
(`/scheduls`), a zmiana `geo.blocking.mode` zmienia kod odpowiedzi dla adresu z blokowanego kraju.

## Powiązane

- `homeportal.portal`: `context/changes/hp-valuation-label-from-hac/` — tam wypadł przypadek,
  który wywołał ten ticket (limit 45 → 50 czeka na restart).
- Pokrętła operatorskie i ich układ w pliku: reguła `.properties` w instrukcjach globalnych usera.
