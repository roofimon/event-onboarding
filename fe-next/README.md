# Event Onboarding — Next.js

A Next.js App Router frontend for the Event Onboarding Spring Boot API.

## Run locally

Start the backend on port `8080`, then:

```bash
bun install
bun run dev
```

Open [http://localhost:5174](http://localhost:5174). Next.js proxies `/api`
requests to `http://127.0.0.1:8080`.

Set `API_PROXY_TARGET` to point at a different backend:

```bash
API_PROXY_TARGET=http://127.0.0.1:18080 bun run dev
```

## Checks

```bash
bun run type-check
bun run lint
bun run build
```

## End-to-end tests

Install dependencies and the Chromium browser once:

```bash
bun install
bunx playwright install chromium
```

Run the Playwright suite:

```bash
bun run e2e
```

The Playwright configuration starts the backend with its `e2e` profile on port
`18080` and the Next.js frontend on port `15174`. To see the browser:

```bash
bun run e2e:headed
```
