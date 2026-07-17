CREATE TABLE report_card_record (
    id UUID PRIMARY KEY,
    school_id UUID NOT NULL,
    enrollment_id UUID NOT NULL,
    is_closure BOOLEAN NOT NULL,
    payload_json CLOB NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE class_lesson_record (
    id UUID PRIMARY KEY,
    school_id UUID NOT NULL,
    allocation_id UUID,
    class_id UUID,
    payload_json CLOB NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE teacher_attendance_record (
    id UUID PRIMARY KEY,
    school_id UUID NOT NULL,
    lesson_id UUID NOT NULL,
    payload_json CLOB NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE student_attendance_record (
    id UUID PRIMARY KEY,
    school_id UUID NOT NULL,
    lesson_id UUID NOT NULL,
    enrollment_id UUID,
    payload_json CLOB NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE assessment_record (
    id UUID PRIMARY KEY,
    school_id UUID NOT NULL,
    allocation_id UUID,
    class_id UUID,
    title VARCHAR(180),
    payload_json CLOB NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE student_grade_record (
    id UUID PRIMARY KEY,
    school_id UUID NOT NULL,
    assessment_id UUID NOT NULL,
    enrollment_id UUID,
    payload_json CLOB NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE class_diary_record (
    id VARCHAR(120) PRIMARY KEY,
    school_id UUID NOT NULL,
    professor_id UUID,
    class_id UUID,
    subject_id UUID,
    school_year INTEGER,
    month_number INTEGER,
    reference_date DATE,
    read_payload_json CLOB,
    write_payload_json CLOB,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE academic_history_record (
    id UUID PRIMARY KEY,
    school_id UUID NOT NULL,
    student_id UUID,
    enrollment_id UUID,
    screen_mode VARCHAR(40),
    screen_payload_json CLOB,
    write_payload_json CLOB,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_report_card_school_enrollment ON report_card_record (school_id, enrollment_id, is_closure, updated_at);
CREATE INDEX idx_class_lesson_school ON class_lesson_record (school_id, created_at);
CREATE INDEX idx_teacher_attendance_lesson ON teacher_attendance_record (school_id, lesson_id, created_at);
CREATE INDEX idx_student_attendance_lesson ON student_attendance_record (school_id, lesson_id, created_at);
CREATE INDEX idx_assessment_school ON assessment_record (school_id, created_at);
CREATE INDEX idx_student_grade_assessment ON student_grade_record (school_id, assessment_id, created_at);
CREATE INDEX idx_student_grade_enrollment ON student_grade_record (school_id, enrollment_id, created_at);
CREATE INDEX idx_class_diary_lookup ON class_diary_record (school_id, professor_id, class_id, subject_id, school_year, month_number, reference_date, updated_at);
CREATE INDEX idx_academic_history_lookup ON academic_history_record (school_id, student_id, enrollment_id, screen_mode, updated_at);
