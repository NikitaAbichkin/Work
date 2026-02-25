# JWT Auth — Документация

**Стек:** Spring Boot + Spring Security + PostgreSQL + Flyway

REST API с двумя эндпоинтами: регистрация и логин. Доступ к остальным эндпоинтам — только по JWT токену.

---

## Структура

```
controller/   AuthController    — принимает HTTP запросы
service/      AuthService       — бизнес-логика (регистрация, логин)
              JwtService        — генерация и валидация JWT
filter/       JwtAuthFilter     — проверяет токен на каждый запрос
config/       SecurityConfig    — настройки Spring Security
model/        User              — сущность (таблица users)
repository/   UserRepository    — запросы к БД
dto/          LoginRequest      — входящий JSON для логина
              RegisterRequest   — входящий JSON для регистрации
              TokenResponse     — исходящий JSON с токеном
```



```
model/ User.java ( сущность юзера )
filter/  JwtAuthFilter ( класс который перехватывает все запросы и перед тем как они 
```

## Эндпоинты

### POST `/api/register`
```json
// запрос
{ "username": "vasya", "password": "12345" }

// ответ
{ "username": "vasya", "message": "User registered successfully" }
```

### POST `/api/login`
```json
// запрос
{ "username": "vasya", "password": "12345" }

// ответ
{ "access_token": "eyJhbGci...", "token_type": "bearer" }
```

### Защищённые эндпоинты
Все остальные пути требуют заголовок:
```
Authorization: Bearer <токен>
```

---

## Зачем столько классов

Каждый класс делает одно конкретное дело — чтобы не было одного монстр-класса на 500 строк.

| Задача | Класс |
|--------|-------|
| Принять запрос от клиента | `AuthController` — просто дверь. Получил JSON, передал дальше. |
| Сделать логику | `AuthService` — мозг. Проверяет пароли, регистрирует, логинит. |
| Работать с токенами | `JwtService` — отдельный спец только по JWT. Создать / прочитать / проверить токен. |
| Проверять токен на каждый запрос | `JwtAuthFilter` — охранник на входе. Каждый запрос через него проходит. |
| Сказать Spring кто пускается без токена | `SecurityConfig` — настройка правил. "Вот эти два пути открыты, остальное — только с токеном". |
| Работать с БД | `UserRepository` — запросы к базе. Найти юзера, сохранить юзера. |
| Описать что такое юзер | `User` — просто говорит: у юзера есть id, username, пароль. |
| Описать форму JSON | `LoginRequest`, `RegisterRequest`, `TokenResponse` — классы-контейнеры для JSON. Пришёл JSON → распаковали в объект. Отправить JSON → упаковали из объекта. |

---

## База данных

Таблица `users`: `id` (PK), `username` (UNIQUE), `hashed_password`

```
jdbc:postgresql://localhost:5432/jwt_auth
```

---

## Поток авторизации

```
register  →  пароль хешируется  →  юзер в БД
login     →  пароль проверяется →  JWT токен
запрос    →  JwtAuthFilter проверяет токен  →  доступ разрешён/запрещён
```

---

## Конфигурация

```yaml
jwt:
  secret: some-super-secret-key-that-is-long-enough-for-hmac
  expiration: 86400000   # 24 часа
```