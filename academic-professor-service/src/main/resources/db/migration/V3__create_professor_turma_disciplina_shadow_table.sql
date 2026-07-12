CREATE TABLE IF NOT EXISTS professor_turma_disciplina (
    id_professor_turma_disciplina UUID PRIMARY KEY,
    id_professor UUID NOT NULL,
    id_turma_disciplina UUID NOT NULL,
    id_turma UUID NOT NULL,
    nome_turma VARCHAR(120),
    id_disciplina UUID NOT NULL,
    nome_disciplina VARCHAR(120),
    data_inicio DATE,
    data_fim DATE,
    ativo BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_professor_turma_disciplina_shadow
    ON professor_turma_disciplina (id_professor, id_turma_disciplina);

CREATE INDEX IF NOT EXISTS idx_professor_turma_disciplina_shadow_turma
    ON professor_turma_disciplina (id_turma);
