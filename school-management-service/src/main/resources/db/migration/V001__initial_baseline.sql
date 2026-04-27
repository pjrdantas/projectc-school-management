CREATE TABLE IF NOT EXISTS flyway_audit_marker (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO flyway_audit_marker (description)
VALUES ('initial baseline');
