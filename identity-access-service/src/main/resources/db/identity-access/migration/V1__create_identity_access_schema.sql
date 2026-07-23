CREATE TABLE usuario (
    id_usuario UUID NOT NULL,
    username VARCHAR(80) NOT NULL,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    id_escola UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_usuario PRIMARY KEY (id_usuario),
    CONSTRAINT uk_usuario_username UNIQUE (username),
    CONSTRAINT uk_usuario_email UNIQUE (email)
);

CREATE TABLE perfil (
    id_perfil UUID NOT NULL,
    codigo VARCHAR(50) NOT NULL,
    nome VARCHAR(120) NOT NULL,
    descricao VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_perfil PRIMARY KEY (id_perfil),
    CONSTRAINT uk_perfil_codigo UNIQUE (codigo)
);

CREATE TABLE permissao (
    id_permissao UUID NOT NULL,
    codigo VARCHAR(80) NOT NULL,
    descricao VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_permissao PRIMARY KEY (id_permissao),
    CONSTRAINT uk_permissao_codigo UNIQUE (codigo)
);

CREATE TABLE usuario_perfil (
    id_usuario_perfil UUID NOT NULL,
    id_usuario UUID NOT NULL,
    id_perfil UUID NOT NULL,
    CONSTRAINT pk_usuario_perfil PRIMARY KEY (id_usuario_perfil),
    CONSTRAINT fk_usuario_perfil_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_usuario_perfil_perfil FOREIGN KEY (id_perfil)
        REFERENCES perfil (id_perfil),
    CONSTRAINT uk_usuario_perfil UNIQUE (id_usuario, id_perfil)
);

CREATE TABLE perfil_permissao (
    id_perfil_permissao UUID NOT NULL,
    id_perfil UUID NOT NULL,
    id_permissao UUID NOT NULL,
    CONSTRAINT pk_perfil_permissao PRIMARY KEY (id_perfil_permissao),
    CONSTRAINT fk_perfil_permissao_perfil FOREIGN KEY (id_perfil)
        REFERENCES perfil (id_perfil) ON DELETE CASCADE,
    CONSTRAINT fk_perfil_permissao_permissao FOREIGN KEY (id_permissao)
        REFERENCES permissao (id_permissao),
    CONSTRAINT uk_perfil_permissao UNIQUE (id_perfil, id_permissao)
);

CREATE TABLE sessao_autenticacao (
    id_sessao_autenticacao UUID NOT NULL,
    id_usuario UUID NOT NULL,
    id_escola UUID,
    refresh_token_hash VARCHAR(255) NOT NULL,
    access_token_hash VARCHAR(255),
    expira_em TIMESTAMP NOT NULL,
    access_expira_em TIMESTAMP,
    revogado BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_sessao_autenticacao PRIMARY KEY (id_sessao_autenticacao),
    CONSTRAINT fk_sessao_autenticacao_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario) ON DELETE CASCADE,
    CONSTRAINT uk_sessao_refresh_token_hash UNIQUE (refresh_token_hash),
    CONSTRAINT uk_sessao_access_token_hash UNIQUE (access_token_hash)
);

CREATE INDEX idx_sessao_refresh_ativa
    ON sessao_autenticacao (refresh_token_hash, revogado, expira_em);

CREATE INDEX idx_sessao_access_ativa
    ON sessao_autenticacao (access_token_hash, revogado, access_expira_em);
