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

export default defineConfig({
  plugins: [react()],
  server: {
    port: parseInt(customEnv.VITE_PORT || '5173', 10),
  },
  define: {
    'import.meta.env.VITE_API_URL': JSON.stringify(customEnv.VITE_API_URL || 'http://localhost:8000/api'),
    'import.meta.env.VITE_USE_MOCK': JSON.stringify(customEnv.VITE_USE_MOCK || 'true'),
    'import.meta.env.VITE_PORT': JSON.stringify(customEnv.VITE_PORT || '5173'),
  },
})
