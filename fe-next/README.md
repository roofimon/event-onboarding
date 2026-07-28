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
