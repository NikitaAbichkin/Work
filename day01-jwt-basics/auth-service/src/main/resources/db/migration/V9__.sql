-- V9: Fix column types and enum values

-- stages.deadline: TIME → DATE (нет осмысленной конвертации TIME→DATE, пересоздаём)
ALTER TABLE stages DROP COLUMN IF EXISTS deadline;
ALTER TABLE stages ADD COLUMN deadline DATE;

-- stages.starts_at: TIME → DATE (аналогично)
ALTER TABLE stages DROP COLUMN IF EXISTS starts_at;
ALTER TABLE stages ADD COLUMN starts_at DATE;

-- Приводим ENUM-значения к UPPERCASE (столбцы VARCHAR, поэтому UPPER() безопасен)
UPDATE goals SET status = UPPER(status) WHERE status IS NOT NULL AND status <> UPPER(status);
UPDATE goals SET priority = UPPER(priority) WHERE priority IS NOT NULL AND priority <> UPPER(priority);
UPDATE stages SET priority = UPPER(priority) WHERE priority IS NOT NULL AND priority <> UPPER(priority);
UPDATE stages SET status = UPPER(status) WHERE status IS NOT NULL AND status <> UPPER(status);
