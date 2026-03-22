# Architecture Context

Этот файл описывает текущее состояние проекта `auth-service` и должен быть основной точкой входа для будущего анализа. Если информация в старых `.md`-файлах расходится с этим документом, ориентироваться нужно на этот файл и на код.

## 1. Что это за проект

`auth-service` — Spring Boot backend для:

- регистрации и логина пользователей;
- подтверждения email через 4-значный код;
- JWT access token + refresh token;
- CRUD целей (`goals`);
- CRUD задач внутри целей (`stages`, но в API они называются `tasks`);
- CRUD результатов цели (`results`) как отдельной сущности;
- AI-помощи для генерации и изменения плана цели.

Текущая версия API живет под префиксами:

- `/api/v1/auth`
- `/api/v1/goals`

## 2. Стек и инфраструктура

- Java 21
- Spring Boot 3.5.x
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- JJWT
- Resend API для писем
- Внешний AI API через `RestClient`

Ключевые runtime-файлы:

- `src/main/resources/application.yml`
- `docker-compose.yml`
- `Dockerfile`
- `pom.xml`

## 3. Главная архитектурная идея

Проект не строится вокруг полноценной сложной security-модели с ролями и кастомным `UserDetailsService`.

Текущая схема проще:

1. `JwtAuthFilter` проверяет Bearer token.
2. Если токен валиден, в `SecurityContext` кладется username.
3. Сервисы для доступа к данным сами извлекают `user_id` из JWT через `JwtService.extractId(...)`.
4. Практически все ownership-проверки происходят на уровне service-слоя через `userId` из токена.

Это важная особенность проекта: основной источник истины для текущего пользователя не Spring principal, а `user_id` claim в JWT.

## 4. Текущая доменная модель

### User

Таблица: `users`

Поля:

- `id`
- `username`
- `hashed_password`
- `email`
- `status`

Статус по умолчанию: `NOT_CONFIRMED`

После подтверждения email статус меняется на `ACTIVE`.

### ConfirmationCode

Таблица: `confirmation_codes`

Используется для:

- подтверждения регистрации;
- сброса пароля.

Особенности:

- код 4-значный;
- срок жизни 5 минут;
- хранит `createdAt` и `expiresAt`;
- привязан к `User`.

### RefreshToken

Таблица: `refresh_tokens`

Особенности:

- refresh token хранится в БД;
- генерируется как UUID-строка;
- срок жизни сейчас 7 дней;
- привязан к `User`;
- при refresh старый токен удаляется, новый создается заново.

### Goal

Таблица: `goals`

Goal принадлежит конкретному пользователю.

Ключевые поля:

- `title`
- `description`
- `priority`
- `start_date`
- `deadline`
- `daily_time_minutes`
- `status`
- `progress`
- `created_at`
- `updated_at`

Enums:

- `GoalStatus`: `IN_PROGRESS`, `COMPLETED`, `FROZEN`, `ARCHIVED`
- `PriorityStatus`: `LOW`, `MEDIUM`, `HIGH`

Связи:

- `ManyToOne` -> `User`
- `OneToMany` -> `Stage`
- `OneToMany` -> `Result`

Логика прогресса:

- `Goal.recalculateProgress()` считает средний `progress` по всем `stages`;
- если задач нет, прогресс = `0`.

### Stage

Таблица: `stages`

В коде сущность называется `Stage`, но в API это в основном `tasks`.

Ключевые поля:

- `title`
- `description`
- `deadline`
- `starts_at`
- `estimated_minutes`
- `priority`
- `progress`
- `status`
- `is_completed`
- `sort_order`
- `created_at`
- `updated_at`
- `completed_at`
- `result_text`

Дополнительно:

- картинки результата stage хранятся в `stage_result_images` через `@ElementCollection`.

Enums:

- `PriorityStage`: `LOW`, `MEDIUM`, `HIGH`
- `StatusPriority`: `IN_PROGRESS`, `COMPLETED`, `FROZEN`, `ARCHIVED`

Поведение:

- если `progress = 100`, stage считается завершенным;
- если статус ставится в `COMPLETED`, прогресс автоматически становится `100`;
- если статус меняется с `COMPLETED` на любой другой, прогресс сбрасывается в `0`.

### Result

Таблица: `results`

Это важно: в текущем проекте `Result` реализован как отдельная сущность, а не как поле внутри `Stage`.

Result принадлежит `Goal`, а не `Stage`.

Поля:

- `description`
- `created_at`
- `images`

Картинки результата хранятся в таблице `result_images` через `@ElementCollection`.

## 5. Основные фичи и как они работают

### 5.1 Auth flow

#### Регистрация

`POST /api/v1/auth/register`

Логика:

1. Проверка уникальности email.
2. Проверка уникальности username.
3. Хэширование пароля через BCrypt с cost 12.
4. Создание `User` со статусом `NOT_CONFIRMED`.
5. Генерация 4-значного `ConfirmationCode`.
6. Отправка кода на email через `ResendEmailService`.

#### Подтверждение email

`POST /api/v1/auth/confirm`

Логика:

