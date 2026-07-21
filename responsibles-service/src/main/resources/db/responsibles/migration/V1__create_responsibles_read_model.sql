CREATE TABLE IF NOT EXISTS responsavel (
    id_responsavel UUID NOT NULL PRIMARY KEY,
    nome_completo VARCHAR(150) NOT NULL,
    cpf VARCHAR(14),
    email VARCHAR(150),
    telefone VARCHAR(20),
    rg VARCHAR(20),
    cep VARCHAR(14),
    logradouro VARCHAR(200),
    numero VARCHAR(20),
    complemento VARCHAR(120),
    bairro VARCHAR(120),
    cidade VARCHAR(120),
    uf VARCHAR(2),
    id_escola UUID NOT NULL,
    escola_nome VARCHAR(150),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_responsibles_read_model_nome
    ON responsavel(nome_completo);

CREATE INDEX IF NOT EXISTS idx_responsibles_read_model_cpf
    ON responsavel(cpf);

CREATE INDEX IF NOT EXISTS idx_responsibles_read_model_escola
    ON responsavel(id_escola);
