const rawApiUrl = (import.meta.env.VITE_API_URL as string | undefined)?.trim();

function resolveApiUrl(): string {
  const browserHost = window.location.hostname;
  const envUrl = rawApiUrl && rawApiUrl.length > 0 ? rawApiUrl : `http://${browserHost}:8080`;

  // If env still points to localhost but app is opened from LAN/public host, use the current host.
  if (envUrl.includes('localhost') && !['localhost', '127.0.0.1'].includes(browserHost)) {
    return envUrl.replace('localhost', browserHost);
  }

  return envUrl;
}

export const config = {
  apiUrl: resolveApiUrl(),
  useMock: (import.meta.env.VITE_USE_MOCK as string || 'false') === 'true',
  port: parseInt(import.meta.env.VITE_PORT as string || '5173', 10),
} as const;