1. Ищется пользователь по username.
2. Ищется код подтверждения.
3. Проверяется срок действия.
4. Пользователь переводится в `ACTIVE`.
5. Код удаляется.

#### Повторная отправка кода

`POST /api/v1/auth/resend`

Особенности:

- если пользователь уже `ACTIVE`, запрос падает;
- есть cooldown примерно 1 минута по последнему коду;
- старые коды удаляются, создается новый.

#### Логин

`POST /api/v1/auth/login`

Логика:

1. Ищется пользователь по username.
2. Если статус `NOT_CONFIRMED`, вход запрещен.
3. Проверяется пароль.
4. Генерируется JWT access token.
5. Генерируется refresh token.
6. Refresh token сохраняется в БД.
7. Возвращается `TokenResponse`.

#### Refresh

`POST /api/v1/auth/refresh`

Логика:

1. Ищется refresh token в БД.
2. Проверяется срок действия.
3. Старый refresh token удаляется.
4. Создается новый refresh token.
5. Генерируется новый access token.

#### Logout

`POST /api/v1/auth/logout`

Удаляет один refresh token.

#### Дополнительные auth-эндпоинты

Также реализованы:

- `POST /api/v1/auth/sendPasswordResetCode`
- `POST /api/v1/auth/verifyCode`
- `POST /api/v1/auth/deleteUser`
- `POST /api/v1/auth/logoutFromEverySession`
- `POST /api/v1/auth/seeAllSessions`

Сброс пароля повторно использует `confirmation_codes`.

### 5.2 Goals flow

#### Создание цели

`POST /api/v1/goals`

Особенности:

- goal создается для пользователя из JWT;
- title обязателен;
- priority валидируется;
- `start_date` обязателен;
- проверяется, что `start_date <= deadline`;
- `daily_time_minutes` не может быть отрицательным;
- возможна передача списка `stages` сразу при создании;
- status по умолчанию ставится в `IN_PROGRESS`;
- progress по умолчанию ставится в `0`.

#### Получение целей

`GET /api/v1/goals`

Используется `ParametersForSearching`:

- пагинация;
- фильтр по `status`;
- фильтр по `priority`;
- сортировка;
- порядок сортировки.

Дополнительно в контроллере есть еще один маршрут с тем же смыслом:

- `GET /api/v1/goals/goals`

Его стоит воспринимать как дублирующий search/list endpoint.

#### Получение одной цели

`GET /api/v1/goals/{id}`

Goal загружается вместе с `stages`.

#### Обновление цели

`PUT /api/v1/goals/{id}`

Важно:

- фактически это partial update;
- проект сознательно использует `PUT`, но поведение ближе к `PATCH`;
- меняются только поля, которые пришли не `null`.

#### Удаление цели

`DELETE /api/v1/goals/{id}`

Удаление каскадно тянет `stages` и `results` через JPA-связи.

### 5.3 Stages / Tasks flow

#### Создание задачи

`POST /api/v1/goals/{goalId}/tasks`

Создает stage внутри goal, затем пересчитывает прогресс goal.

#### Обновление задачи

`PATCH /api/v1/goals/{goalId}/tasks/{stageId}`

Обновляет только переданные поля.

Поддерживаются:

- title
- description
- priority
- estimatedMinutes
- deadline
- startsAt
- progress
- status

После обновления stage прогресс goal пересчитывается.

#### Удаление задачи

`DELETE /api/v1/goals/{goalId}/tasks/{stageId}`

Удаляет stage через `goal.getStages().remove(stage)` и пересчитывает прогресс goal.

#### Список задач цели

`GET /api/v1/goals/{goalId}/stages`

Пагинация идет через DTO `Allstages`.

#### Одна задача

`GET /api/v1/goals/{goalId}/tasks/{stageId}`

Сервис дополнительно проверяет, что stage принадлежит текущему пользователю.

### 5.4 Results flow

#### Что важно понять

Results в текущем проекте привязаны к `Goal`, а не к `Stage`.

Эндпоинты:

- `GET /api/v1/goals/{goalId}/results`
- `POST /api/v1/goals/{goalId}/results`
- `PATCH /api/v1/goals/{goalId}/results/{resultId}`
- `DELETE /api/v1/goals/{goalId}/results/{resultId}`

Правила:

- у результата должно быть описание или хотя бы одна картинка;
- доступ идет только через goal текущего пользователя.

### 5.5 AI flow

AI-часть сейчас не сохраняет данные автоматически в БД.

Она генерирует DTO-ответ, который дальше может использовать клиент.

Эндпоинты:

- `POST /api/v1/goals/ai-decompose`
- `POST /api/v1/goals/ai-help`

#### `ai-decompose`

Принимает свободный prompt и возвращает структуру будущей цели:

- title
- description
- priority
- start_date
- deadline
- daily_time_minutes
- stages[]

#### `ai-help`

Принимает:

- prompt
- `goalId`

Дополнительно сериализует текущую goal в JSON и передает ее в system prompt, чтобы AI мог предложить изменения на основе существующей цели.

#### Технически

