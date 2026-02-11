import { Link, NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { config } from '../config';
import '../styles/navbar.css';

export default function Navbar() {
  const { isAuthenticated, user, logout } = useAuth();

  return (
    <nav className="navbar">
      <Link to="/" className="navbar-brand">
        JWT Auth
      </Link>

      <div className="navbar-links">
        {config.useMock && <span className="mock-banner">Мок-режим</span>}

        <NavLink to="/">Главная</NavLink>

        {isAuthenticated ? (
          <>
            <NavLink to="/dashboard">Панель</NavLink>
            <span style={{ color: 'var(--color-text-secondary)', fontSize: '0.875rem' }}>
              {user?.username}
            </span>
            <button className="navbar-logout" onClick={logout}>
              Выйти
            </button>
          </>
        ) : (
          <>
            <NavLink to="/login">Войти</NavLink>
            <NavLink to="/register">Регистрация</NavLink>
          </>
        )}
      </div>
    </nav>
  );
}
