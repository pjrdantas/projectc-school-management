ALTER TABLE professor_turma_disciplina
    ADD COLUMN IF NOT EXISTS ativo_chave BOOLEAN;

UPDATE professor_turma_disciplina
SET ativo_chave = CASE WHEN ativo THEN TRUE ELSE NULL END;

DROP INDEX IF EXISTS uk_professor_turma_disciplina;

CREATE UNIQUE INDEX IF NOT EXISTS uk_professor_turma_disciplina_ativa
    ON professor_turma_disciplina (id_professor, id_turma_disciplina, ativo_chave);
