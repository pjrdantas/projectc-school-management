DROP TABLE IF EXISTS professor_alocacao_shadow_sync_state;
DROP TABLE IF EXISTS professor_turma_shadow_sync_state;
DROP TABLE IF EXISTS professor_shadow_sync_state;

DROP INDEX IF EXISTS uk_professor_turma_disciplina_shadow;
DROP INDEX IF EXISTS idx_professor_turma_disciplina_shadow_turma;

CREATE UNIQUE INDEX IF NOT EXISTS uk_professor_turma_disciplina
    ON professor_turma_disciplina (id_professor, id_turma_disciplina);

CREATE INDEX IF NOT EXISTS idx_professor_turma_disciplina_turma
    ON professor_turma_disciplina (id_turma);
