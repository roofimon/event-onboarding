import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const apiProxyTarget = process.env.E2E_API_PROXY_TARGET ?? 'http://127.0.0.1:8080'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    // Proxy API calls to the Spring Boot backend during development.
    proxy: {
      '/api': {
        target: apiProxyTarget,
        changeOrigin: true,
      },
    },
  },
})
