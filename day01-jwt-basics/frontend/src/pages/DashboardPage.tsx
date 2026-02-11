import { useAuth } from '../context/AuthContext';
import { decodeToken } from '../utils/token';
import '../styles/dashboard.css';

export default function DashboardPage() {
  const { user, token } = useAuth();
  const payload = token ? decodeToken(token) : null;

  const formatExpiry = (exp: number) => {
    const date = new Date(exp * 1000);
    return date.toLocaleString();
  };

  const timeUntilExpiry = (exp: number) => {
    const diff = exp * 1000 - Date.now();
    if (diff <= 0) return 'Истёк';
    const minutes = Math.floor(diff / 60000);
    const seconds = Math.floor((diff % 60000) / 1000);
    return `${minutes}мин ${seconds}сек`;
  };

  return (
    <div className="dashboard-page">
      <h1>Панель управления</h1>

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
