# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

`homeportal-commons` is a multi-module Maven library (`pl.homeportal:homeportal-commons:5.0`, packaging `pom`) of shared utilities for the Homeportal real-estate platform. It is a **library**, not a runnable application — there is no `main`. Code is built, installed to the local repo, and consumed by downstream Homeportal projects. Java 8 (`source`/`target` 1.8).

## Build & Test

```bash
mvn clean install          # build all modules + run tests (also what the ./build script does)
mvn -pl homeportal-commons-data -am clean install   # build one module and its deps
mvn -pl homeportal-commons-java test -Dtest=StringUtilsTest          # run a single test class
mvn -pl homeportal-commons-java test -Dtest=StringUtilsTest#someCase # run a single test method
mvn deploy -Dmaven.test.skip=true                   # deploy artifacts (tests skipped)
```

Convenience skills exist for these: `mb` (build), `md` (deploy), `mbd` (build then deploy). Tests use JUnit 4 + Hamcrest.

## Module layout & dependency order

Only these five modules are part of this aggregator build (declared in the root `pom.xml`); build/edit them in dependency order:

1. **homeportal-commons-java** — foundation, no internal deps. Utilities under `pl.homeportal.commons.*`: text, datetime, file, image, zip, json, security, validation, reflection, i18n, exceptions, scheduler, MVC helpers, AOP aspects.
2. **homeportal-commons-data** — depends on `-java`. JPA/Hibernate Search persistence + the real-estate domain model.
3. **homeportal-commons-logging** — depends on `-java` and `-data`. Standardized entity-logging helpers.
4. **homeportal-commons-mail** — depends on `-java` and `-logging`. Velocity-templated email.
5. **homeportal-commons-test** — Spring MVC test helpers.

> The `homeportal-commons-geo-api` and `homeportal-commons-location-api` directories are **not** in the root `<modules>` and have a different parent (`pl.homeportal-platform`). They are not built by `mvn install` here — ignore them unless explicitly working on them.

## Conventions

- **Dependency versions are centralized.** All versions live in the root `pom.xml` `<dependencyManagement>`. Module POMs declare dependencies *without* `<version>` — when adding a dependency, add/reference its version in the root POM.
- **Lombok** (`@Getter`/`@Setter`/`@NoArgsConstructor`/etc.) is used throughout for entities and DTOs.
- All production code lives under the `pl.homeportal.commons` package root.

## Architecture notes

**AOP via annotations (in `-java`).** Cross-cutting behavior is driven by custom annotations paired with a Spring `@Component @Aspect`. To enable in a consuming app, the aspect class must be a registered Spring bean and AspectJ proxying enabled:
- `@ExecutionTime` → `ExecutionTimeAspect` logs method timing.
- `@ModelAttributeCondition` → `ModelAttributeConditionAspect` conditionally short-circuits Spring MVC `@ModelAttribute` methods based on the current request URI.

**Persistence (in `-data`).**
- `AbstractEntity<IDENTITY extends Number>` is the JPA `@MappedSuperclass` base for all entities (auto-generated `@Id`, plus `isPersisted`/`isTransient` helpers).
- `FullTextRepository<T>` / `FullTextRepositoryImpl<T extends AbstractEntity>` wrap **Hibernate Search + Lucene** full-text indexing and search. `SearchQuery` + `SearchQueryBuilder` + `QueryParameter` build queries; the `search/bridge` package holds Hibernate Search `FieldBridge`s (`FeatureBridge`, `NumericBridge`, `DateBridge`, `PropertyTypeBridge`) that map domain values into the Lucene index.
- The `pageable` package (`Page`, `PageItems`, `PageItem`) is a custom pagination abstraction layered over Spring Data's `Pageable`.
- The real-estate domain (`model/`) centers on `Product`, `Market`, and the `Feature` system (`Feature`, `FeatureType`, `FeatureConverter`), with property-type marker interfaces in `model/interfaces` (`IHouse`, `IApartment`, `ILand`, `IOffice`, `IHall`, `IRent`, `ISale`, ...).

**Logging (in `-logging`).** `LoggingSupport` is a static helper exposing standardized, format-string message templates (`INFORMATION_SAVE`, `ERROR_DELETE`, ...) for consistent entity-CRUD logging over SLF4J. Prefer these helpers/templates over ad-hoc log strings when logging entity operations.

**Mail (in `-mail`).** `Notifier`/`NotifierAdapter` send `VelocityEmail`s rendered from `EmailTemplate`s using Apache Velocity + commons-email; `BaseDTO` is the template model base. `SessionMockProvider` supports testing without a real mail session.

**Exceptions (in `-java`).** Use the `Homeportal*Exception` hierarchy (`HomeportalServiceException`, `HomeportalValidationException`, `HomeportalSecurityException`) rather than raw runtime exceptions.
