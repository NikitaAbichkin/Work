import axios from 'axios';
import { config } from '../config';
import { getToken, isTokenExpired, removeToken } from '../utils/token';

const api = axios.create({
  baseURL: config.apiUrl,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Перехватчик запросов: прикрепляем токен
api.interceptors.request.use(
  (reqConfig) => {
    const token = getToken();
    if (token) {
      if (isTokenExpired(token)) {
        removeToken();
        window.location.href = '/login';
        return Promise.reject(new Error('Токен истёк'));
      }
      reqConfig.headers.Authorization = `Bearer ${token}`;
    }
    return reqConfig;
  },
  (error) => Promise.reject(error)
);

// Перехватчик ответов: обработка 401
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      removeToken();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
