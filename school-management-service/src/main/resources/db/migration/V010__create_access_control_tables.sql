CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE usuario (
    id_usuario UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(80) NOT NULL UNIQUE,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE perfil (
    id_perfil UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nome VARCHAR(120) NOT NULL,
    descricao VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE permissao (
    id_permissao UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(80) NOT NULL UNIQUE,
    descricao VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE usuario_perfil (
    id_usuario_perfil UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_usuario UUID NOT NULL,
    id_perfil UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuario_perfil_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT fk_usuario_perfil_perfil FOREIGN KEY (id_perfil) REFERENCES perfil (id_perfil),
    CONSTRAINT uk_usuario_perfil UNIQUE (id_usuario, id_perfil)
);

CREATE TABLE perfil_permissao (
    id_perfil_permissao UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_perfil UUID NOT NULL,
    id_permissao UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_perfil_permissao_perfil FOREIGN KEY (id_perfil) REFERENCES perfil (id_perfil),
    CONSTRAINT fk_perfil_permissao_permissao FOREIGN KEY (id_permissao) REFERENCES permissao (id_permissao),
    CONSTRAINT uk_perfil_permissao UNIQUE (id_perfil, id_permissao)
);

CREATE TABLE sessao_autenticacao (
    id_sessao_autenticacao UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_usuario UUID NOT NULL,
    refresh_token_hash VARCHAR(255) NOT NULL,
    expira_em TIMESTAMP NOT NULL,
    revogado BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sessao_autenticacao_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
);

CREATE INDEX idx_usuario_username ON usuario (username);
CREATE INDEX idx_usuario_email ON usuario (email);
CREATE INDEX idx_usuario_perfil_usuario ON usuario_perfil (id_usuario);
CREATE INDEX idx_usuario_perfil_perfil ON usuario_perfil (id_perfil);
CREATE INDEX idx_perfil_permissao_perfil ON perfil_permissao (id_perfil);
CREATE INDEX idx_perfil_permissao_permissao ON perfil_permissao (id_permissao);
CREATE INDEX idx_sessao_autenticacao_usuario ON sessao_autenticacao (id_usuario);
