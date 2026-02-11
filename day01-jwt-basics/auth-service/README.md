# JWT Basics — Full-Stack аутентификация

## Описание проекта

Учебный проект, демонстрирующий работу JWT (JSON Web Token) аутентификации.

- **Бэкенд:** Python, FastAPI, SQLAlchemy, PostgreSQL, Argon2, PyJWT
- **Фронтенд:** React, TypeScript, Vite, Axios

Реализованы три основных процесса:
- **Регистрация** — хеширование пароля алгоритмом Argon2 и сохранение в PostgreSQL
- **Логин** — проверка пароля и выдача JWT токена
- **Защищённый запрос** — валидация токена и предоставление доступа к данным

---

## Структура бэкенда

| Файл | Роль |
|---|---|
| `config.py` | Конфигурация — читает переменные из `.env` |
| `database.py` | Подключение к PostgreSQL через SQLAlchemy |
| `models.py` | Модель таблицы `users` в БД |
| `schemas.py` | Pydantic-схемы для валидации запросов/ответов |
| `password_utils.py` | Хеширование и проверка паролей (Argon2) |
| `jwt_utils.py` | Создание и проверка JWT токенов |
| `routes.py` | API эндпоинты FastAPI (регистрация, логин, профиль) |
| `jwt_basics.py` | Обучающий скрипт — симуляция всего flow без сервера |

---

## Что делает каждый файл

### config.py — Конфигурация

Использует `pydantic-settings` для загрузки переменных окружения из `.env`:

- `DATABASE_URL` — строка подключения к PostgreSQL
- `JWT_SECRET_KEY` — секретный ключ для подписи токенов
- `JWT_ALGORITHM` — алгоритм подписи (по умолчанию HS256)
- `ACCESS_TOKEN_EXPIRE_MINUTES` — время жизни токена в минутах

Объект `settings` создаётся один раз при импорте и используется во всех остальных модулях.

### database.py — Подключение к БД

- `engine` — движок SQLAlchemy, устанавливает соединение с PostgreSQL
- `Sessionlocal` — фабрика сессий. Каждая сессия = одно соединение с БД
- `Base` — базовый класс для ORM-моделей (от него наследуются таблицы)
- `get_db()` — генератор-зависимость для FastAPI:
  - Создаёт сессию БД
  - Отдаёт её в обработчик роута через `Depends(get_db)`
  - После завершения запроса — закрывает сессию в блоке `finally`

### models.py — Модель таблицы

Класс `User` описывает таблицу `users` в PostgreSQL:

| Поле | Тип | Описание |
|---|---|---|
| `id` | int | Первичный ключ, автоинкремент |
| `username` | str(50) | Уникальное имя, с индексом для быстрого поиска |
| `hashed_password` | str(5000) | Хеш пароля (не сам пароль!) |
| `created_at` | datetime | Дата создания, автоматически UTC |

### schemas.py — Pydantic-схемы

Описывают структуру данных для запросов и ответов API:

- `UserCreate` — тело запроса на регистрацию (`username`, `password`)
- `UserLogin` — тело запроса на логин (`username`, `password`)
- `UserResponse` — ответ при регистрации (`username`, `message`)
- `TokenResponse` — ответ при логине (`access_token`, `token_type`)
- `ProfileResponse` — ответ профиля (`username`, `user_id`)

> Примечание: в текущем коде `routes.py` принимает `data: dict` вместо этих схем. Схемы подготовлены, но пока не подключены к роутам.

### password_utils.py — Хеширование паролей

Использует **Argon2** — один из самых стойких алгоритмов хеширования:

- `hash_password(password)` — принимает пароль строкой, возвращает хеш
- `verify_password(password, hashed_password)` — сравнивает введённый пароль с хешем из БД. Возвращает `True` / `False`

Пароль **никогда** не хранится в открытом виде. При логине сравнивается хеш от введённого пароля с хешем из БД.

### jwt_utils.py — Работа с JWT токенами

- `create_token(data, expires_delta)`:
  1. Копирует словарь с данными (например `{"username": "nikita", "user_id": 1}`)
  2. Добавляет `exp` (время истечения) и `iat` (время создания)
  3. Подписывает всё секретным ключом через `jwt.encode()` → возвращает строку-токен

