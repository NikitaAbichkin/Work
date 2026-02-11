import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import type { User, LoginRequest, RegisterRequest, RegisterResponse } from '../types/auth';
import { service } from '../services';
import { getToken, setToken, removeToken, decodeToken, isTokenExpired } from '../utils/token';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<RegisterResponse>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setTokenState] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // При монтировании: проверяем localStorage на наличие токена
  useEffect(() => {
    const savedToken = getToken();
    if (savedToken && !isTokenExpired(savedToken)) {
      const payload = decodeToken(savedToken);
      if (payload) {
        setTokenState(savedToken);
        setUser({ username: payload.username, user_id: payload.user_id });
      } else {
        removeToken();
      }
    } else if (savedToken) {
      // Токен есть, но истёк
      removeToken();
    }
    setIsLoading(false);
  }, []);

  const login = async (data: LoginRequest): Promise<void> => {
    const response = await service.login(data);
    const newToken = response.access_token;
    setToken(newToken);
    setTokenState(newToken);

    const payload = decodeToken(newToken);
    if (payload) {
      setUser({ username: payload.username, user_id: payload.user_id });
    }
  };

  const register = async (data: RegisterRequest): Promise<RegisterResponse> => {
    return service.register(data);
  };

  const logout = () => {
    removeToken();
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

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth должен использоваться внутри AuthProvider');
  }
  return context;
}
