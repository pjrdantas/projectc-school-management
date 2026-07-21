ALTER TABLE aluno
    ADD COLUMN IF NOT EXISTS id_escola UUID;

ALTER TABLE aluno
    ADD COLUMN IF NOT EXISTS id_status_aluno UUID;

ALTER TABLE aluno
    ADD COLUMN IF NOT EXISTS ra VARCHAR(80);

ALTER TABLE aluno
    ADD COLUMN IF NOT EXISTS rm VARCHAR(80);

ALTER TABLE aluno
    ADD COLUMN IF NOT EXISTS emancipado BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE aluno
    ADD COLUMN IF NOT EXISTS data_ingresso DATE;

ALTER TABLE aluno
    ADD COLUMN IF NOT EXISTS data_saida DATE;

ALTER TABLE aluno
    ADD COLUMN IF NOT EXISTS motivo_saida TEXT;

ALTER TABLE aluno
    ADD COLUMN IF NOT EXISTS ativo BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE aluno
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

UPDATE aluno
SET id_escola = (
    SELECT pessoa.id_escola
    FROM pessoa
    WHERE pessoa.id_pessoa = aluno.id_pessoa
)
WHERE id_escola IS NULL
  AND id_pessoa IS NOT NULL;

ALTER TABLE aluno
    ADD CONSTRAINT fk_people_aluno_status
        FOREIGN KEY (id_status_aluno) REFERENCES status_aluno(id_status_aluno);

CREATE INDEX IF NOT EXISTS idx_people_aluno_escola_ativo_nome
    ON aluno(id_escola, ativo, nome_completo);

CREATE INDEX IF NOT EXISTS idx_people_aluno_status
    ON aluno(id_status_aluno);
