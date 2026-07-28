# Event Onboarding

A 4-step onboarding wizard with a **Kotlin + Spring Boot** modular-monolith
backend and a **Next.js + TypeScript** frontend. The repository also contains
alternative frontend implementations used for comparison and experimentation.

## The flow

| Step | What the user does | What the backend does |
|------|--------------------|-----------------------|
| 1. Email | Submits only an email | Creates an application, generates a 6-digit token and **prints it to the console** |
| 2. Token verify | Enters the token from the console | Verifies it |
| 3. Fulfillment | Enters name, email, phone, salary, years of experience | Stores the details |
| 4. Credit scoring | — | Computes a weighted score (0–100), generates an account password with the email as username, and **prints the credentials to the console**. **> 40 → welcome page**, otherwise **decline page** |

State lives in memory keyed by an `applicationId` (a `ConcurrentHashMap`), so it
is reset on restart. The verification token and generated password are **never**
returned over the API — they only appear in the server console.

## Architecture (Modular Monolith + Ports & Adapters)

The backend is one deployable Spring Boot application composed from Gradle modules.
Each onboarding step owns its use case, HTTP adapter, DTOs, and required outbound
ports; the framework-free services depend only on module APIs and the shared kernel.

```
module-shared-kernel/           application aggregate, workflow states, exceptions,
                                shared repository contract
module-onboarding-email/        step 1: create/query application and issue token
module-onboarding-token-verification/ step 2: verify token
module-onboarding-fulfillment/  step 3: capture applicant details
module-onboarding-scoring/      step 4: score, decide, provision account, publish event
module-account/                 account provisioning, login, and profile updates
module-infrastructure/          persistence, generators, notifiers, BCrypt, scoring,
                                RabbitMQ and Avro adapters
module-application/             Spring Boot composition root, global web/E2E configuration
```

Business modules depend on `shared-kernel`; scoring additionally calls the public
account-provisioning API. `infrastructure` implements outbound ports, and only
`application` assembles every module into an executable. Tests exercise each module
with plain port doubles and pin the integrated web flow by overriding adapter beans.

## Quick start

Requirements:

- Java 21
- Docker with Compose
- Bun 1.3 or newer

Start the supporting services and backend:

```bash
docker compose up -d rabbitmq apicurio-registry
./gradlew :module-application:bootRun
```

In another terminal, start the Next.js frontend:

```bash
cd fe-next
bun install
bun run dev
```

Open `http://localhost:5174`. The frontend's server-side `/api` proxy forwards
requests to the Spring Boot API at `http://127.0.0.1:8080`, avoiding browser
CORS issues. Set `API_PROXY_TARGET` before `bun run dev` to use another backend.

At the verification step, copy the six-digit token printed by the backend.
Approved applications also print their generated login password to the backend
console.

## Backend (port 8080)

```bash
./gradlew test                         # unit and integration tests
./gradlew check                        # tests plus the 80% coverage gate
./gradlew jacocoRootReport             # aggregate HTML and XML coverage
./gradlew :module-application:bootRun  # start the API
```

RabbitMQ is available at `localhost:5672`; its management UI is at
`http://localhost:15672` with `guest` / `guest`. Apicurio Registry is exposed at
`http://localhost:8081`.

After credit scoring is calculated, the backend emits a
`CreditScoringCalculatedEvent` through the outbound domain-event port. The
RabbitMQ adapter publishes it to the `event-onboarding.domain-events` exchange
with routing key `onboarding.credit-scoring.calculated`.

The same scoring step also generates an account password. The username is the
application email address, and the password is printed by the console credential
notifier.

### API

| Method & path | Body | Returns |
|---|---|---|
| `POST /api/onboarding/start` | `{ email }` | `{ applicationId, step }` |
| `POST /api/onboarding/{id}/verify-token` | `{ token }` | `{ verified, step }` |
| `POST /api/onboarding/{id}/fulfillment` | `{ name, email, phone, salary, yearsOfExperience }` | `{ step }` |
| `POST /api/onboarding/{id}/score` | — | `{ score, approved, step }` |
| `GET  /api/onboarding/{id}` | — | application state (no token) |
| `POST /api/auth/login` | `{ email, password }` | applicant profile |
| `PUT /api/auth/profile` | profile fields plus `email` and `password` | updated profile |

## Next.js frontend (port 5174)

The primary frontend is in `fe-next`. It uses the Next.js App Router, React,
TypeScript, and Bun. Wizard and profile state are retained in browser
`sessionStorage`, and guarded routes redirect when the required application or
profile state is missing.

```bash
cd fe-next
bun install
bun run dev
bun run type-check
bun run lint
bun run build
```

To use a different backend:

```bash
API_PROXY_TARGET=http://127.0.0.1:18080 bun run dev
```

## Other frontend implementations

The repository retains several alternative clients:

- `frontend/` — Vue 3, TypeScript, vue-router, and Vite on port `5173`
- `fe-cljs/` — ClojureScript implementation
- `fe-kobweb/` — Kotlin/Kobweb implementation

The Vue client includes the existing Playwright end-to-end suite:

```bash
cd frontend
npm install
npm run e2e
```

The Playwright suite uses the backend's `e2e` Spring profile. That profile keeps
production behavior unchanged but exposes `/api/e2e/scenario` so tests can set a
fixed verification token.

## Code quality with SonarQube

The root Gradle build uses SonarScanner for Gradle to analyze all Kotlin backend
modules. The `sonar` task runs the test suite and aggregate JaCoCo report first,
then uploads the analysis and combined coverage to a local SonarQube instance.
Frontend source trees are not included in this analysis.

Start the local SonarQube Community Build:

```bash
docker compose up -d sonarqube
docker compose ps sonarqube
```

SonarQube is available at `http://localhost:9000` and requires about 4 GB of
available memory. On the first visit, sign in with `admin` / `admin`, change the
password, then create a user token under **My Account → Security**.

Run the analysis without committing the token:

```bash
SONAR_TOKEN=<token> ./gradlew sonar
```

To use another SonarQube server:

```bash
SONAR_HOST_URL=https://sonarqube.example.com SONAR_TOKEN=<token> ./gradlew sonar
```

The command uploads the result without waiting for the server-side quality gate.
The existing `./gradlew check` command continues to enforce the local 80% line
coverage threshold independently. The Compose service uses SonarQube's embedded
database for local development only; use a supported external database for a
production installation.
