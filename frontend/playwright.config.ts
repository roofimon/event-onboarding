import { defineConfig, devices } from '@playwright/test'

export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: 'http://127.0.0.1:15173',
    headless: true,
    trace: 'on-first-retry',
  },
  webServer: [
    {
      command: "cd .. && ./gradlew :application:bootRun --args='--spring.profiles.active=e2e --server.address=127.0.0.1 --server.port=18080'",
      url: 'http://127.0.0.1:18080/api/e2e/health',
      reuseExistingServer: false,
      timeout: 120_000,
    },
    {
      command: 'E2E_API_PROXY_TARGET=http://127.0.0.1:18080 npm run dev -- --host 127.0.0.1 --port 15173',
      url: 'http://127.0.0.1:15173',
      reuseExistingServer: false,
      timeout: 120_000,
    },
  ],
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
})
