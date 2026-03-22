import { createContext } from 'react';
import type { User, LoginRequest, RegisterRequest, RegisterResponse } from '../types/auth';

export interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<RegisterResponse>;
  logout: () => Promise<void>;
}

export const AuthContext = createContext<AuthContextType | null>(null);
