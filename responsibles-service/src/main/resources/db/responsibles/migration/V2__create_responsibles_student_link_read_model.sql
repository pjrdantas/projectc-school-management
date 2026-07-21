CREATE TABLE IF NOT EXISTS aluno_responsavel (
    id_aluno_responsavel UUID NOT NULL PRIMARY KEY,
    id_aluno UUID NOT NULL,
    id_responsavel UUID NOT NULL,
    id_parentesco UUID,
    responsavel_financeiro BOOLEAN NOT NULL DEFAULT FALSE,
    responsavel_pedagogico BOOLEAN NOT NULL DEFAULT FALSE,
    autorizado_retirar BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_responsibles_read_model_aluno_responsavel_responsavel
        FOREIGN KEY (id_responsavel) REFERENCES responsavel(id_responsavel),
    CONSTRAINT uk_responsibles_read_model_aluno_responsavel
        UNIQUE (id_aluno, id_responsavel)
);

CREATE INDEX IF NOT EXISTS idx_responsibles_read_model_aluno_responsavel_aluno
    ON aluno_responsavel(id_aluno);

CREATE INDEX IF NOT EXISTS idx_responsibles_read_model_aluno_responsavel_responsavel
    ON aluno_responsavel(id_responsavel);

CREATE INDEX IF NOT EXISTS idx_responsibles_read_model_aluno_responsavel_parentesco
    ON aluno_responsavel(id_parentesco);
