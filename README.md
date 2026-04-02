# Spring RMT — Refactoring and Metrics Tool

Spring RMT is an automated tool for detecting design pattern refactoring opportunities in Java projects, applying those refactorings, and measuring the resulting code quality improvements.

## What It Does

Given a Java project (uploaded as a ZIP file), the tool:

1. Parses the source code and identifies classes that are candidates for classic Gang-of-Four design pattern refactorings
2. Automatically transforms the code to apply each refactoring
3. Computes object-oriented quality metrics on both the original and refactored code
4. Presents the candidates with quality scores so the user can evaluate and download any refactored variant

The detection algorithms are based on published academic research:
- **Wei et al. 2014** — detects candidates for the **Strategy** and **Factory Method** patterns
- **Zafeiris et al. 2016** — detects candidates for the **Template Method** pattern

## Architecture

The system is composed of four Spring Boot services that communicate via Redis queues and share files through AWS S3.

```
User
 │
 ▼
project-sync-bff          (port 8080)  — UI and REST API entry point
 │  stores ZIP in S3, enqueues project ID
 ▼
detection-and-refactoring (port 8081)  — AST analysis and code transformation
 │  downloads ZIP, detects candidates, applies refactorings, stores results in S3
 ▼
metrics-calculator        (port 8083)  — Code quality measurement
    downloads original + refactored ZIPs, runs CK metrics, stores scores in Redis
```

### Services

#### `project-sync-bff`
The entry point for users. Accepts a ZIP upload via a Thymeleaf/HTMX web UI or a REST API. Computes a SHA-256 hash of the file as a project ID (idempotency), stores the ZIP in S3, persists project state in Redis, and sends the project ID to the `detect-pattern` queue. Also exposes endpoints to poll for results and to download a merged refactored ZIP.

#### `detection-and-refactoring`
Consumes messages from the `detect-pattern` queue. Downloads and extracts the project ZIP from S3, parses every `.java` file into an AST using JavaParser, then runs two detection pipelines:
- `DetectionMethodsManagerWei` — Strategy and Factory Method candidates (Wei et al. 2014)
- `DetectionMethodsManagerZaiferis` — Template Method candidates (Zafeiris et al. 2016)

For each candidate, it clones the original file set, mutates the AST to apply the refactoring, and uploads the result as a new ZIP to S3. It then enqueues the project ID to the `measure-pattern` queue.

#### `metrics-calculator`
Consumes messages from the `measure-pattern` queue. For each refactoring candidate, it downloads the original and refactored ZIPs, writes them to temporary directories, and runs the CK metrics library to extract class-level metrics:
- **DIT** — Depth of Inheritance Tree
- **WMC** — Weighted Methods per Class (cyclomatic complexity)
- **LOC** — Lines of Code

From these, it derives three quality attribute scores by comparing the refactored metrics to the original:
- **Maintainability**
- **Reliability**
- **Reusability**

Results are stored back in Redis and the project status is set to `FINISHED`.

#### `config-starter`
A shared Spring Boot auto-configuration library depended on by all three services. Provides domain models (`Project`, `CandidateInformation`, `JavaFile`, `RefactorFiles`), S3 integration, Redis configuration, file compression/decompression, JavaParser setup, queue message definitions, and quality attribute interfaces.

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.0.4 |
| Build | Maven (monorepo) |
| Messaging | Redis via Rqueue (switchable to AWS SQS) |
| Storage | AWS S3 via Spring Cloud AWS |
| State | Redis via Spring Data Redis |
| AST Parsing | JavaParser 3.26.1 |
| Code Metrics | CK library 0.7.0 |
| UI | Thymeleaf + HTMX + Bootstrap 5 |
| Infrastructure | Docker Compose + LocalStack + Terraform |

## Running Locally

### Prerequisites
- Docker and Docker Compose
- Java 25
- Maven
- `tflocal` (Terraform wrapper for LocalStack): `pip install terraform-local`

### One-command setup

**macOS / Linux**
```bash
./rmt.sh
```

**Windows (PowerShell)**
```powershell
.\rmt.ps1
```

This builds all modules, builds Docker images, starts LocalStack and Redis, and provisions the AWS resources. The UI is available at `http://localhost:8080`.

### Available commands

