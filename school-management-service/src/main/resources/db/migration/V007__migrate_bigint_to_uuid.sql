ALTER TABLE aluno ADD COLUMN id_uuid UUID;
UPDATE aluno
SET id_uuid = CAST('00000000-0000-0000-0000-' || RIGHT('000000000000' || CAST(id AS VARCHAR), 12) AS UUID)
WHERE id_uuid IS NULL;
ALTER TABLE aluno ALTER COLUMN id_uuid SET NOT NULL;

ALTER TABLE periodo_letivo ADD COLUMN id_uuid UUID;
UPDATE periodo_letivo
SET id_uuid = CAST('00000000-0000-0000-0000-' || RIGHT('000000000000' || CAST(id AS VARCHAR), 12) AS UUID)
WHERE id_uuid IS NULL;
ALTER TABLE periodo_letivo ALTER COLUMN id_uuid SET NOT NULL;

ALTER TABLE turma ADD COLUMN id_uuid UUID;
ALTER TABLE turma ADD COLUMN periodo_letivo_id_uuid UUID;
UPDATE turma
SET id_uuid = CAST('00000000-0000-0000-0000-' || RIGHT('000000000000' || CAST(id AS VARCHAR), 12) AS UUID)
WHERE id_uuid IS NULL;
UPDATE turma t
SET periodo_letivo_id_uuid = p.id_uuid
FROM periodo_letivo p
WHERE t.periodo_letivo_id = p.id;
ALTER TABLE turma ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE turma ALTER COLUMN periodo_letivo_id_uuid SET NOT NULL;

ALTER TABLE matricula ADD COLUMN id_uuid UUID;
ALTER TABLE matricula ADD COLUMN aluno_id_uuid UUID;
ALTER TABLE matricula ADD COLUMN turma_id_uuid UUID;
ALTER TABLE matricula ADD COLUMN periodo_letivo_id_uuid UUID;
UPDATE matricula
SET id_uuid = CAST('00000000-0000-0000-0000-' || RIGHT('000000000000' || CAST(id AS VARCHAR), 12) AS UUID)
WHERE id_uuid IS NULL;
UPDATE matricula m
SET aluno_id_uuid = (SELECT a.id_uuid FROM aluno a WHERE a.id = m.aluno_id),
    turma_id_uuid = (SELECT t.id_uuid FROM turma t WHERE t.id = m.turma_id),
    periodo_letivo_id_uuid = (SELECT p.id_uuid FROM periodo_letivo p WHERE p.id = m.periodo_letivo_id);
ALTER TABLE matricula ALTER COLUMN id_uuid SET NOT NULL;
ALTER TABLE matricula ALTER COLUMN aluno_id_uuid SET NOT NULL;
ALTER TABLE matricula ALTER COLUMN turma_id_uuid SET NOT NULL;
ALTER TABLE matricula ALTER COLUMN periodo_letivo_id_uuid SET NOT NULL;

ALTER TABLE matricula DROP CONSTRAINT fk_matricula_aluno;
ALTER TABLE matricula DROP CONSTRAINT fk_matricula_turma;
ALTER TABLE matricula DROP CONSTRAINT fk_matricula_periodo_letivo;
ALTER TABLE turma DROP CONSTRAINT fk_turma_periodo_letivo;
ALTER TABLE turma DROP CONSTRAINT uk_turma_codigo_periodo;

ALTER TABLE aluno DROP PRIMARY KEY;
ALTER TABLE periodo_letivo DROP PRIMARY KEY;
ALTER TABLE turma DROP PRIMARY KEY;
ALTER TABLE matricula DROP PRIMARY KEY;

ALTER TABLE aluno RENAME COLUMN id TO id_legacy;
ALTER TABLE aluno RENAME COLUMN id_uuid TO id;
ALTER TABLE periodo_letivo RENAME COLUMN id TO id_legacy;
ALTER TABLE periodo_letivo RENAME COLUMN id_uuid TO id;

ALTER TABLE turma RENAME COLUMN id TO id_legacy;
ALTER TABLE turma RENAME COLUMN id_uuid TO id;
ALTER TABLE turma RENAME COLUMN periodo_letivo_id TO periodo_letivo_id_legacy;
ALTER TABLE turma RENAME COLUMN periodo_letivo_id_uuid TO periodo_letivo_id;

ALTER TABLE matricula RENAME COLUMN id TO id_legacy;
ALTER TABLE matricula RENAME COLUMN id_uuid TO id;
ALTER TABLE matricula RENAME COLUMN aluno_id TO aluno_id_legacy;
ALTER TABLE matricula RENAME COLUMN aluno_id_uuid TO aluno_id;
ALTER TABLE matricula RENAME COLUMN turma_id TO turma_id_legacy;
ALTER TABLE matricula RENAME COLUMN turma_id_uuid TO turma_id;
ALTER TABLE matricula RENAME COLUMN periodo_letivo_id TO periodo_letivo_id_legacy;
ALTER TABLE matricula RENAME COLUMN periodo_letivo_id_uuid TO periodo_letivo_id;

ALTER TABLE aluno ADD CONSTRAINT aluno_pkey PRIMARY KEY (id);
ALTER TABLE periodo_letivo ADD CONSTRAINT periodo_letivo_pkey PRIMARY KEY (id);
ALTER TABLE turma ADD CONSTRAINT turma_pkey PRIMARY KEY (id);
ALTER TABLE matricula ADD CONSTRAINT matricula_pkey PRIMARY KEY (id);

ALTER TABLE turma ADD CONSTRAINT fk_turma_periodo_letivo FOREIGN KEY (periodo_letivo_id) REFERENCES periodo_letivo (id);
ALTER TABLE turma ADD CONSTRAINT uk_turma_codigo_periodo UNIQUE (codigo, periodo_letivo_id);

ALTER TABLE matricula ADD CONSTRAINT fk_matricula_aluno FOREIGN KEY (aluno_id) REFERENCES aluno (id);
ALTER TABLE matricula ADD CONSTRAINT fk_matricula_turma FOREIGN KEY (turma_id) REFERENCES turma (id);
ALTER TABLE matricula ADD CONSTRAINT fk_matricula_periodo_letivo FOREIGN KEY (periodo_letivo_id) REFERENCES periodo_letivo (id);
