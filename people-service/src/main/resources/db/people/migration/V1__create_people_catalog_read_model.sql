CREATE TABLE IF NOT EXISTS tipo_pessoa (
    id_tipo_pessoa UUID NOT NULL PRIMARY KEY,
    codigo VARCHAR(40) NOT NULL,
    descricao VARCHAR(120) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_people_read_model_tipo_pessoa_codigo UNIQUE (codigo)
);

CREATE TABLE IF NOT EXISTS tipo_endereco (
    id_tipo_endereco UUID NOT NULL PRIMARY KEY,
    codigo VARCHAR(40) NOT NULL,
    descricao VARCHAR(120) NOT NULL,
    CONSTRAINT uk_people_read_model_tipo_endereco_codigo UNIQUE (codigo)
);
