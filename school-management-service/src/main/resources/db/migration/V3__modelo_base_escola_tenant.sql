CREATE TABLE IF NOT EXISTS escola (
    id_escola uuid PRIMARY KEY,
    nome varchar(150) NOT NULL,
    codigo_inep varchar(30),
    cnpj varchar(18),
    telefone varchar(30),
    email varchar(150),
    id_endereco uuid,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp
);

ALTER TABLE escola
    ADD COLUMN IF NOT EXISTS telefone varchar(30);

ALTER TABLE escola
    ADD COLUMN IF NOT EXISTS email varchar(150);

ALTER TABLE escola
    ADD COLUMN IF NOT EXISTS id_endereco uuid;

ALTER TABLE escola
    ADD COLUMN IF NOT EXISTS ativo boolean DEFAULT true NOT NULL;

ALTER TABLE escola
    ADD COLUMN IF NOT EXISTS updated_at timestamp;

INSERT INTO escola (id_escola, nome, ativo, created_at)
VALUES ('00000000-0000-0000-0000-000000000047', 'Escola padrão', true, CURRENT_TIMESTAMP)
ON CONFLICT (id_escola) DO NOTHING;

ALTER TABLE usuario
    ADD COLUMN IF NOT EXISTS id_escola uuid;

UPDATE usuario
SET id_escola = '00000000-0000-0000-0000-000000000047'
WHERE id_escola IS NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_usuario_escola'
    ) THEN
        ALTER TABLE usuario
            ADD CONSTRAINT fk_usuario_escola
            FOREIGN KEY (id_escola)
            REFERENCES escola(id_escola);
    END IF;
END $$;

ALTER TABLE sessao_autenticacao
    ADD COLUMN IF NOT EXISTS id_escola uuid;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_sessao_autenticacao_escola'
    ) THEN
        ALTER TABLE sessao_autenticacao
            ADD CONSTRAINT fk_sessao_autenticacao_escola
            FOREIGN KEY (id_escola)
            REFERENCES escola(id_escola);
    END IF;
END $$;
