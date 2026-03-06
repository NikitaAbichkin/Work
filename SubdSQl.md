-- 1. Удаление старых таблиц, если они уже существуют (чтобы скрипт можно было запускать заново)
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- 2. Создание таблицы пользователей
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Создание таблицы заказов (с колонкой cost вместо amount)
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    cost DECIMAL(10, 2) NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Генерация 100 000 тестовых пользователей
INSERT INTO users (username, email)
SELECT 
    'user_' || i, 
    'user_' || i || '@example.com'
FROM generate_series(1, 100000) AS i;

-- 5. Генерация 300 000 тестовых заказов
INSERT INTO orders (user_id, cost)
SELECT 
    floor(random() * 100000 + 1)::int, 
    round((random() * 1000 + 10)::numeric, 2) 
FROM generate_series(1, 300000);

-- 6. Создание индексов
-- Индекс для поиска пользователей по имени
CREATE INDEX idx_users_username ON users(username);

-- Индекс для сортировки заказов по дате (от новых к старым)
CREATE INDEX idx_orders_order_date ON orders(order_date DESC);

-- Составной индекс на user_id и cost. 
-- Он отлично справится как с простым поиском всех заказов юзера (по user_id),
-- так и со сложным поиском заказов конкретного юзера с фильтрацией по цене.
CREATE INDEX idx_orders_user_cost ON orders(user_id, cost);

-- Отдельный индекс чисто на cost, если нужен поиск дешевых/дорогих заказов по всей базе сразу
CREATE INDEX idx_orders_cost_only ON orders(cost);