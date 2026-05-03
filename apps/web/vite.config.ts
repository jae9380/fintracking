import { defineConfig } from 'vite'
import react, { reactCompilerPreset } from '@vitejs/plugin-react'
import babel from '@rolldown/plugin-babel'
import tailwindcss from '@tailwindcss/vite'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    tailwindcss(),
    react(),
    babel({ presets: [reactCompilerPreset()] })
  ],
  server: {
    port: 5173,
    proxy: {
      '/auth-service':         { target: 'http://localhost:8000', changeOrigin: true },
      '/account-service':      { target: 'http://localhost:8000', changeOrigin: true },
      '/transaction-service':  { target: 'http://localhost:8000', changeOrigin: true },
      '/budget-service':       { target: 'http://localhost:8000', changeOrigin: true },
      '/notification-service': { target: 'http://localhost:8000', changeOrigin: true },
    },
  },
  resolve: {
    alias: {
      '@fintracking/types': resolve(__dirname, '../../packages/types/src'),
      '@fintracking/api': resolve(__dirname, '../../packages/api/src'),
      '@fintracking/services': resolve(__dirname, '../../packages/services/src'),
      '@fintracking/ui': resolve(__dirname, '../../packages/ui/src'),
      '@fintracking/utils': resolve(__dirname, '../../packages/utils/src'),
    },
  },
})
