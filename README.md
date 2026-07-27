# Event Onboarding

A 4-step onboarding wizard: **Kotlin + Spring Boot** REST backend with an
in-memory store, driven by a **Vue.js** single-page frontend.

## The flow

| Step | What the user does | What the backend does |
|------|--------------------|-----------------------|
| 1. Email | Submits only an email | Creates an application, generates a 6-digit token and **prints it to the console** |
| 2. Token verify | Enters the token from the console | Verifies it |
| 3. Fulfillment | Enters name, email, phone, salary, years of experience | Stores the details |
| 4. Credit scoring | — | Computes a random score (0–100), generates an account password with the email as username, and **prints the credentials to the console**. **> 40 → welcome page**, otherwise **decline page** |

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

## Backend (port 8080)

```bash
docker compose up -d rabbitmq
./gradlew test       # run unit + MockMvc tests
./gradlew :module-application:bootRun  # start the API on http://localhost:8080
```

RabbitMQ is available on `localhost:5672`; the management UI is available at
`http://localhost:15672` with `guest` / `guest`.

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

## Frontend (port 5173)

Vue 3 + **TypeScript** + vue-router, built with Vite. Shared API types live in
`src/types.ts` and mirror the backend DTOs; the build is type-checked by `vue-tsc`.

```bash
cd frontend
bun install
bun run dev          # http://localhost:5173
bun run type-check   # vue-tsc --noEmit
bun run build        # type-check + production build
bun run e2e          # starts backend with the e2e profile + Vite, then runs Playwright headless
```

The Vite dev server proxies `/api` to `http://localhost:8080`, so run the backend
alongside it. Walk through the four steps in the browser — at step 2, copy the
token from the backend console.

The Playwright suite uses the backend's `e2e` Spring profile. That profile keeps
production randomness unchanged, but exposes `/api/e2e/scenario` so tests can
set a fixed token and credit score for success and decline cases.
