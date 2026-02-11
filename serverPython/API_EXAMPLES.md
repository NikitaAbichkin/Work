# Примеры запросов для проверки API

## Быстрый старт

1. Запустите сервер:
```bash
source venv/bin/activate
uvicorn server:app --host 0.0.0.0 --port 4004 --reload
```

2. Откройте в браузере: **http://localhost:4004/docs**
   - Там можно протестировать все endpoints прямо в браузере!

## Примеры curl команд

### 1. Получить меню
```bash
curl http://localhost:4004/menu
```

**Ожидаемый результат:** JSON список всех блюд

---

### 2. Добавить новое блюдо
```bash
curl -X POST "http://localhost:4004/menu" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "Десерт1",
    "title": "Тирамису",
    "cost": 350
  }'
```

**Ожидаемый результат:** Обновленное меню с новым блюдом

---

### 3. Обновить существующее блюдо
```bash
curl -X PUT "http://localhost:4004/menu" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "Десерт1",
    "title": "Тирамису классический",
    "cost": 400
  }'
```

**Ожидаемый результат:** Меню с обновленным блюдом

---

### 4. Удалить блюдо
```bash
curl -X DELETE "http://localhost:4004/menu/Десерт1"
```

**Ожидаемый результат:** Меню без удаленного блюда

---

## Тестовые данные

### Примеры блюд для добавления:

```json
{
  "id": "Суп1",
  "title": "Борщ",
  "cost": 200
}
```

```json
{
  "id": "Гарнир1",
  "title": "Картофельное пюре",
  "cost": 150
}
```

```json
{
  "id": "Напиток1",
  "title": "Компот",
  "cost": 50
}
```

## Проверка через Python

```python
import requests

# Получить меню
response = requests.get("http://localhost:4004/menu")
print(response.json())

# Добавить блюдо
new_meal = {"id": "Тест1", "title": "Тестовое блюдо", "cost": 100}
response = requests.post("http://localhost:4004/menu", json=new_meal)
print(response.json())
```

## Проверка через JavaScript/Fetch

```javascript
// Получить меню
fetch('http://localhost:4004/menu')
  .then(res => res.json())
  .then(data => console.log(data));

// Добавить блюдо
fetch('http://localhost:4004/menu', {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({
    id: "Тест1",
    title: "Тестовое блюдо",
    cost: 100
  })
})
  .then(res => res.json())
  .then(data => console.log(data));
```
