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
    const response = await api.post<RegisterResponse>('/register', data);
    return response.data;
  },

  async login(data: LoginRequest): Promise<LoginResponse> {
    const response = await api.post<LoginResponse>('/login', data);
    return response.data;
  },

  async getProfile(token: string): Promise<ProfileResponse> {
    const response = await api.get<ProfileResponse>('/profile', {
      headers: { Authorization: `Bearer ${token}` },
    });
    return response.data;
  },
};
