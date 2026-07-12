CREATE TABLE IF NOT EXISTS professor_shadow_sync_state (
    id_escola UUID PRIMARY KEY,
    professores_completos BOOLEAN NOT NULL,
    professor_count BIGINT NOT NULL,
    sincronizado_em TIMESTAMP NOT NULL
);
