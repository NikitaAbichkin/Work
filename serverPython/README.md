# Python Server

Асинхронный сервер для системы заказов еды с двумя реализациями:
1. TCP-сервер (asyncio) - `main.py`
2. REST API (FastAPI) - `server.py`

## Установка зависимостей

```bash
# Создать виртуальное окружение
python3 -m venv venv

# Активировать
source venv/bin/activate

# Установить зависимости
pip install -r requirements.txt
```

## Запуск

### REST API сервер (FastAPI):
```bash
source venv/bin/activate
uvicorn server:app --host 0.0.0.0 --port 4004 --reload
```

**Интерактивная документация:** http://localhost:4004/docs

### TCP сервер (asyncio):
```bash
python main.py
```

**Клиент:**
```bash
python Client.py
```

По умолчанию сервер запускается на `0.0.0.0:4004`

## REST API Endpoints (FastAPI)

### Получить меню
```bash
curl http://localhost:4004/menu
```

### Добавить блюдо
```bash
curl -X POST "http://localhost:4004/menu" \
  -H "Content-Type: application/json" \
  -d '{"id": "Суп3", "title": "Борщ", "cost": 250}'
```

### Обновить блюдо
```bash
curl -X PUT "http://localhost:4004/menu" \
  -H "Content-Type: application/json" \
  -d '{"id": "Суп3", "title": "Борщ острый", "cost": 300}'
```

### Удалить блюдо
```bash
curl -X DELETE "http://localhost:4004/menu/Суп3"
```

## TCP сервер - Команды

| Команда | Описание |
|---------|----------|
| `menu` | Показать меню блюд |
| `orders` | Показать список заказов |
| `add_meal_to_menu` | Добавить блюдо в меню |
| `update_meal_from_menu` | Обновить блюдо по id |
| `delete_meal_from_menu` | Удалить блюдо по id |
| `exit` | Отключиться от сервера |
| `stop` | Остановить сервер |
| `help` | Показать список всех команд |

## Структура

- `main.py` — TCP-сервер (asyncio)
- `Client.py` — клиент для подключения
- `meal.py` — класс блюда (id, title, cost)
- `order.py` — класс заказа (id, date, meals)
- `file_helper.py` — работа с JSON-файлами
- `Menu1.json` — данные меню
- `Orders1.json` — данные заказов