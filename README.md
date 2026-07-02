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

## Architecture (Ports & Adapters / Hexagonal)

The core is framework-free and depends only on ports; Spring lives at the edges.

```
domain/                         pure entities & rules (OnboardingApplication, OnboardingStep,
                                APPROVAL_THRESHOLD, exceptions) — no Spring
application/
  service/                      OnboardingService (implements the use case via ports)
ports/
  inbound/                      OnboardingUseCase           ← driving port
  outbound/                     ApplicationRepository, TokenGenerator,
                                VerificationNotifier, CreditScorer,
                                PasswordGenerator, CredentialNotifier   ← driven ports
adapters/
  inbound/web/                  OnboardingController, ExceptionHandler, DTOs, CORS
  outbound/persistence/         InMemoryApplicationRepository
  outbound/token/               RandomTokenGenerator, RandomPasswordGenerator
  outbound/notification/        ConsoleVerificationNotifier, ConsoleCredentialNotifier
                                (the "print to console" adapters)
  outbound/scoring/             RandomCreditScorer
config/                         BeanConfiguration — composition root wiring the service
```

The controller depends on the `OnboardingUseCase` port, never on the concrete
service. The service depends only on outbound ports, so the in-memory store,
random token/password/score and console delivery are all swappable without touching the
core (e.g. drop in a JPA repository or an email notifier by adding one adapter).
Tests exercise the service with plain port doubles and pin the web flow by
overriding the `TokenGenerator`/`CreditScorer` beans.

## Backend (port 8080)

```bash
docker compose up -d rabbitmq
./gradlew test       # run unit + MockMvc tests
./gradlew bootRun    # start the API on http://localhost:8080
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
npm install
npm run dev          # http://localhost:5173
npm run type-check   # vue-tsc --noEmit
npm run build        # type-check + production build
npm run e2e          # starts backend with the e2e profile + Vite, then runs Playwright headless
```

The Vite dev server proxies `/api` to `http://localhost:8080`, so run the backend
alongside it. Walk through the four steps in the browser — at step 2, copy the
token from the backend console.

The Playwright suite uses the backend's `e2e` Spring profile. That profile keeps
production randomness unchanged, but exposes `/api/e2e/scenario` so tests can
set a fixed token and credit score for success and decline cases.