- используется `RestClient`;
- модель, ключ, timeout и URL берутся из `application.yml`;
- ответ парсится вручную через `ObjectMapper`;
- из ответа вырезаются markdown fences ```json ... ```, если они пришли.

## 6. Security

Файл: `src/main/java/com/example/auth/config/SecurityConfig.java`

Текущее поведение:

- CSRF отключен;
- включен CORS;
- `/api/v1/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**` открыты;
- все остальные роуты требуют authentication;
- `JwtAuthFilter` стоит перед `UsernamePasswordAuthenticationFilter`;
- session policy = `STATELESS`.

Особенность:

- CORS сейчас разрешен очень широко через `allowedOriginPatterns("*")`.

## 7. Формат ответов и ошибки

Проект старается возвращать ответы в обертке `ApiResponse`.

Глобальная обработка ошибок находится в:

- `src/main/java/com/example/auth/exception/GlobalExceptionHandler.java`

Обрабатываются, в частности:

- invalid credentials;
- inactive account;
- invalid token;
- invalid refresh token;
- user not found;
- goal not found;
- stage not found;
- invalid arguments;
- validation errors;
- общий `RuntimeException`.

## 8. База данных и миграции

Основные миграции:

- `V1` — users
- `V2` — confirmation_codes
- `V3` — email/status у users
- `V4` — refresh_tokens
- `V5` — identity для auth-таблиц
- `V6` — goals + stages
- `V7` — промежуточное обновление схемы goals/stages
- `V8` — дополнительные поля goals/stages + `stage_result_images`
- `V9` — переводы дат и нормализация enum-значений
- `V10` — `estimated_time -> estimated_minutes`, `updated_at` у stages
- `V11` — `results` и `result_images`

Текущая фактическая схема важнее старых плановых документов.

## 9. Ключевые файлы по слоям

### Entry point

- `src/main/java/com/example/auth/AuthApplication.java`

### Config / security

- `src/main/java/com/example/auth/config/SecurityConfig.java`
- `src/main/java/com/example/auth/filter/JwtAuthFilter.java`
- `src/main/java/com/example/auth/service/JwtService.java`

### Controllers

- `src/main/java/com/example/auth/controller/AuthController.java`
- `src/main/java/com/example/auth/controller/GoalController.java`

### Services

- `src/main/java/com/example/auth/service/AuthService.java`
- `src/main/java/com/example/auth/service/GoalService.java`
- `src/main/java/com/example/auth/service/StageService.java`
- `src/main/java/com/example/auth/service/ResultService.java`
- `src/main/java/com/example/auth/service/AiPlanService.java`
- `src/main/java/com/example/auth/service/ResendEmailService.java`

### Models

- `src/main/java/com/example/auth/model/User.java`
- `src/main/java/com/example/auth/model/ConfirmationCode.java`
- `src/main/java/com/example/auth/model/RefreshToken.java`
- `src/main/java/com/example/auth/model/Goal.java`
- `src/main/java/com/example/auth/model/Stage.java`
- `src/main/java/com/example/auth/model/Result.java`

### Repositories

- `UserRepository`
- `ConfirmationCodeRepository`
- `RefreshTokenRepository`
- `GoalRepository`
- `StageRepository`
- `ResultRepository`

## 10. Важные проектные договоренности

Это важно сохранить в голове при будущих правках:

- текущий API надо считать `v1`;
- `Result` как отдельная сущность — это текущая и нормальная архитектура проекта;
- `PUT /goals/{id}` сейчас намеренно используется как partial update;
- stages в коде называются `Stage`, но наружу в API чаще подаются как `tasks`;
- ownership почти везде проверяется через `user_id` из JWT;
- AI-эндпоинты пока возвращают результат генерации, но не сохраняют его автоматически;
- этот документ должен быть важнее старых заметок и handover-файлов.

## 11. Что игнорировать при будущем анализе

С высокой вероятностью не надо опираться как на источник истины на следующие документы, если они противоречат коду:

- `PROGRESS.md`
- `API.md`
- `План по шагам .md`
- `Архитекутра и  тз.md`
- `Documents/PROJECT_HANDOVER_CONTEXT.md`
- `Documents/финальный первый день.md`

`эндпоинты.md` ближе к реальному API, но все равно окончательным источником истины остаются контроллеры и сервисы.

## 12. Что еще стоит помнить

- В `application.yml` сейчас лежат реальные ключи/секреты. Для продового режима это надо будет вынести в env, но как описание текущего состояния это важно.
- `CleanupService.java` существует, но сейчас не является нормальным активным Spring bean и на него не стоит рассчитывать как на работающий runtime-механизм очистки.
- Тестовое покрытие пока ограниченное: есть `AuthControllerTest`, но проект далеко не полностью покрыт тестами.

## 13. Как использовать этот файл дальше

Если в будущем нужно быстро понять проект:

1. Сначала читать этот файл.
2. Потом, если нужно, идти только в конкретный слой:
   - controller для маршрутов;
   - service для бизнес-логики;
   - model/repository для данных;
   - migration для структуры БД.
3. Старые markdown-файлы использовать только как исторический контекст, а не как источник истины.
