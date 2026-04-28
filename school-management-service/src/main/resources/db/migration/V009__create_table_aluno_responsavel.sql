CREATE TABLE aluno_responsavel (
    id_aluno_responsavel UUID PRIMARY KEY,
    id_aluno UUID NOT NULL,
    id_responsavel UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_aluno_responsavel_aluno
        FOREIGN KEY (id_aluno) REFERENCES aluno (id_aluno),
    CONSTRAINT fk_aluno_responsavel_responsavel
        FOREIGN KEY (id_responsavel) REFERENCES responsavel (id_responsavel),
    CONSTRAINT uk_aluno_responsavel UNIQUE (id_aluno, id_responsavel)
);

CREATE INDEX idx_aluno_responsavel_aluno ON aluno_responsavel (id_aluno);
CREATE INDEX idx_aluno_responsavel_responsavel ON aluno_responsavel (id_responsavel);
