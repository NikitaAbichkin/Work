-- V10: Rename estimated_time → estimated_minutes (VARCHAR→INTEGER) + add updated_at

-- Переименовываем и меняем тип, сохраняя числовые данные
ALTER TABLE stages RENAME COLUMN estimated_time TO estimated_minutes;
ALTER TABLE stages ALTER COLUMN estimated_minutes TYPE INTEGER USING NULLIF(estimated_minutes, '')::INTEGER;

-- Добавляем updated_at
ALTER TABLE stages ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