| Command | Description |
|---|---|
| `./rmt.sh all` | Build everything and start the full Docker environment (default) |
| `./rmt.sh dev` | Build modules and start infrastructure only — run services with Maven |
| `./rmt.sh build` | Compile and install all Maven modules |
| `./rmt.sh images` | Build Docker images |
| `./rmt.sh infra` | Start LocalStack + Redis and provision Terraform resources |

### First-time Terraform initialisation

Before running `rmt.sh` for the first time, initialise Terraform once:

```bash
tflocal -chdir=infra init
```

### Running services without Docker

Use `./rmt.sh dev` to start the infrastructure, then run each service in a separate terminal:

```bash
mvn spring-boot:run -pl project-sync-bff
mvn spring-boot:run -pl detection-and-refactoring
mvn spring-boot:run -pl metrics-calculator
```

The web UI is available at `http://localhost:8080`.

## Usage

### Web UI
Navigate to `http://localhost:8080`, upload a ZIP of a Java project, and wait for the analysis to complete. The results page shows each refactoring candidate with its quality scores. Each candidate can be downloaded as a refactored ZIP.

### REST API
```
POST /api/refactor        — Upload a ZIP (multipart/form-data, field: "file")
GET  /api/refactor/{id}   — Poll for results by project ID
GET  /api/download/{id}/{candidateId} — Download a refactored ZIP
```

## Testing

### Unit Tests

Run all unit tests:

```bash
mvn test
```

Run tests for a specific module:

```bash
mvn test -pl detection-and-refactoring
```

Unit tests use Mockito and do not require Docker or any external services.

### Integration Tests

Integration tests exercise the **complete application** with all services and no mocking. Full Spring context loads with real LocalStack (S3) and Redis containers via Testcontainers.

**Prerequisites**: Docker must be running (`docker ps` should work).

```bash
# Run all integration tests
mvn test

# Run tests for specific service
mvn test -pl detection-and-refactoring
mvn test -pl project-sync-bff
mvn test -pl metrics-calculator
```

#### What Gets Tested

Integration tests are in `src/test/java/.../integration/` across three services:

**detection-and-refactoring** (Pattern Detection & Refactoring):
- **WeiStrategyIntegrationTest** — Strategy pattern: if-chain logic → abstract Strategy + concrete implementations
- **WeiFactoryMethodIntegrationTest** — Factory Method pattern: if-chain factory → abstract factory + subclasses
- **ZafeirisTemplateMethodIntegrationTest** — Template Method pattern: super-call hierarchy → template with hooks

**project-sync-bff** (Entry Point):
- **ProjectUploadIntegrationTest** — ZIP upload → S3 storage → Redis persistence → detection pipeline initiation

**metrics-calculator** (Quality Metrics):
- **MetricsCalculationIntegrationTest** — Original vs refactored code analysis → CK metrics calculation → quality scores storage

#### Test Execution Flow

Each integration test:
1. Creates a fixture ZIP from actual source code (from fixtures.Wei / fixtures.Zafeiris)
2. Uploads the ZIP to LocalStack S3 (`projects` bucket)
3. Saves a `BaseProject` to Redis with initial status
4. **Calls `ProcessRefactorCandidate.process()`** — full pipeline with no mocks:
   - `DetectionMethodsManagerWei` or `DetectionMethodsManagerZaiferis` detects patterns
   - `WeiEtAl2014Executor` or `ZafeirisEtAl2016Executor` performs refactoring
   - `ProjectUpdater` uploads refactored ZIPs to S3 and updates BaseProject in Redis
   - `SendProjectRedis` enqueues a message to the `measure-pattern` queue
5. Asserts:
   - `BaseProject.status` contains `REFACTORED`
   - `CandidateInformation` has the correct `DesignPattern`
   - Refactored ZIP in S3 contains transformed code
   - Message was enqueued to Redis queue (verified via RedisTemplate)

**Performance**: First test run ~30 seconds (container startup), subsequent runs ~5-10 seconds (container reuse).

## References

- Wei, L. et al. (2014). *Automated Detection and Refactoring of Design Patterns in Object-Oriented Systems*
- Zafeiris, V. E. et al. (2016). *Automated Refactoring of Super-Call Hierarchies to the Template Method Design Pattern*
- Aniche, M. (2015). *CK: A Java Code Metrics Extractor*
