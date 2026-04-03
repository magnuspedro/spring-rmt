# Spring RMT

[![CI](https://github.com/magnuspedro/spring-rmt/actions/workflows/ci.yml/badge.svg)](https://github.com/magnuspedro/spring-rmt/actions/workflows/ci.yml)

Spring RMT detects design-pattern refactoring opportunities in Java projects, applies the refactorings, and measures the impact with CK metrics.

The project is organized as a Maven monorepo with independent Spring Boot services connected through Redis queues and S3-backed file storage.

## What It Supports

- **Strategy** and **Factory Method** detection based on Wei et al. (2014)
- **Template Method** detection based on Zafeiris et al. (2016)
- **Cinneide refactoring tool methods** for composed mini-transformations
- Quality comparison between original and refactored code

### Cinneide Tool Methods

Implemented in `detection-and-refactoring` as AST transformations:

- Mini-transformations: `ABSTRACTION`, `ENCAPSULATE_CONSTRUCTION`, `ABSTRACT_ACCESS`, `PARTIAL_ABSTRACTION`, `DELEGATION`, `WRAPPER`
- Composed pattern methods: `applyFactoryMethod`, `applySingleton`, `applyAbstractFactory`, `applyStrategy`, `applyBridge`

## Modules

- `project-sync-bff`: web UI and REST API
- `detection-and-refactoring`: candidate detection and AST-based refactoring
- `metrics-calculator`: CK metrics and quality attribute calculation
- `config-starter`: shared models and infrastructure configuration

## Flow

1. A ZIP file is uploaded through `project-sync-bff`
2. The project is stored and queued for analysis
3. `detection-and-refactoring` detects candidates and generates refactored variants
4. `metrics-calculator` compares original and refactored code using CK metrics
5. The user can inspect the results and download the selected refactoring

## Tech Stack

- Java 25
- Spring Boot 4.0.4
- Maven
- Redis + Rqueue
- AWS S3 via Spring Cloud AWS
- JavaParser
- CK
- Testcontainers + LocalStack

## Run Locally

### Prerequisites

- Java 25
- Maven
- Docker
- `tflocal`

### Full environment

```bash
./rmt.sh
```

UI: `http://localhost:8080`

### Development mode

Start infrastructure:

```bash
./rmt.sh dev
```

Run services:

```bash
mvn spring-boot:run -pl project-sync-bff
mvn spring-boot:run -pl detection-and-refactoring
mvn spring-boot:run -pl metrics-calculator
```

`-pl` means `--projects`, so Maven runs the command only for the specified module.

### First-time Terraform init

```bash
tflocal -chdir=infra init
```

## Build and Test

Build all modules:

```bash
mvn clean install
```

Run all tests:

```bash
mvn test
```

Run tests for one module:

```bash
mvn test -pl project-sync-bff
mvn test -pl detection-and-refactoring
mvn test -pl metrics-calculator
```

Integration tests are included in the normal Maven test run and require Docker because the project uses Testcontainers with Redis and LocalStack.

If you only want to validate one service, use `-pl` to limit the test run to that module.

## Usage

### Web UI

Open `http://localhost:8080`, upload a ZIP with Java source code, and wait for the analysis to finish.

### REST API

```text
POST /rmt/api/v1/upload
GET  /rmt/api/v1/project/{id}
POST /rmt/api/v1/project/{id}/download
```

The API returns a project identifier that can be used to poll the analysis status and download the generated output.

## References

- Liu Wei, Hu Zhi-gang, Liu Hong-tao, and Yang Liu. (2014). *Automated pattern-directed refactoring for complex conditional statements*
- Vassilis E. Zafeiris, Sotiris H. Poulias, N.A. Diamantidis, and E.A. Giakoumakis. (2017). *Automated refactoring of super-class method invocations to the Template Method design pattern*
- Mel Ó Cinnéide. (2000). *Automated Application of Design Patterns: A Refactoring Approach*. PhD thesis, Trinity College Dublin, University of Dublin.
