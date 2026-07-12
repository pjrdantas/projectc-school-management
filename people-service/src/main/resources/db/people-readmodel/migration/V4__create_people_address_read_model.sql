CREATE TABLE IF NOT EXISTS endereco (
    id_endereco UUID NOT NULL PRIMARY KEY,
    cep VARCHAR(10),
    logradouro VARCHAR(150),
    numero VARCHAR(20),
    complemento VARCHAR(100),
    bairro VARCHAR(100),
    cidade VARCHAR(100),
    uf VARCHAR(2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_people_read_model_endereco_cep
    ON endereco(cep);

CREATE INDEX IF NOT EXISTS idx_people_read_model_endereco_cidade_uf
    ON endereco(cidade, uf);

CREATE TABLE IF NOT EXISTS pessoa_endereco (
    id_pessoa_endereco UUID NOT NULL PRIMARY KEY,
    id_pessoa UUID NOT NULL,
    id_endereco UUID NOT NULL,
    id_tipo_endereco UUID,
    principal BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_people_read_model_pessoa_endereco_pessoa
        FOREIGN KEY (id_pessoa) REFERENCES pessoa(id_pessoa),
    CONSTRAINT fk_people_read_model_pessoa_endereco_endereco
        FOREIGN KEY (id_endereco) REFERENCES endereco(id_endereco),
    CONSTRAINT fk_people_read_model_pessoa_endereco_tipo
        FOREIGN KEY (id_tipo_endereco) REFERENCES tipo_endereco(id_tipo_endereco)
);

CREATE INDEX IF NOT EXISTS idx_people_read_model_pessoa_endereco_pessoa
    ON pessoa_endereco(id_pessoa);

CREATE INDEX IF NOT EXISTS idx_people_read_model_pessoa_endereco_endereco
    ON pessoa_endereco(id_endereco);

CREATE INDEX IF NOT EXISTS idx_people_read_model_pessoa_endereco_tipo
    ON pessoa_endereco(id_tipo_endereco);

CREATE INDEX IF NOT EXISTS idx_people_read_model_pessoa_endereco_principal
    ON pessoa_endereco(id_pessoa, principal);
