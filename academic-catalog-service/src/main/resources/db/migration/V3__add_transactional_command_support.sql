ALTER TABLE outbox_event
    ADD COLUMN causation_id uuid,
    ADD COLUMN usuario_id uuid;

CREATE TABLE command_idempotency (
    id_command_idempotency uuid PRIMARY KEY,
    escola_id uuid NOT NULL,
    idempotency_key varchar(160) NOT NULL,
    request_hash varchar(64) NOT NULL,
    resource_type varchar(100) NOT NULL,
    resource_id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    CONSTRAINT uk_command_idempotency_escola_key UNIQUE (escola_id, idempotency_key)
);

CREATE INDEX idx_command_idempotency_created_at
    ON command_idempotency(created_at);
