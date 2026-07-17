CREATE TABLE source_school (
    id UUID PRIMARY KEY,
    school_id UUID NOT NULL,
    name VARCHAR(180) NOT NULL,
    inep_code VARCHAR(32),
    cnpj VARCHAR(32),
    cep VARCHAR(16),
    street VARCHAR(180),
    address_number VARCHAR(32),
    address_extra VARCHAR(120),
    district VARCHAR(120),
    city VARCHAR(120),
    state_code VARCHAR(8),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE student_transfer (
    id UUID PRIMARY KEY,
    school_id UUID NOT NULL,
    student_id UUID NOT NULL,
    source_school_id UUID NOT NULL,
    source_grade VARCHAR(120) NOT NULL,
    source_school_year VARCHAR(32) NOT NULL,
    transfer_date DATE,
    transfer_reason VARCHAR(300),
    source_status VARCHAR(80),
    documents_delivered VARCHAR(40),
    note VARCHAR(500),
    transfer_type VARCHAR(80),
    transfer_status VARCHAR(80),
    operator_user VARCHAR(120),
    operation_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_student_transfer_source_school
        FOREIGN KEY (source_school_id) REFERENCES source_school(id)
);

CREATE TABLE student_document (
    id UUID PRIMARY KEY,
    school_id UUID NOT NULL,
    student_id UUID NOT NULL,
    document_type VARCHAR(120) NOT NULL,
    file_name VARCHAR(180),
    file_url VARCHAR(500),
    document_number VARCHAR(180),
    file_path VARCHAR(500),
    uploaded_at TIMESTAMP NOT NULL,
    note VARCHAR(500)
);

CREATE TABLE administrative_document (
    id UUID PRIMARY KEY,
    school_id UUID NOT NULL,
    school_name VARCHAR(180),
    entity_type VARCHAR(80) NOT NULL,
    entity_id UUID NOT NULL,
    document_type VARCHAR(120) NOT NULL,
    document_number VARCHAR(180),
    file_path VARCHAR(500),
    uploaded_at TIMESTAMP NOT NULL,
    note VARCHAR(500)
);

CREATE TABLE enrollment_record (
    id UUID PRIMARY KEY,
    school_id UUID NOT NULL,
    student_id UUID NOT NULL,
    class_id UUID,
    school_name VARCHAR(180),
    grade_id UUID,
    grade_name VARCHAR(120),
    term_id UUID,
    status VARCHAR(80),
    enrollment_type VARCHAR(80),
    enrollment_date DATE,
    note VARCHAR(500),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE enrollment_step (
    id UUID PRIMARY KEY,
    enrollment_id UUID NOT NULL,
    description VARCHAR(180) NOT NULL,
    step_order INTEGER,
    status VARCHAR(80),
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    note VARCHAR(500),
    CONSTRAINT fk_enrollment_step_record
        FOREIGN KEY (enrollment_id) REFERENCES enrollment_record(id)
);

CREATE INDEX idx_source_school_school_id ON source_school (school_id, name);
CREATE INDEX idx_student_transfer_school_student ON student_transfer (school_id, student_id, created_at);
CREATE INDEX idx_student_document_school_student ON student_document (school_id, student_id, uploaded_at);
CREATE INDEX idx_admin_document_school_entity ON administrative_document (school_id, entity_type, entity_id, uploaded_at);
CREATE INDEX idx_enrollment_record_school ON enrollment_record (school_id, created_at);
CREATE INDEX idx_enrollment_step_record ON enrollment_step (enrollment_id, step_order);
