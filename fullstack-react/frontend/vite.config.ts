import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // 개발 서버에서 /api 요청을 스프링부트(8080)로 전달 → CORS 없이 개발 가능
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
