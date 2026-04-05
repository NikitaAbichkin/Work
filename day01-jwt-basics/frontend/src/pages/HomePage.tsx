import { Link } from 'react-router-dom';
import { useAuth } from '../context/useAuth';
import '../styles/home.css';

export default function HomePage() {
  const { isAuthenticated } = useAuth();

  return (
    <div className="home-page">
      <section className="hero-card">
        <div className="hero-copy">
          <span className="hero-kicker">JWT flow without backend edits</span>
          <h1>Пользовательский сценарий для проверки auth и целей</h1>
          <p className="subtitle">
            Зарегистрируйся, подтверди почту, войди, проверь защищённый профиль и пройди
            сценарий с целями так, как это увидит обычный пользователь.
          </p>
        </div>

        <div className="home-cta">
          {isAuthenticated ? (
            <>
              <Link to="/dashboard" className="cta-primary">
                Открыть панель
              </Link>
              <Link to="/goals" className="cta-secondary">
                Перейти к целям
              </Link>
            </>
          ) : (
            <>
              <Link to="/register" className="cta-primary">
                Создать аккаунт
              </Link>
              <Link to="/login" className="cta-secondary">
                Уже есть вход
              </Link>
            </>
          )}
        </div>
      </section>

      <section className="home-grid">
        <article className="home-panel">
          <p className="panel-eyebrow">Что проверить</p>
          <ol className="home-steps">
            <li>
              <span className="step-number">1</span>
              <div className="step-content">
                <h3>Регистрация и подтверждение</h3>
                <p>Фронт создаёт пользователя, затем отправляет код подтверждения на отдельном шаге.</p>
              </div>
            </li>
            <li>
              <span className="step-number">2</span>
              <div className="step-content">
                <h3>Логин и сессия</h3>
                <p>После входа токен сохраняется, а защищённые страницы открываются без ручных действий.</p>
              </div>
            </li>
            <li>
              <span className="step-number">3</span>
              <div className="step-content">
                <h3>Защищённый запрос</h3>
                <p>В панели видно, отвечает ли профильный эндпоинт с `Authorization: Bearer ...`.</p>
              </div>
            </li>
            <li>
              <span className="step-number">4</span>
              <div className="step-content">
                <h3>Работа с целями</h3>
                <p>Создай цель, открой детали, добавь задачи и результат, чтобы проверить прикладной flow.</p>
              </div>
            </li>
          </ol>
        </article>

        <aside className="home-panel home-checklist">
          <p className="panel-eyebrow">Мини-чеклист</p>
          <ul>
            <li>Маршруты `/login`, `/register`, `/dashboard`, `/goals` связаны между собой.</li>
            <li>После перезагрузки сессия восстанавливается из `localStorage`, если токен живой.</li>
            <li>При `401` фронт чистит токен и отправляет пользователя обратно на логин.</li>
            <li>Верхний бар показывает текущий режим: `Mock` или реальный `API`.</li>
          </ul>
        </aside>
      </section>
    </div>
  );
}
