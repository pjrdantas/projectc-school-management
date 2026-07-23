ALTER TABLE student_document
    ADD COLUMN IF NOT EXISTS storage_reference VARCHAR(500);

ALTER TABLE student_document
    ADD COLUMN IF NOT EXISTS content_type VARCHAR(160);

ALTER TABLE student_document
    ADD COLUMN IF NOT EXISTS file_size BIGINT;

ALTER TABLE student_document
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

ALTER TABLE student_document
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

ALTER TABLE administrative_document
    ADD COLUMN IF NOT EXISTS storage_reference VARCHAR(500);

ALTER TABLE administrative_document
    ADD COLUMN IF NOT EXISTS content_type VARCHAR(160);

ALTER TABLE administrative_document
    ADD COLUMN IF NOT EXISTS file_size BIGINT;

ALTER TABLE administrative_document
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

ALTER TABLE administrative_document
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

UPDATE student_document
SET storage_reference = file_path
WHERE storage_reference IS NULL
  AND file_path IS NOT NULL;

UPDATE administrative_document
SET storage_reference = file_path
WHERE storage_reference IS NULL
  AND file_path IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_student_document_school_active
    ON student_document (school_id, deleted_at, uploaded_at);

CREATE INDEX IF NOT EXISTS idx_administrative_document_school_active
    ON administrative_document (school_id, deleted_at, uploaded_at);