- `verify_token(token)`:
  1. `jwt.decode()` проверяет подпись и срок действия
  2. Если всё ок — возвращает payload (данные из токена)
  3. `ExpiredSignatureError` → токен истёк → `None`
  4. `InvalidTokenError` → подпись невалидна → `None`

### routes.py — API эндпоинты

FastAPI-приложение с тремя роутами на префиксе `/api`:

**`POST /api/register`** — Регистрация:
1. Принимает `username` + `password`
2. Хеширует пароль через `password_utils.hash_password()`
3. Проверяет — нет ли уже такого юзера в БД
4. Если нет — создаёт объект `User`, сохраняет в БД (`db.add` → `db.commit`)
5. `db.refresh()` — подтягивает из БД присвоенный `id`
6. Возвращает `{"username": "...", "message": "User registered successfully"}`

**`POST /api/login`** — Вход:
1. Принимает `username` + `password`
2. Ищет юзера в БД по `username`
3. Сверяет введённый пароль с хешем через `verify_password()`
4. Если всё ок — создаёт JWT токен на **3 минуты** с данными `{username, user_id}`
5. Возвращает `{"access_token": "...", "token_type": "bearer"}`

**`GET /api/profile`** — Защищённый эндпоинт:
1. Читает заголовок `Authorization` из HTTP-запроса
2. Убирает префикс `"Bearer "` — остаётся чистый токен
3. Проверяет токен через `verify_token()`
4. Если валидный — возвращает `{username, user_id}` из payload
5. Если нет — возвращает ошибку

Также настроен **CORS** — разрешает запросы с `http://localhost:5173` (фронтенд на Vite).

### jwt_basics.py — Обучающий скрипт

Скрипт-симуляция, демонстрирующий весь flow **без FastAPI и БД**:
- `simulate_registration()` — хеширует пароль, возвращает словарь
- `simulate_login()` — проверяет пароль → создаёт токен
- `simulate_protected_request()` — проверяет токен → разрешает/запрещает доступ

---

## Схема вызовов (API)

```
Клиент (React)                         Сервер (FastAPI)
─────────────                           ────────────────
POST /api/register                      routes.py → registration()
  {username, password}         →          → password_utils.hash_password()
                                          → db.add(User) → db.commit()
                               ←        {username, message}

POST /api/login                         routes.py → login()
  {username, password}         →          → db.query(User)
                                          → password_utils.verify_password()
                                          → jwt_utils.create_token()
                               ←        {access_token, token_type}

GET /api/profile                        routes.py → profile()
  Header: Authorization: Bearer eyJ...     → jwt_utils.verify_token()
                               ←        {username, user_id}
```

---

## Схема вызовов (скрипт jwt_basics.py)

```
jwt_basics.py
│
├── simulate_registration("nikita", "123")
│   └── password_utils.hash_password("123")          → хеш
│
├── simulate_login("nikita", "123", хеш)
│   ├── password_utils.verify_password("123", хеш)   → True/False
│   └── jwt_utils.create_token({username, user_id})   → токен
│
└── simulate_protected_request(токен)
    └── jwt_utils.verify_token(токен)                 → payload или None
```

---

## Полный флоу аутентификации

```
1. РЕГИСТРАЦИЯ
   Клиент отправляет: {username: "nikita", password: "123"}
   Сервер:
     "123" → Argon2 hash → "$argon2id$..." → сохранение в PostgreSQL
   Ответ: {username: "nikita", message: "User registered successfully"}

2. ЛОГИН
   Клиент отправляет: {username: "nikita", password: "123"}
   Сервер:
     Ищет юзера в БД → берёт хеш → verify_password("123", хеш) → True
     → create_token({username: "nikita", user_id: 1}) → "eyJhbGci..."
   Ответ: {access_token: "eyJhbGci...", token_type: "bearer"}

3. ЗАЩИЩЁННЫЙ ЗАПРОС
   Клиент отправляет: GET /api/profile + заголовок Authorization: Bearer eyJhbGci...
   Сервер:
     "eyJhbGci..." → verify_token → {username: "nikita", user_id: 1, exp: ...}
     → Подпись верна, токен не истёк → доступ разрешён
   Ответ: {username: "nikita", user_id: 1}
```

---

## Ключевой принцип

После логина сервер **не хранит сессию**. Вся информация о пользователе зашита внутри JWT токена. При каждом запросе сервер просто проверяет подпись — если она валидна и токен не истёк, значит пользователь авторизован.
