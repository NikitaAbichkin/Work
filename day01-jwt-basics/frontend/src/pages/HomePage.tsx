import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import '../styles/home.css';

export default function HomePage() {
  const { isAuthenticated } = useAuth();

  return (
    <div className="home-page">
      <h1>JWT Аутентификация — Демо</h1>
      <p className="subtitle">
        Узнай, как JSON Web Tokens работают в реальном фронтенд-приложении
      </p>

      <ol className="home-steps">
        <li>
          <span className="step-number">1</span>
          <div className="step-content">
            <h3>Регистрация</h3>
            <p>Создай аккаунт с именем пользователя и паролем. Сервер сохраняет твои данные.</p>
          </div>
        </li>
        <li>
          <span className="step-number">2</span>
          <div className="step-content">
            <h3>Вход</h3>
            <p>
              Отправь данные на сервер. Он вернёт JWT-токен с информацией о пользователе,
              подписанный секретным ключом.
            </p>
          </div>
        </li>
        <li>
          <span className="step-number">3</span>
          <div className="step-content">
            <h3>Хранение токена</h3>
            <p>
              Токен сохраняется в localStorage. Это base64-кодированная JSON-строка с частями:
              header, payload и signature.
            </p>
          </div>
        </li>
        <li>
          <span className="step-number">4</span>
          <div className="step-content">
            <h3>Защищённые маршруты</h3>
            <p>
              Каждый API-запрос включает токен в заголовке Authorization. Сервер проверяет
              подпись перед тем, как дать доступ.
            </p>
          </div>
        </li>
      </ol>

      <div className="home-cta">
        {isAuthenticated ? (
          <Link to="/dashboard" className="cta-primary">
            Перейти в панель
          </Link>
        ) : (
          <>
            <Link to="/register" className="cta-primary">
              Начать
            </Link>
            <Link to="/login" className="cta-secondary">
              Уже есть аккаунт
            </Link>
          </>
        )}
      </div>
    </div>
  );
}
