CREATE TABLE IF NOT EXISTS professor (
    id_professor UUID PRIMARY KEY,
    id_pessoa UUID NOT NULL,
    nome_completo VARCHAR(150) NOT NULL,
    id_escola UUID NOT NULL,
    escola_nome VARCHAR(120),
    registro_profissional VARCHAR(80),
    formacao VARCHAR(150),
    ativo BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_professor_pessoa_escola
    ON professor (id_pessoa, id_escola);

CREATE INDEX IF NOT EXISTS idx_professor_escola_nome
    ON professor (id_escola, nome_completo);
