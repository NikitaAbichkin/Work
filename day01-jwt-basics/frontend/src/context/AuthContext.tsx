import { useState, type ReactNode } from 'react';
import type { User, LoginRequest, RegisterRequest, RegisterResponse } from '../types/auth';
import { service } from '../services';
import {
  getRefreshToken,
  getToken,
  setToken,
  setRefreshToken,
  removeToken,
  removeRefreshToken,
  decodeToken,
  isTokenExpired,
} from '../utils/token';
import { AuthContext } from './auth-context';

interface InitialAuthState {
  user: User | null;
  token: string | null;
}

function getInitialAuthState(): InitialAuthState {
  const savedToken = getToken();
  if (!savedToken) {
    return { user: null, token: null };
  }

  if (isTokenExpired(savedToken)) {
    removeToken();
    return { user: null, token: null };
  }

  const payload = decodeToken(savedToken);
  if (!payload) {
    removeToken();
    return { user: null, token: null };
  }

  return {
    token: savedToken,
    user: { username: payload.username, user_id: payload.user_id },
  };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const initialState = getInitialAuthState();
  const [user, setUser] = useState<User | null>(initialState.user);
  const [token, setTokenState] = useState<string | null>(initialState.token);
  const [isLoading] = useState(false);

  const login = async (data: LoginRequest): Promise<void> => {
    const response = await service.login(data);
    const newToken = response.access_token;
    setToken(newToken);
    if (response.refreshToken) {
      setRefreshToken(response.refreshToken);
    }
    setTokenState(newToken);

    const payload = decodeToken(newToken);
    if (payload) {
      setUser({ username: payload.username, user_id: payload.user_id });
    }
  };

  const register = async (data: RegisterRequest): Promise<RegisterResponse> => {
    return service.register(data);
  };

  const logout = async () => {
    const refreshToken = getRefreshToken();
    if (refreshToken) {
      try {
        await service.logout(refreshToken);
      } catch {
        // Локально выходим даже если серверный logout не ответил.
      }
    }

    removeToken();
    removeRefreshToken();
    setTokenState(null);
    setUser(null);
  };

  const isAuthenticated = !!token && !!user;

  return (
    <AuthContext.Provider
      value={{ user, token, isAuthenticated, isLoading, login, register, logout }}
    >
      {children}
    </AuthContext.Provider>
  );
}
