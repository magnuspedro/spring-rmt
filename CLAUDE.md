# Spring RMT — Claude Code Guide

## Project Overview

Spring RMT is a Maven monorepo that automatically detects design-pattern refactoring opportunities in Java projects, applies the transformations, and measures the resulting code-quality change using CK metrics.

## Module Structure

| Module | Artifact ID | Purpose |
|---|---|---|
| `config-starter` | `config-starter` | Shared library: domain models, S3/Redis/SQS configuration, JavaParser setup, queue definitions |
| `project-sync-bff` | `projectsyncbff` | Entry point: Thymeleaf/HTMX UI and REST API; accepts ZIP uploads, drives the pipeline |
| `detection-and-refactoring` | `detectionandrefactoring` | Consumes `detect-pattern` queue; parses Java ASTs, detects candidates, applies refactorings |
| `metrics-calculator` | `metricscalculator` | Consumes `measure-pattern` queue; runs CK metrics on original and refactored code |

`config-starter` is a library, not a runnable service. The other three are Spring Boot applications.

## Build & Run Commands

```bash
# Build everything (from repo root)
mvn clean install

# Build a single module
mvn clean install -pl config-starter
mvn clean package -pl detection-and-refactoring

# Run a service locally (from repo root)
mvn spring-boot:run -pl project-sync-bff
mvn spring-boot:run -pl detection-and-refactoring
mvn spring-boot:run -pl metrics-calculator

# Run all tests
mvn test

# Run tests for one module only
mvn test -pl detection-and-refactoring
```

## One-command Setup

```bash
./rmt.sh          # build + docker images + full local environment (default)
./rmt.sh dev      # build + start infra only, then run services with mvn spring-boot:run
./rmt.sh build    # Maven build only
./rmt.sh infra    # LocalStack + Redis + Terraform only
```

Windows: `.\rmt.ps1 [command]` — same subcommands.

Before first run, initialise Terraform once: `tflocal -chdir=infra init`

## Tech Stack

- **Java 25** (source/target set to 21 in pom.xml)
- **Spring Boot 4.0.4** / Spring Framework 7
- **Spring Cloud 2025.1.1 (Oakwood)** / **Spring Cloud AWS 4.0.0**
- **Rqueue 4.0.0-SNAP-RELEASE** for Redis-backed queues
- **Lombok 1.18.38** — first version with Java 25 annotation-processor support
- **JavaParser 3.26.1** for AST manipulation
- **CK 0.7.0** (mauricioaniche) for code metrics

## Code Conventions

### General
- Lombok `@Builder` / `@SuperBuilder` / `@RequiredArgsConstructor` are used extensively — do not add manual constructors or boilerplate.
- Spring beans are wired by constructor (via `@RequiredArgsConstructor`), not field injection.
- `var` is used for local variables throughout; follow the same style.
- No additional comments or Javadoc on existing code unless logic is genuinely non-obvious.

### Testing
- Tests use **JUnit 5** + **Mockito** (subclass mock maker — see `mockito-extensions/org.mockito.plugins.MockMaker`).
- `@MockitoBean` (not `@MockBean`) for Spring slice tests.
- `@WebMvcTest` is from `org.springframework.boot.webmvc.test.autoconfigure` (Boot 4 package).
- `MockPart` is used instead of `MockMultipartFile.file()` (removed in Spring Framework 7).
- Fixtures for the Wei and Zafeiris tests live in `detection-and-refactoring/src/test/java/fixtures/`.

### Domain Models (config-starter)
- `BaseProject` — tracks pipeline status with a `LinkedHashSet<ProjectStatus>` (order matters).
- `RefactorFiles` — a mutable builder class (not a record) that accumulates files and candidates during a refactoring run.
- `JavaFile` — wraps raw source + parsed `CompilationUnit`; `getParsed()` returns `Object` cast to `CompilationUnit`.

## Detection Algorithm Structure

```
DetectionMethodsManagerWei  (Strategy + Factory Method — Wei et al. 2014)
  └─ WeiEtAl2014
       ├─ WeiEtAl2014StrategyVerifier   → WeiEtAl2014StrategyCandidate
       ├─ WeiEtAl2014FactoryVerifier    → WeiEtAl2014FactoryCandidate
       ├─ WeiEtAl2014StrategyExecutor
       └─ WeiEtAl2014FactoryExecutor

DetectionMethodsManagerZaiferis  (Template Method — Zafeiris et al. 2016)
  └─ ZafeirisEtAl2016
       ├─ ZafeirisEtAl2016Verifier
       │    ├─ SuperInvocationPreconditions
       │    ├─ ExtractMethodPreconditions
       │    └─ SiblingPreconditions
       └─ ZafeirisEtAl2016Executor
            └─ FragmentsSplitter
```

### Key algorithm rules (already enforced in code)
- Wei verifier: candidate methods must have ≥ 1 parameter and a non-void return type. The parameter used in the if-condition is the **discriminator** — identified at runtime, not assumed to be at index 0.
- Zafeiris verifier: excludes `toString`, `equals`, `hashCode`, `clone`, `finalize`, `compareTo` and any getter/setter. Requires exactly 1 non-nested `super()` call per method. Sibling classes are grouped by shared parent type.
- CK metrics are averaged across all classes (not summed) before comparison.

## Infrastructure (Local)

| Service | Port |
|---|---|
| project-sync-bff | 8080 |
| detection-and-refactoring | 8081 |
| metrics-calculator | 8083 |
| LocalStack (AWS emulation) | 4566 |
| Redis | 6379 |

Terraform (`infra/main.tf`) provisions the S3 buckets and SQS queues in LocalStack.

## Git Conventions

- Main branch: `main`. Working branch: `develop`. Features branch from `develop`.
- Commit messages follow Conventional Commits: `fix:`, `feat:`, `refactor:`, `chore:`, `test:`, `docs:`.
- Do not include `Co-Authored-By` trailers in commits.
