import { config } from '../config';
import { mockService } from './mock.service';
import { authService } from './auth.service';
import type { AuthService } from '../types/auth';

export const service: AuthService = config.useMock ? mockService : authService;
