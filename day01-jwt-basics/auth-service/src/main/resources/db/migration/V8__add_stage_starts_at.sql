-- Дата/время, с которого этап планируется к выполнению (опционально)
ALTER TABLE stages ADD COLUMN starts_at TIMESTAMP WITHOUT TIME ZONE;
