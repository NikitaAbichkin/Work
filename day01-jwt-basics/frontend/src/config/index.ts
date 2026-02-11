export const config = {
  apiUrl: import.meta.env.VITE_API_URL as string || 'http://localhost:8000/api',
  useMock: (import.meta.env.VITE_USE_MOCK as string || 'true') === 'true',
  port: parseInt(import.meta.env.VITE_PORT as string || '5173', 10),
} as const;
