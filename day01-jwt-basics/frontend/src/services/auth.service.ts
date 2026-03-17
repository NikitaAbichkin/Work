import api from './api';
import type {
  AuthService,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  ProfileResponse,
} from '../types/auth';

export const authService: AuthService = {
  async register(data: RegisterRequest): Promise<RegisterResponse> {
    const response = await api.post('/api/v1/auth/register', data);
    return response.data.data;
  },

  async login(data: LoginRequest): Promise<LoginResponse> {
    const response = await api.post('/api/v1/auth/login', data);
    return response.data.data;
  },

  async getProfile(token: string): Promise<ProfileResponse> {
    const response = await api.get('/api/v1/auth/profile', {
      headers: { Authorization: `Bearer ${token}` },
    });
    return response.data.data;
  },

  async confirm(username: string, code: string): Promise<void> {
    await api.post('/api/v1/auth/confirm', { username, code });
  },

  async resend(email: string): Promise<void> {
    await api.post('/api/v1/auth/resend', { email });
  },
};
