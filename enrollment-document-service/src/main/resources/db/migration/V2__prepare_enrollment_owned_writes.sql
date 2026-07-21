ALTER TABLE enrollment_record
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

ALTER TABLE enrollment_record
    ADD COLUMN IF NOT EXISTS cancelled_at TIMESTAMP;

ALTER TABLE enrollment_record
    ADD COLUMN IF NOT EXISTS cancellation_reason VARCHAR(500);

UPDATE enrollment_record
SET status = 'PENDENTE'
WHERE status IS NULL;

ALTER TABLE enrollment_record
    ALTER COLUMN status SET DEFAULT 'PENDENTE';

ALTER TABLE enrollment_record
    ALTER COLUMN status SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_enrollment_record_school_student_term_status
    ON enrollment_record (school_id, student_id, term_id, status);

CREATE UNIQUE INDEX IF NOT EXISTS uk_enrollment_record_school_student_class_term_status
    ON enrollment_record (school_id, student_id, class_id, term_id, status);
