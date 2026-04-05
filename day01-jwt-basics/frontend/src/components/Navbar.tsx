import { Link, NavLink } from 'react-router-dom';
import { useAuth } from '../context/useAuth';
import { config } from '../config';
import '../styles/navbar.css';

export default function Navbar() {
  const { isAuthenticated, user, logout } = useAuth();
  const modeLabel = config.useMock ? 'Mock' : 'API';

  return (
    <nav className="navbar">
      <div className="navbar-brand-group">
        <Link to="/" className="navbar-brand">
          Focus Flow
        </Link>
        <div className="navbar-meta">
          <span className={`mode-badge ${config.useMock ? 'mode-badge-mock' : 'mode-badge-live'}`}>
            {modeLabel}
          </span>
          <span className="navbar-api">{config.apiUrl}</span>
        </div>
      </div>

      <div className="navbar-links">
        <NavLink to="/">Главная</NavLink>

        {isAuthenticated ? (
          <>
            <NavLink to="/dashboard">Панель</NavLink>
            <NavLink to="/goals">Цели</NavLink>
            <div className="navbar-user">
              <span className="navbar-user-label">Сессия</span>
              <span className="navbar-user-name">{user?.username}</span>
            </div>
            <button className="navbar-logout" onClick={() => void logout()}>
              Выйти
            </button>
          </>
        ) : (
          <>
            <NavLink to="/login">Войти</NavLink>
            <Link to="/register" className="navbar-cta">
              Попробовать
            </Link>
          </>
        )}
      </div>
    </nav>
  );
}
