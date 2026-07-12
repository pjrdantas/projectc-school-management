CREATE TABLE IF NOT EXISTS professor_turma_shadow_sync_state (
    id_turma UUID PRIMARY KEY,
    id_escola UUID NOT NULL,
    alocacoes_completas BOOLEAN NOT NULL,
    alocacao_count BIGINT NOT NULL,
    sincronizado_em TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_professor_turma_shadow_sync_state_escola
    ON professor_turma_shadow_sync_state (id_escola);
