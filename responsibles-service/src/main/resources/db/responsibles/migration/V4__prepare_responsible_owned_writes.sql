ALTER TABLE responsavel
    ADD COLUMN IF NOT EXISTS ativo BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE responsavel
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS uk_responsibles_responsavel_escola_cpf
    ON responsavel(id_escola, cpf);

CREATE INDEX IF NOT EXISTS idx_responsibles_responsavel_escola_ativo_nome
    ON responsavel(id_escola, ativo, nome_completo);
