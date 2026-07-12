CREATE TABLE IF NOT EXISTS people_funcionario_read_model (
    id_funcionario UUID NOT NULL PRIMARY KEY,
    id_pessoa UUID NOT NULL,
    id_escola UUID NOT NULL,
    nome_completo VARCHAR(200) NOT NULL,
    cargo_descricao VARCHAR(120),
    ativo BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_people_funcionario_read_model_pessoa
    ON people_funcionario_read_model (id_pessoa);

CREATE INDEX IF NOT EXISTS idx_people_funcionario_read_model_escola_ativo
    ON people_funcionario_read_model (id_escola, ativo);

CREATE INDEX IF NOT EXISTS idx_people_funcionario_read_model_escola_nome
    ON people_funcionario_read_model (id_escola, nome_completo);
