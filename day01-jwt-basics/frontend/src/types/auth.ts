export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  access_token: string;
  token_type: string;
}

export interface RegisterResponse {
  username: string;
  message: string;
}

export interface ProfileResponse {
  username: string;
  user_id: number;
}

export interface TokenPayload {
  username: string;
  user_id: number;
  exp: number;
  iat?: number;
}

export interface User {
  username: string;
  user_id: number;
}

export interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}

export interface AuthService {
  login(data: LoginRequest): Promise<LoginResponse>;
  register(data: RegisterRequest): Promise<RegisterResponse>;
  getProfile(token: string): Promise<ProfileResponse>;
}
