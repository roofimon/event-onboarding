# Prerequisites & Setup

Everything needed to run the backend, the frontend, and the Playwright
end-to-end suite locally.

## 1. Required tooling

| Tool | Version | Why |
|---|---|---|
| JDK | **21** | All backend modules pin `JavaLanguageVersion.of(21)` via the Gradle toolchain. |
| [Bun](https://bun.sh) | latest | Frontend package manager and script runner (replaces npm). Install: `curl -fsSL https://bun.sh/install \| bash`. |
| Node.js | **>= 18** on `PATH` | Only needed because a couple of frontend tools (`vue-tsc`, `playwright`) are shebang scripts (`#!/usr/bin/env node`) that Bun executes via the system Node rather than its own runtime. Playwright refuses to run at all on Node < 18. Vite itself doesn't have this constraint — `bun run dev`/`build`/`preview` explicitly force Bun's own runtime (`bun --bun vite`) so they work even if the default Node is old. |
| Docker + Docker Compose | any recent | Runs RabbitMQ and the Apicurio schema registry. |

The repo doesn't have a `.nvmrc`/`sdkman` file, so verify manually:

```bash
java -version      # must report 21 (Gradle toolchains auto-detect installed JDKs
                    # but won't download one for you — no foojay resolver is configured)
bun --version
node --version      # must be >= 18
docker compose version
```

If your default Node is older than 18 (check with `nvm alias default` if you
use nvm), either switch it (`nvm alias default 20`) or make sure a newer
Node is ahead of the old one on `PATH` before running `bun run type-check`,
`bun run build`, or `bun run e2e`.

## 2. Start infrastructure

```bash
docker compose up -d rabbitmq apicurio-registry
docker compose ps
```

- RabbitMQ: `localhost:5672` (AMQP), management UI at <http://localhost:15672> (`guest`/`guest`)
- Apicurio Registry: <http://localhost:8081>

Only `rabbitmq` is strictly required to run the app — domain-event publishing
is fire-and-forget, so a missing Apicurio registry just logs a warning
instead of breaking onboarding. Bring up `apicurio-registry` too if you want
to inspect the registered Avro schema (see [`SCHEMA.md`](SCHEMA.md)).

Connection details are hardcoded in
[`module-application/src/main/resources/application.properties`](module-application/src/main/resources/application.properties)
(`localhost:5672`, `guest`/`guest`, registry at `localhost:8081`) — no `.env`
file or extra configuration is needed for local development.

## 3. Backend

```bash
./gradlew test                          # unit + MockMvc tests
./gradlew :module-application:bootRun   # start the API on http://localhost:8080
```

## 4. Frontend

```bash
cd frontend
bun install
bun run dev          # http://localhost:5173, proxies /api to localhost:8080
bun run type-check   # vue-tsc --noEmit
bun run build         # type-check + production build
```

Run the backend (step 3) alongside the dev server. Walk through the wizard
in the browser — at step 2, copy the verification token from the backend
console; after step 4, the generated account credentials also print there.

## 5. End-to-end tests (Playwright)

```bash
cd frontend
bunx playwright install --with-deps chromium   # one-time browser install
bun run e2e            # headless
bun run e2e:headed     # with a visible browser window
```

`bun run e2e` runs `playwright test`, which (per
[`playwright.config.ts`](frontend/playwright.config.ts)) automatically:

1. Starts the backend with `./gradlew :module-application:bootRun` on the
   `e2e` Spring profile, bound to `127.0.0.1:18080`. That profile leaves
   production randomness untouched but exposes `POST /api/e2e/scenario`,
   which lets tests pin a fixed token and credit score for deterministic
   success/decline cases (see
   [`E2eTestConfiguration.kt`](module-application/src/main/kotlin/com/example/eventonboarding/application/E2eTestConfiguration.kt)).
2. Starts the Vite dev server on `127.0.0.1:15173`, proxying `/api` to the
   e2e backend instance above.
3. Runs the specs in [`frontend/e2e/onboarding.spec.ts`](frontend/e2e/onboarding.spec.ts)
   against `http://127.0.0.1:15173` in headless Chromium.

Both servers are managed by Playwright (started fresh and torn down per
run) — you do **not** need to start the backend or frontend yourself first,
just make sure RabbitMQ is up (step 2) since the `e2e` backend still
connects to it.

### One-time browser install caveat (Linux/WSL)

`bunx playwright install --with-deps chromium` needs `sudo` to install OS
packages (e.g. `libasound2`) the first time. If your shell can't prompt for
a sudo password (common under WSL or in a non-interactive session), run it
manually first:

```bash
sudo bunx playwright install-deps chromium
bunx playwright install chromium
```

Without these OS libraries, tests fail at browser launch with an error like
`error while loading shared libraries: libasound.so.2`.

## Ports summary

| Port | Service |
|---|---|
| 8080 | Backend API (normal `bootRun`) |
| 18080 | Backend API (`e2e` profile, Playwright-managed) |
| 5173 | Frontend dev server |
| 15173 | Frontend dev server (`e2e`, Playwright-managed) |
| 5672 | RabbitMQ (AMQP) |
| 15672 | RabbitMQ management UI |
| 8081 | Apicurio Registry |
