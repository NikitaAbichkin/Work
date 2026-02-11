import type {
  AuthService,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  ProfileResponse,
} from '../types/auth';

// "База данных" в памяти
interface MockUser {
  user_id: number;
  username: string;
  password: string;
}

let users: MockUser[] = [];
let nextId = 1;

function delay(ms = 500): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function createFakeJWT(payload: Record<string, unknown>): string {
  const header = { alg: 'HS256', typ: 'JWT' };
  const encode = (obj: unknown) =>
    btoa(JSON.stringify(obj))
      .replace(/\+/g, '-')
      .replace(/\//g, '_')
      .replace(/=+$/, '');

  const h = encode(header);
  const p = encode(payload);
  const signature = encode({ fake: 'signature' });

  return `${h}.${p}.${signature}`;
}

export const mockService: AuthService = {
  async register(data: RegisterRequest): Promise<RegisterResponse> {
    await delay();

    const exists = users.find((u) => u.username === data.username);
    if (exists) {
      throw new Error('Пользователь уже существует');
    }

    const user: MockUser = {
      user_id: nextId++,
      username: data.username,
      password: data.password,
    };
    users.push(user);

    return {
      username: user.username,
      message: 'Регистрация прошла успешно',
    };
  },

  async login(data: LoginRequest): Promise<LoginResponse> {
    await delay();

    const user = users.find(
      (u) => u.username === data.username && u.password === data.password
    );
    if (!user) {
      throw new Error('Неверное имя пользователя или пароль');
    }

    const now = Math.floor(Date.now() / 1000);
    const token = createFakeJWT({
      username: user.username,
      user_id: user.user_id,
      iat: now,
      exp: now + 3600, // 1 час
    });

    return {
      access_token: token,
      token_type: 'bearer',
    };
  },

  async getProfile(token: string): Promise<ProfileResponse> {
    await delay(300);

    // Декодируем фейковый токен для получения данных пользователя
    try {
      const parts = token.split('.');
      const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')));
      return {
        username: payload.username,
        user_id: payload.user_id,
      };
    } catch {
      throw new Error('Недействительный токен');
    }
  },
};
