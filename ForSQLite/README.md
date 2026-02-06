# SQLite + Python — Простой пример

## Структура

```
ForSQLite/
├── main.py         # Запуск и демонстрация
├── database.py     # Подключение к SQLite
├── student.py      # Модель студента
├── students_db.py  # CRUD-операции
└── students.db     # Файл БД (создаётся при запуске)
```

## Как запустить

```bash
cd /Users/macbookairm313/Documents/ForSQLite
python3 main.py
```

## Что происходит при запуске

1. Создаётся файл `students.db` (база данных)
2. В ней создаётся таблица `students`
3. Добавляются 3 студента (INSERT)
4. Выводятся все студенты (SELECT)
5. Один студент переводится в другую группу (UPDATE)
6. Один студент удаляется (DELETE)

## Как устроен код

### `database.py` — работа с SQLite

Три метода — больше ничего не нужно:

| Метод | Для чего | Когда использовать |
|-------|----------|--------------------|
| `run(sql, params)` | Выполнить запрос | INSERT, UPDATE, DELETE, CREATE |
| `fetch_one(sql, params)` | Получить одну строку | SELECT ... WHERE id = ? |
| `fetch_all(sql, params)` | Получить все строки | SELECT ... (список) |

### `student.py` — модель данных

Просто класс с полями: `id`, `name`, `birthday`, `group`.
Метод `__str__` позволяет делать `print(student)` напрямую.

### `students_db.py` — все SQL-запросы

| Метод | SQL | Что делает |
|-------|-----|------------|
| `create_table()` | `CREATE TABLE` | Создаёт таблицу |
| `add()` | `INSERT INTO` | Добавляет студента |
| `get_by_id()` | `SELECT ... WHERE id=?` | Находит по ID |
| `get_all()` | `SELECT ...` | Все студенты |
| `get_by_group()` | `SELECT ... WHERE grp=?` | По группе |
| `update()` | `UPDATE ... WHERE id=?` | Обновляет данные |
| `delete()` | `DELETE ... WHERE id=?` | Удаляет студента |

