import { defineConfig } from 'vite'
import react, { reactCompilerPreset } from '@vitejs/plugin-react'
import babel from '@rolldown/plugin-babel'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    babel({ presets: [reactCompilerPreset()] })
  ],
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
