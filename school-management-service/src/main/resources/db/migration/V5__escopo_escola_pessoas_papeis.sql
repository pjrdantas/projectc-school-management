ALTER TABLE pessoa
    ADD COLUMN IF NOT EXISTS id_escola uuid;

UPDATE pessoa
SET id_escola = '00000000-0000-0000-0000-000000000047'
WHERE id_escola IS NULL;

ALTER TABLE pessoa
    ALTER COLUMN id_escola SET NOT NULL;

ALTER TABLE pessoa
    ADD CONSTRAINT fk_pessoa_escola
    FOREIGN KEY (id_escola) REFERENCES escola(id_escola);

ALTER TABLE pessoa
    DROP CONSTRAINT IF EXISTS pessoa_cpf_key;

ALTER TABLE pessoa
    DROP CONSTRAINT IF EXISTS uk_pessoa_cpf;

ALTER TABLE pessoa
    ADD CONSTRAINT uk_pessoa_escola_cpf
    UNIQUE (id_escola, cpf);

CREATE INDEX IF NOT EXISTS idx_pessoa_escola
    ON pessoa(id_escola);

CREATE INDEX IF NOT EXISTS idx_pessoa_escola_nome
    ON pessoa(id_escola, nome_completo);
