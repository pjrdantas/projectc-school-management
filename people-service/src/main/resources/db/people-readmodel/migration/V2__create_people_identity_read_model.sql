CREATE TABLE IF NOT EXISTS pessoa (
    id_pessoa UUID NOT NULL PRIMARY KEY,
    id_escola UUID NOT NULL,
    escola_nome VARCHAR(150) NOT NULL,
    nome_completo VARCHAR(150) NOT NULL,
    cpf VARCHAR(14),
    rg VARCHAR(20),
    orgao_emissor_rg VARCHAR(20),
    uf_rg VARCHAR(2),
    email VARCHAR(150),
    telefone VARCHAR(20),
    data_nascimento DATE,
    sexo VARCHAR(20),
    nome_social VARCHAR(150),
    nacionalidade VARCHAR(80),
    naturalidade VARCHAR(100),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_people_read_model_pessoa_escola_cpf UNIQUE (id_escola, cpf)
);

CREATE INDEX IF NOT EXISTS idx_people_read_model_pessoa_escola
    ON pessoa(id_escola);

CREATE INDEX IF NOT EXISTS idx_people_read_model_pessoa_escola_nome
    ON pessoa(id_escola, nome_completo);

CREATE TABLE IF NOT EXISTS pessoa_tipo_pessoa (
    id_pessoa_tipo_pessoa UUID NOT NULL PRIMARY KEY,
    id_pessoa UUID NOT NULL,
    id_tipo_pessoa UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_people_read_model_pessoa_tipo_pessoa_pessoa
        FOREIGN KEY (id_pessoa) REFERENCES pessoa(id_pessoa),
    CONSTRAINT fk_people_read_model_pessoa_tipo_pessoa_tipo
        FOREIGN KEY (id_tipo_pessoa) REFERENCES tipo_pessoa(id_tipo_pessoa),
    CONSTRAINT uk_people_read_model_pessoa_tipo_pessoa
        UNIQUE (id_pessoa, id_tipo_pessoa)
);

CREATE INDEX IF NOT EXISTS idx_people_read_model_pessoa_tipo_pessoa_pessoa
    ON pessoa_tipo_pessoa(id_pessoa);

CREATE INDEX IF NOT EXISTS idx_people_read_model_pessoa_tipo_pessoa_tipo
    ON pessoa_tipo_pessoa(id_tipo_pessoa);
