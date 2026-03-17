import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { service } from '../services';
import '../styles/forms.css';

export default function RegisterPage() {
  const { register } = useAuth();
  const [step, setStep] = useState<'register' | 'confirm'>('register');

  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [code, setCode] = useState('');

  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const handleRegister = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!username.trim() || !password.trim() || !email.trim()) {
      setError('Все поля обязательны');
      return;
    }

    setLoading(true);
    try {
      const result = await register({ username: username.trim(), password, email: email.trim() });
      setSuccess(result.message || 'Код подтверждения отправлен на email');
      setStep('confirm');
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { error?: string } } })?.response?.data?.error
        || (err instanceof Error ? err.message : 'Ошибка регистрации');
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const handleConfirm = async (e: FormEvent) => {
    e.preventDefault();
    setError('');

    if (!code.trim()) {
      setError('Введите код подтверждения');
      return;
    }

    setLoading(true);
    try {
      await service.confirm(username.trim(), code.trim());
      setSuccess('Аккаунт подтверждён! Теперь можете войти.');
      setStep('register');
      setUsername('');
      setEmail('');
      setPassword('');
      setCode('');
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { error?: string } } })?.response?.data?.error
        || (err instanceof Error ? err.message : 'Неверный код');
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    setError('');
    try {
      await service.resend(email.trim());
      setSuccess('Код отправлен повторно');
    } catch {
      setError('Ошибка при повторной отправке');
    }
  };

  return (
    <div className="form-page">
      <div className="form-card">
        {step === 'register' ? (
          <>
            <h2>Регистрация</h2>

            {error && <div className="form-error">{error}</div>}
            {success && <div className="form-success">{success} — <Link to="/login">Войти</Link></div>}

            <form onSubmit={handleRegister}>
              <div className="form-group">
                <label htmlFor="username">Имя пользователя</label>
                <input
                  id="username"
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="Введите имя пользователя"
                  disabled={loading}
                />
              </div>

              <div className="form-group">
                <label htmlFor="email">Email</label>
                <input
                  id="email"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="Введите email"
                  disabled={loading}
                />
              </div>

              <div className="form-group">
                <label htmlFor="password">Пароль</label>
                <input
                  id="password"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Введите пароль"
                  disabled={loading}
                />
              </div>

              <button type="submit" className="form-button" disabled={loading}>
                {loading ? 'Регистрация...' : 'Зарегистрироваться'}
              </button>
            </form>

            <div className="form-footer">
              Уже есть аккаунт? <Link to="/login">Войти</Link>
            </div>
          </>
        ) : (
          <>
            <h2>Подтверждение email</h2>
            <p style={{ marginBottom: '16px', color: 'var(--color-text-secondary)', fontSize: '0.9rem' }}>
              Код отправлен на <strong>{email}</strong>
            </p>

            {error && <div className="form-error">{error}</div>}
            {success && <div className="form-success">{success}</div>}

            <form onSubmit={handleConfirm}>
              <div className="form-group">
                <label htmlFor="code">Код подтверждения</label>
                <input
                  id="code"
                  type="text"
                  value={code}
                  onChange={(e) => setCode(e.target.value)}
                  placeholder="Введите код из письма"
                  disabled={loading}
                />
              </div>

              <button type="submit" className="form-button" disabled={loading}>
                {loading ? 'Проверка...' : 'Подтвердить'}
              </button>
            </form>

            <div className="form-footer">
              <button
                onClick={handleResend}
                style={{ background: 'none', border: 'none', color: 'var(--color-primary)', cursor: 'pointer', fontSize: '0.875rem' }}
              >
                Отправить код повторно
              </button>
              {' · '}
              <button
                onClick={() => { setStep('register'); setError(''); setSuccess(''); }}
                style={{ background: 'none', border: 'none', color: 'var(--color-text-secondary)', cursor: 'pointer', fontSize: '0.875rem' }}
              >
                Назад
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
