ALTER TABLE aluno_responsavel
    ADD COLUMN IF NOT EXISTS id_parentesco UUID;

ALTER TABLE aluno_responsavel
    ADD COLUMN IF NOT EXISTS responsavel_financeiro BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE aluno_responsavel
    ADD COLUMN IF NOT EXISTS responsavel_pedagogico BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE aluno_responsavel
    ADD COLUMN IF NOT EXISTS autorizado_retirar BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_people_read_model_aluno_responsavel_parentesco
    ON aluno_responsavel(id_parentesco);
