ALTER TABLE administrative_document
    ADD COLUMN IF NOT EXISTS file_name VARCHAR(180);
