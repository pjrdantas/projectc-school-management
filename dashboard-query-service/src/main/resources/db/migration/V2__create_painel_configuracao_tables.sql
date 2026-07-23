CREATE TABLE painel_publico (
    id UUID PRIMARY KEY,
    escola_id UUID NOT NULL,
    codigo VARCHAR(40) NOT NULL,
    descricao VARCHAR(120) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_painel_publico_escola_codigo UNIQUE (escola_id, codigo)
);

CREATE TABLE painel_configuracao (
    id UUID PRIMARY KEY,
    escola_id UUID NOT NULL,
    publico_id UUID NOT NULL,
    codigo VARCHAR(80) NOT NULL,
    nome VARCHAR(150) NOT NULL,
    descricao TEXT,
    ativo BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_painel_configuracao_publico FOREIGN KEY (publico_id) REFERENCES painel_publico (id),
    CONSTRAINT uk_painel_configuracao_escola_codigo UNIQUE (escola_id, codigo)
);

CREATE TABLE painel_widget (
    id UUID PRIMARY KEY,
    escola_id UUID NOT NULL,
    painel_id UUID NOT NULL,
    codigo VARCHAR(80) NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    descricao TEXT,
    tipo_widget VARCHAR(40) NOT NULL,
    ordem INTEGER NOT NULL,
    query_referencia VARCHAR(150),
    ativo BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_painel_widget_configuracao FOREIGN KEY (painel_id) REFERENCES painel_configuracao (id),
    CONSTRAINT uk_painel_widget_painel_codigo UNIQUE (painel_id, codigo),
    CONSTRAINT uk_painel_widget_painel_ordem UNIQUE (painel_id, ordem)
);

CREATE INDEX idx_painel_configuracao_escola_publico ON painel_configuracao (escola_id, publico_id);
CREATE INDEX idx_painel_widget_escola_painel ON painel_widget (escola_id, painel_id);
