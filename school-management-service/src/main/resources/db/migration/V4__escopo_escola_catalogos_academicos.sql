ALTER TABLE periodo_letivo
    ADD COLUMN IF NOT EXISTS id_escola uuid;

UPDATE periodo_letivo
SET id_escola = '00000000-0000-0000-0000-000000000047'
WHERE id_escola IS NULL;

ALTER TABLE periodo_letivo
    ALTER COLUMN id_escola SET NOT NULL;

ALTER TABLE periodo_letivo
    ADD CONSTRAINT fk_periodo_letivo_escola
    FOREIGN KEY (id_escola) REFERENCES escola(id_escola);

CREATE INDEX IF NOT EXISTS idx_periodo_letivo_escola
    ON periodo_letivo(id_escola);

ALTER TABLE serie
    ADD COLUMN IF NOT EXISTS id_escola uuid;

UPDATE serie
SET id_escola = '00000000-0000-0000-0000-000000000047'
WHERE id_escola IS NULL;

ALTER TABLE serie
    ALTER COLUMN id_escola SET NOT NULL;

ALTER TABLE serie
    ADD CONSTRAINT fk_serie_escola
    FOREIGN KEY (id_escola) REFERENCES escola(id_escola);

CREATE INDEX IF NOT EXISTS idx_serie_escola
    ON serie(id_escola);

ALTER TABLE disciplina
    ADD COLUMN IF NOT EXISTS id_escola uuid;

UPDATE disciplina
SET id_escola = '00000000-0000-0000-0000-000000000047'
WHERE id_escola IS NULL;

ALTER TABLE disciplina
    ALTER COLUMN id_escola SET NOT NULL;

ALTER TABLE disciplina
    ADD CONSTRAINT fk_disciplina_escola
    FOREIGN KEY (id_escola) REFERENCES escola(id_escola);

CREATE INDEX IF NOT EXISTS idx_disciplina_escola
    ON disciplina(id_escola);

ALTER TABLE turma
    ADD COLUMN IF NOT EXISTS id_escola uuid;

UPDATE turma
SET id_escola = '00000000-0000-0000-0000-000000000047'
WHERE id_escola IS NULL;

ALTER TABLE turma
    ALTER COLUMN id_escola SET NOT NULL;

ALTER TABLE turma
    ADD CONSTRAINT fk_turma_escola
    FOREIGN KEY (id_escola) REFERENCES escola(id_escola);

ALTER TABLE turma
    DROP CONSTRAINT IF EXISTS turma_id_periodo_letivo_codigo_key;

ALTER TABLE turma
    DROP CONSTRAINT IF EXISTS uk_turma_periodo_codigo;

ALTER TABLE turma
    ADD CONSTRAINT uk_turma_escola_periodo_codigo
    UNIQUE (id_escola, id_periodo_letivo, codigo);

CREATE INDEX IF NOT EXISTS idx_turma_escola
    ON turma(id_escola);
