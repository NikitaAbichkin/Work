import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/useAuth';
import { goalsService } from '../services/goals.service';
import { decodeToken } from '../utils/token';
import type { ProtectedStatusResponse } from '../types/auth';
import '../styles/dashboard.css';

export default function DashboardPage() {
  const { user, token } = useAuth();
  const payload = token ? decodeToken(token) : null;
  const [protectedStatus, setProtectedStatus] = useState<ProtectedStatusResponse | null>(null);
  const [profileState, setProfileState] = useState<'idle' | 'loading' | 'success' | 'error'>('loading');
  const [profileError, setProfileError] = useState('');
  const [currentTime, setCurrentTime] = useState(() => Date.now());

  useEffect(() => {
    if (!token) return;

    let cancelled = false;

    goalsService.getGoals(0, 1)
      .then((data) => {
        if (cancelled) return;
        setProtectedStatus({
          totalGoals: data.totalElements ?? data.content.length,
          hasGoals: (data.totalElements ?? data.content.length) > 0,
        });
        setProfileState('success');
      })
      .catch((err: unknown) => {
        if (cancelled) return;
        setProfileState('error');
        setProfileError(err instanceof Error ? err.message : 'Не удалось загрузить профиль');
      });

    return () => {
      cancelled = true;
    };
  }, [token]);

  useEffect(() => {
    const timerId = window.setInterval(() => {
      setCurrentTime(Date.now());
    }, 1000);

    return () => {
      window.clearInterval(timerId);
    };
  }, []);

  const formatExpiry = (exp: number) => {
    const date = new Date(exp * 1000);
    return date.toLocaleString('ru-RU');
  };

  const timeUntilExpiry = (exp: number) => {
    const diff = exp * 1000 - currentTime;
    if (diff <= 0) return 'Истёк';
    const minutes = Math.floor(diff / 60000);
    const seconds = Math.floor((diff % 60000) / 1000);
    return `${minutes}мин ${seconds}сек`;
  };

  return (
    <div className="dashboard-page">
      <section className="dashboard-hero">
        <div>
          <p className="dashboard-kicker">Личный кабинет</p>
          <h1>Проверка пользовательской сессии</h1>
          <p className="dashboard-subtitle">
            Здесь видно, что логин сработал, токен декодируется, а защищённый эндпоинт отвечает.
          </p>
        </div>
        <Link to="/goals" className="dashboard-link">
          Открыть цели
        </Link>
      </section>

      <div className="dashboard-summary">
        <div className="summary-card">
          <span className="summary-label">Пользователь</span>
          <strong>{user?.username ?? 'Не найден'}</strong>
        </div>
        <div className="summary-card">
          <span className="summary-label">ID</span>
          <strong>{user?.user_id ?? '—'}</strong>
        </div>
        <div className="summary-card">
          <span className="summary-label">Проверка API</span>
          <strong>
            {profileState === 'loading' && 'Проверяем...'}
            {profileState === 'success' && 'Ответ получен'}
            {profileState === 'error' && 'Ошибка'}
            {profileState === 'idle' && 'Ожидание'}
          </strong>
        </div>
      </div>

      <div className="dashboard-card">
        <h3>Информация о пользователе</h3>
        <div className="dashboard-field">
          <span className="label">Имя пользователя</span>
          <span className="value">{user?.username}</span>
        </div>
        <div className="dashboard-field">
          <span className="label">ID пользователя</span>
          <span className="value">{user?.user_id}</span>
        </div>
      </div>

      <div className="dashboard-card">
        <h3>Проверка защищённого эндпоинта</h3>
        <div className={`dashboard-status dashboard-status-${profileState}`}>
          {profileState === 'loading' && 'Отправляем `GET /api/v1/goals?page=0&size=1` с Bearer-токеном...'}
          {profileState === 'success' && `API ответил успешно. Найдено целей: ${protectedStatus?.totalGoals ?? 0}`}
          {profileState === 'error' && `Защищённый запрос к goals вернул ошибку: ${profileError}`}
          {profileState === 'idle' && 'Запрос ещё не запускался'}
        </div>
      </div>

      {payload && (
        <div className="dashboard-card">
          <h3>Информация о токене</h3>
          <div className="dashboard-field">
            <span className="label">Истекает</span>
            <span className="value">{formatExpiry(payload.exp)}</span>
          </div>
          <div className="dashboard-field">
            <span className="label">Осталось времени</span>
            <span className="value">{timeUntilExpiry(payload.exp)}</span>
          </div>
          {payload.iat && (
            <div className="dashboard-field">
              <span className="label">Выдан</span>
              <span className="value">{formatExpiry(payload.iat)}</span>
            </div>
          )}
        </div>
      )}

      {payload && (
        <div className="dashboard-card">
          <h3>Декодированный payload</h3>
          <div className="token-payload">{JSON.stringify(payload, null, 2)}</div>
        </div>
      )}

      {token && (
        <div className="dashboard-card">
          <h3>Сырой токен</h3>
          <div className="token-raw">{token}</div>
        </div>
      )}
    </div>
  );
}
