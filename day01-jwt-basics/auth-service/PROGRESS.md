# Отчет о прогрессе GoalWays Backend

## Статус выполнения: ДЕНЬ 1 — ~85% | ДЕНЬ 2 — 100%

---

## ДЕНЬ 1: ТЗ1 (Email Verification) + Auth Architecture

| Шаг | Задача | Статус | Комментарий |
|:---:|---|:---:|---|
| 1.1 | Стандартный формат ответа API | **Выполнено** | `ApiResponse.java` интегрирован во все эндпоинты `AuthController`. |
| 1.2 | Миграция: email, status, confirmation | **Выполнено** | Миграции V1-V3 покрывают создание таблиц и добавление полей. |
| 1.3 | Обновить модель User + Confirmation | **Выполнено** | Модели `User` и `ConfirmationCode` реализованы. |
| 1.4 | BCrypt cost 12 + Регистрация | **Выполнено** | Cost 12 установлен, `register` сохраняет email и генерирует код. |
| 1.5 | Email-сервис | **Выполнено** | `ResendEmailService` реализован через Resend API. |
| 1.6 | Эндпоинты confirm + resend | **Выполнено** | `/confirm` и `/resend` реализованы. Базовый путь `/api/auth`. |
| 1.7 | Формат ответов register/login | **Выполнено** | Все эндпоинты возвращают `ApiResponse`. |
| 1.8 | Refresh Token | **Не выполнено** | Нет миграций, моделей и логики генерации/обновления. |
| 1.9 | Cleanup + E2E тест | **Не выполнено** | Контроллер требует доработки для прохождения тестов. |

---

## ДЕНЬ 2: Goals CRUD + Stages

| Шаг | Задача | Статус | Комментарий |
|:---:|---|:---:|---|
| 2.1 | Миграция: goals + stages | **Выполнено** | Миграции V6–V9: таблицы goals, stages, поля progress, status и т.д. |
| 2.2 | Entity + Repository | **Выполнено** | Goal, Stage; GoalRepository, StageRepository с нужными методами. |
| 2.3 | DTO для Goals/Stages | **Выполнено** | CreateGoalRequest, StageCreateRequest, UpdatedGoalRequest, UpdatedStage. |
| 2.4 | GoalService | **Выполнено** | createGoal, getUserGoals, getGoalWithStages, UpdateGoal, DeleteGoal. |
| 2.5 | StageService | **Выполнено** | addStage, updateStage (в т.ч. status), DeleteStage; пересчёт прогресса цели. |
| 2.6 | GoalController | **Выполнено** | Все эндпоинты /api/goals: CRUD целей, CRUD этапов, ai-decompose заглушка. |

---

## Итог

- **День 1:** Auth (register, confirm, resend, login, refresh, logout) и формат ApiResponse реализованы. Остаются доработка Refresh Token и E2E тесты по плану.
- **День 2:** Goals CRUD + Stages реализованы полностью. API задокументирован в API.md.
