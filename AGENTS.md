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
- **Add JavaDoc to all public methods** — document purpose, parameters, return values, and exceptions.
- **Remove inline comments** — move explanations to JavaDoc.

### Modern Java Features (Java 25)
- **Pattern matching for instanceof** — `if (node instanceof TryStmt tryStmt)`
- **Pattern matching for switch** — `return switch (value) { case FOO -> ...; }`
- **Records** — immutable data carriers instead of inner classes
- **Stream.toList()** — not `.collect(Collectors.toList())`
- **Optional** — not null (Replace Primitive Obsession)

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

### AST Handler Architecture

The `AstHandler` God class (676 lines) has been split into focused handlers:

```
AstHandler (facade, @Deprecated)
├── AstMethodHandler     → method operations (getMethods, retrieveOverriddenMethod, methodsParamsMatch)
├── AstNodeHandler       → generic node operations (getClassOrInterfaceDeclaration, getIfStatements, nodeHasClazz)
├── AstVariableHandler   → variable operations (getVariableSimpleName, doesNodeUsesVar, getVariableDeclarations)
├── AstSuperCallHandler  → super call operations (getSuperCalls, hasDirectSuperCall)
├── AstStatementHandler  → statement operations (getExpressionStatement, nodeHasReturnStatement)
├── AstTypeHandler       → type operations (getParentType, doesCompilationUnitsMatch)
└── AstMethodHelper      → static utilities (isPositionOutOfBounds, doesMethodCallsMatch)
```

When adding new AST operations, add them to the appropriate handler class rather than `AstHandler`.

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

### Commit Pattern

**Follow these rules for every change:**

1. **Make atomic, focused commits** — one logical change per commit
2. **Commit frequently as work progresses** — don't batch unrelated changes
3. **Use Conventional Commits format**:
   ```
   <type>(<scope>): <subject>

   <body explaining what and why>
   ```

   Types: `feat:`, `fix:`, `refactor:`, `docs:`, `test:`, `chore:`, `perf:`, `style:`

4. **Reference issue/pr numbers** in body if applicable

5. **No Co-Authored-By trailers** unless explicitly co-authored

### Example Commits

```bash
# Good
refactor(ast): extract specialized AST handlers from God class

Extract the 676-line AstHandler into focused handlers:
- AstMethodHandler: method operations
- AstVariableHandler: variable operations
- AstSuperCallHandler: super call operations

Applies Extract Class refactoring pattern (Martin Fowler).

docs(cinneide): add JavaDoc to CinneideEtAl2000

Add comprehensive class and method JavaDoc documenting:
- Supported design patterns (Factory Method, Singleton, etc.)
- Research paper reference (Cinneide et al. 2000)

# Bad
fix stuff
updated files
refactoring
```

### Branch Workflow

1. Features branch from `develop`: `git checkout -b feature/short-description`
3. Make atomic commits as you work
4. Push and create PR when complete
5. PR title should follow conventional commits

### PR Pattern

When creating a PR via `gh pr create`:

```bash
gh pr create --title "refactor(scope): brief description" --body "## Summary
- What changed
- Why it matters

## Changes
- Bullet points

## Test Plan
- [ ] Compiles
- [ ] Tests pass
- [ ] Manual verification"
```

## General Rules

- Don't over-explore the codebase with excessive grep/read calls. If you haven't converged on an approach after 3-4 searches, pause and share what you've found.
- When the user asks to fix tests, fix the tests — not the source code — unless explicitly asked.
- **Do what has been asked; nothing more, nothing less.**
- **NEVER create files unless absolutely necessary**
- **ALWAYS prefer editing existing files**
- **NEVER proactively create documentation files**
- **ALWAYS keep memory in the current working directory and `memories/` folder**
- **ALWAYS commit frequently with atomic changes**

## Self-Improvement Loop

The user may have shared a `PERSONAL.md` file with specific instructions. If so:

- Review `PERSONAL.md` at the start of every session
- After ANY correction from the user: update `PERSONAL.md` with the pattern
- Write rules that prevent the same mistake from happening again

## Martin Fowler Refactoring Patterns to Apply

| Pattern | When to Use | Example |
|---------|-------------|---------|
| Extract Method | Long method with complex logic | Break down 50+ line methods |
| Extract Class | God class with multiple responsibilities | AST handlers, executors |
| Decompose Conditional | Complex nested conditionals | Preconditions validation |
| Replace Primitive Obsession | Using null instead of Optional | Return Optional<T> not null |
| Replace Inner Class with Record | Mutable inner class | SuperReturnVar → record |
| Introduce Parameter Object | Long parameter lists | RefactorFiles builder |

## Clean Code Principles

- **Single Responsibility** — each class/method does one thing well
- **DRY** — don't repeat yourself (extract to RefactoringUtils)
- **Meaningful Names** — `retrieveOverriddenMethod` not `getMeth`
- **Small Methods** — aim for 10-15 lines max
- **Guard Clauses** — return early for negative cases
- **No Deep Nesting** — maximum 2-3 levels
