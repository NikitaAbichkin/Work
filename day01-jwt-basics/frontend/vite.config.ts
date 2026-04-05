import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))

// Парсим ForFRONTEND.env вручную
function loadCustomEnv(): Record<string, string> {
  const result: Record<string, string> = {}
  const envPath = path.resolve(__dirname, 'ForFRONTEND.env')

  if (fs.existsSync(envPath)) {
    const content = fs.readFileSync(envPath, 'utf-8')
    for (const line of content.split('\n')) {
      const trimmed = line.trim()
      if (trimmed && !trimmed.startsWith('#')) {
        const eqIndex = trimmed.indexOf('=')
        if (eqIndex !== -1) {
          const key = trimmed.slice(0, eqIndex).trim()
          const value = trimmed.slice(eqIndex + 1).trim()
          result[key] = value
        }
      }
    }
  }

  return result
}

const customEnv = loadCustomEnv()
const viteApiUrl = process.env.VITE_API_URL || customEnv.VITE_API_URL || 'http://localhost:8000/api'
const viteUseMock = process.env.VITE_USE_MOCK || customEnv.VITE_USE_MOCK || 'true'
const vitePort = process.env.VITE_PORT || customEnv.VITE_PORT || '5173'

export default defineConfig({
  plugins: [react()],
  server: {
    host: true,
    port: parseInt(vitePort, 10),
  },
  define: {
    'import.meta.env.VITE_API_URL': JSON.stringify(viteApiUrl),
    'import.meta.env.VITE_USE_MOCK': JSON.stringify(viteUseMock),
    'import.meta.env.VITE_PORT': JSON.stringify(vitePort),
  },
})
