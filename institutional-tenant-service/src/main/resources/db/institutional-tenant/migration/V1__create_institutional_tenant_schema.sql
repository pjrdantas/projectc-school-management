CREATE TABLE escola (
    id_escola UUID NOT NULL,
    nome VARCHAR(150) NOT NULL,
    codigo_inep VARCHAR(30),
    cnpj VARCHAR(18),
    telefone VARCHAR(30),
    email VARCHAR(150),
    id_endereco UUID,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT pk_escola PRIMARY KEY (id_escola)
);

CREATE TABLE usuario_escola (
    id_usuario_escola UUID NOT NULL,
    id_usuario UUID NOT NULL,
    id_escola UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_usuario_escola PRIMARY KEY (id_usuario_escola),
    CONSTRAINT fk_usuario_escola_escola FOREIGN KEY (id_escola)
        REFERENCES escola (id_escola) ON DELETE CASCADE,
    CONSTRAINT uk_usuario_escola UNIQUE (id_usuario, id_escola)
);

CREATE INDEX idx_escola_ativa_nome
    ON escola (ativo, nome);

CREATE INDEX idx_usuario_escola_usuario
    ON usuario_escola (id_usuario);

CREATE INDEX idx_usuario_escola_escola
    ON usuario_escola (id_escola);
