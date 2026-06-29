CREATE TABLE IF NOT EXISTS professor_alocacao_shadow_sync_state (
    id_professor UUID PRIMARY KEY,
    id_escola UUID NOT NULL,
    alocacoes_completas BOOLEAN NOT NULL,
    alocacao_count BIGINT NOT NULL,
    sincronizado_em TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_professor_alocacao_shadow_sync_state_escola
    ON professor_alocacao_shadow_sync_state (id_escola);
