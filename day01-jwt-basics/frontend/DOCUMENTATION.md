# JWT Аутентификация — Фронтенд. Документация

## Быстрый старт

```bash
cd frontend
npm install
npm run dev
```

Открой http://localhost:5173

---

## Конфигурация

Редактируй `ForFRONTEND.env`:

| Переменная | По умолчанию | Описание |
|------------|-------------|----------|
| `VITE_API_URL` | `http://localhost:8000/api` | Базовый URL бэкенд-API |
| `VITE_PORT` | `5173` | Порт дев-сервера |
| `VITE_USE_MOCK` | `true` | `true` = мок-режим (без бэкенда), `false` = реальный API |

---

## Структура проекта

```
src/
├── config/index.ts          — Читает env-переменные, экспортирует объект конфига
├── types/auth.ts            — TypeScript-интерфейсы для auth-потока
├── utils/token.ts           — Утилиты для JWT (сохранение, декодирование, проверка срока)
├── services/
│   ├── api.ts               — Axios-инстанс с перехватчиками (interceptors)
│   ├── mock.service.ts      — Мок-реализация (БД в памяти, фейковый JWT)
│   ├── auth.service.ts      — Реальные API-вызовы
│   └── index.ts             — Переключатель между мок/реальным по конфигу
├── context/AuthContext.tsx   — React Context для состояния авторизации
├── components/
│   ├── Navbar.tsx            — Навигационная панель с условными ссылками
│   ├── PrivateRoute.tsx      — Защита маршрута (редирект на /login)
│   └── LoadingSpinner.tsx    — Индикатор загрузки
├── pages/
│   ├── HomePage.tsx          — Главная страница с описанием JWT-потока
│   ├── RegisterPage.tsx      — Форма регистрации
│   ├── LoginPage.tsx         — Форма входа
│   └── DashboardPage.tsx     — Защищённая страница с инфо о пользователе и токене
├── styles/                   — CSS-файлы (переменные, формы, навбар и т.д.)
├── App.tsx                   — Настройка роутера + AuthProvider
├── App.css                   — Глобальные стили
└── main.tsx                  — Точка входа React
```

---

## Как это работает

### 1. Регистрация
- Пользователь заполняет имя + пароль на `/register`
- `AuthContext.register()` вызывает сервис
- Сервис отправляет `POST /api/register { username, password }`
- При успехе: показывает сообщение со ссылкой на вход

### 2. Вход (Логин)
- Пользователь вводит данные на `/login`
- `AuthContext.login()` вызывает сервис
- Сервис отправляет `POST /api/login { username, password }`
- Ответ: `{ access_token: "eyJ...", token_type: "bearer" }`
- Токен сохраняется в `localStorage`
- Токен декодируется для извлечения `username`, `user_id`, `exp`
- Пользователь перенаправляется на `/dashboard`

### 3. Защищённые маршруты
- `PrivateRoute` проверяет `isAuthenticated` из AuthContext
- Если нет валидного токена — редирект на `/login`
- Axios-перехватчик автоматически прикрепляет заголовок `Authorization: Bearer <токен>`

### 4. Выход (Логаут)
- Удаляет токен из `localStorage`
- Сбрасывает состояние AuthContext
- Перенаправляет на `/login`

### 5. Сохранение при обновлении страницы
- При монтировании `AuthContext` проверяет `localStorage` на наличие токена
- Если токен есть и не истёк — восстанавливает состояние пользователя
- Если токен истёк — удаляет его

---

## Утилиты для токена (src/utils/token.ts)

- `getToken()` — прочитать из localStorage
- `setToken(token)` — сохранить в localStorage
- `removeToken()` — удалить из localStorage
- `decodeToken(token)` — вручную декодировать payload JWT (base64url → JSON)
- `isTokenExpired(token)` — проверить поле `exp` относительно текущего времени

Внешние JWT-библиотеки не используются — цель в том, чтобы понять, что JWT это просто base64-кодированный JSON.

---

## Мок-режим

Когда `VITE_USE_MOCK=true`:
- Бэкенд не нужен
- БД пользователей в памяти (сбрасывается при обновлении страницы)
- Фейковые JWT-токены с правильной структурой (header.payload.signature)
- Имитация задержки 500мс
- Жёлтый баннер "МОК-РЕЖИМ" в навбаре

Для переключения на реальный API: установи `VITE_USE_MOCK=false` в `ForFRONTEND.env` и перезапусти дев-сервер.

---

## API-контракт

Твой бэкенд должен реализовать:

| Метод | Эндпоинт | Тело запроса | Ответ |
|-------|----------|-------------|-------|
| POST | `/api/register` | `{"username":"...","password":"..."}` | `{"username":"...","message":"..."}` |
| POST | `/api/login` | `{"username":"...","password":"..."}` | `{"access_token":"eyJ...","token_type":"bearer"}` |
| GET | `/api/profile` | — | `{"username":"...","user_id":1}` |

Эндпоинт `/api/profile` требует заголовок `Authorization: Bearer <токен>`.

Не забудь включить CORS для `http://localhost:5173` на бэкенде.

---

## Зависимости

| Пакет | Назначение |
|-------|-----------|
| `react`, `react-dom` | UI-библиотека |
| `react-router-dom` | Клиентский роутинг |
| `axios` | HTTP-клиент с поддержкой перехватчиков |
| `typescript` | Типобезопасность |
| `vite` | Сборщик и дев-сервер |
