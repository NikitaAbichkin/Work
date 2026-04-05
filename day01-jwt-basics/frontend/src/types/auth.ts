export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
}

export interface LoginResponse {
  access_token: string;
  token_type: string;
  refreshToken?: string;
}

export interface RegisterResponse {
  username: string;
  message: string;
}

export interface ProtectedStatusResponse {
  totalGoals: number;
  hasGoals: boolean;
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
  logout(refreshToken: string): Promise<void>;
  confirm(username: string, code: string): Promise<void>;
  resend(email: string): Promise<void>;
}
