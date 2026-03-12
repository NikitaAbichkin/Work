# Auth Service API

Base URL: `http://10.3.25.112:8080`

Все ответы приходят в формате:
```json
{ "status": "success", "data": { ... } }
{ "status": "error",   "error": "сообщение об ошибке" }
```

---

## POST /api/auth/register

Регистрация нового пользователя. На email придёт код подтверждения.

**Body:**
```json
{
  "username": "nikita",
  "password": "secret123",
  "email": "nikita@example.com"
}
```

**Ответ 200:**
```json
{
  "status": "success",
  "data": {
    "username": "nikita",
    "message": "Письмо с кодом подтверждения отправлено на email"
  }
}
```

---

## POST /api/auth/confirm

Подтверждение email по коду из письма.

**Body:**
```json
{
  "username": "nikita",
  "code": "4821"
}
```

**Ответ 200:**
```json
{
  "status": "success",
  "data": {
    "message": "user seccessfully confirmed"
  }
}
```

---

## POST /api/auth/resend

Повторная отправка кода подтверждения.

**Body:**
```json
{
  "email": "nikita@example.com"
}
```

**Ответ 200:**
```json
{
  "status": "success",
  "data": {
    "message": "code to nikita@example.com was seccessfully send"
  }
}
```

---

## POST /api/auth/login

Вход. Возвращает access и refresh токены.

> Аккаунт должен быть подтверждён через /confirm

**Body:**
```json
{
  "username": "nikita",
  "password": "secret123"
}
```

**Ответ 200:**
```json
{
  "status": "success",
  "data": {
    "access_token": "eyJhbGci...",
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
    "token_type": "bearer"
  }
}
```

---

## POST /api/auth/refresh

Обновление access токена по refresh токену (ротация — старый refresh токен инвалидируется).

**Body:**
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Ответ 200:**
```json
{
  "status": "success",
  "data": {
    "access_token": "eyJhbGci...",
    "refreshToken": "новый-refresh-токен",
    "token_type": "bearer"
  }
}
```

---

## POST /api/auth/logout

Выход — инвалидирует refresh токен.

**Body:**
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Ответ 200:**
```json
{
  "status": "success",
  "data": null
}
```

---

## Защищённые эндпоинты

Для запросов к защищённым эндпоинтам передавай заголовок:

```
Authorization: Bearer eyJhbGci...
```

Access токен живёт **24 часа**. После истечения используй `/refresh`.

---

## API целей и этапов (Goals + Stages)

Базовый путь: **`/api/goals`**. Все запросы требуют заголовок `Authorization: Bearer <access_token>`.

### POST /api/goals

Создать цель. Можно передать список этапов в теле (или пустой массив).

**Body:**
```json
{
  "title": "Выучить английский",
  "description": "Дойти до B2 за год",
  "stages": [
    {
      "title": "Купить учебник",
      "description": null,
      "priority": "HIGH",
      "estimatedTime": "1 день",
      "deadline": null,
      "startsAt": null,
      "sortOrder": 0
    }
  ]
}
```

**Ответ 201:** в `data` — объект цели (Goal) с полями id, title, description, status, progress, stages и т.д.

---

### GET /api/goals

Список целей текущего пользователя с пагинацией.

**Query:** `page` (по умолчанию 0), `size` (по умолчанию 20).

**Пример:** `GET /api/goals?page=0&size=10`

**Ответ 200:** в `data` — объект страницы Spring (content, totalElements, totalPages, number, size и т.д.).

---

### GET /api/goals/{id}

Одна цель по id со всеми этапами (для детального просмотра).

**Ответ 200:** в `data` — объект Goal с заполненным списком stages.

---

### PUT /api/goals

Обновить цель. В теле обязателен `goalId`, остальные поля — по необходимости.

**Body:**
```json
{
  "goalId": 1,
  "title": "Новое название",
  "description": "Новое описание",
  "stages": null
}
```

**Ответ 200:** в `data` — обновлённая цель.

---

### DELETE /api/goals/{id}

Удалить цель по id.

**Ответ 200:** в `data` — строка с сообщением об удалении.

---

### POST /api/goals/{goalId}/stages

Добавить этап к цели.

**Body:**
```json
{
  "title": "Этап 1",
  "description": "Описание",
  "priority": "MEDIUM",
  "estimatedTime": "2 часа",
  "deadline": null,
  "startsAt": null,
  "sortOrder": 0
}
```

**Ответ 201:** в `data` — созданный этап (Stage).

---

### PUT /api/goals/{goalId}/stages/{stageId}

Обновить этап. goalId и stageId можно передать в пути; в теле — только поля, которые меняем (частичное обновление). Статус этапа: `IN_PROGRESS`, `FROZEN`, `COMPLETED`.

**Body (пример — смена статуса):**
```json
{
  "status": "COMPLETED"
}
```

**Ответ 200:** в `data` — обновлённый этап. Прогресс цели пересчитывается автоматически.

---

### DELETE /api/goals/{goalId}/stages/{stageId}

Удалить этап у цели.

**Ответ 200:** в `data` — строка с сообщением об удалении.

---

### POST /api/goals/{id}/ai-decompose

Заглушка: разложение цели на этапы через ИИ. Пока всегда возвращает 202.

**Ответ 202:** в `data` — `{ "message": "processing" }`.